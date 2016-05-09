package com.kickbackapps.ghostcall.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.kickbackapps.ghostcall.GhostCallDatabaseAdapter;
import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.adapters.ContactAdapter;
import com.kickbackapps.ghostcall.model.ContactItem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by viks on 2/25/16.
 */
public class PhoneBookActivity extends AppCompatActivity implements View.OnFocusChangeListener, SearchView.OnQueryTextListener {

    private Activity mActivity;

    private ListView listView;
    private ProgressBar progressBar;

    private List<ContactItem> contactItems;
    private String id, name, phone, image_uri;
    private int queryLength;
    private byte[] contactImage = null;
    private Bitmap bitmap;
    private ContactAdapter adapter;

    private SearchView searchView;
    private MenuItem searchMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phonebook);
        mActivity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Phonebook");

        listView = (ListView) findViewById(R.id.lvContact);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                finishWithResult(i);
            }
        });

        try {
            GhostCallDatabaseAdapter numberAdapter = new GhostCallDatabaseAdapter(mActivity);
            numberAdapter.open();
            numberAdapter.deleteContact();
            numberAdapter.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        new ContactInfo().execute();
    }

    private void finishWithResult(int position) {
        Bundle conData = new Bundle();
        conData.putString("name", contactItems.get(position).getName());
        conData.putString("contact", contactItems.get(position).getPhone());
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onFocusChange(View view, boolean b) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (progressBar.getVisibility() == View.GONE){
            queryLength = newText.length();
            adapter.getFilter().filter(newText);
        }
        return false;
    }

    private void setListAdapter() {
        adapter = new ContactAdapter(mActivity, contactItems);
        listView.setAdapter(adapter);
    }

    private void readContacts() {
        contactItems = new ArrayList<>();
        ContentResolver cr = mActivity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactItem item = new ContactItem();
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                /*if (name != null)
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                else name = "No Name";*/
                image_uri = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        /*phone = phone.replaceAll("\\s+", "");
                        phone = phone.replaceAll("[^0-9]", "");*/
                        phone = phone.replaceAll(" ", "");
                        phone = phone.replaceAll("-", "");
                    }
                    pCur.close();
                }
                if (image_uri != null) {
                    try {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(mActivity.getContentResolver(), Uri.parse(image_uri));
                        contactImage = getImageBytes(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    contactImage = null;
                }

                if (phone != null && !phone.equals("") && name != null) {
                    item.setId(id);
                    item.setName(name);
                    item.setContactImage(contactImage);
                    item.setPhone(phone);
                    contactItems.add(item);

                    try {
                        GhostCallDatabaseAdapter numberAdapter = new GhostCallDatabaseAdapter(mActivity);
                        numberAdapter.open();
                        numberAdapter.createContact(id, name, phone, contactImage);
                        numberAdapter.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }
            }

            Collections.sort(contactItems, new Comparator<ContactItem>() {
                @Override
                public int compare(ContactItem contact1, ContactItem contact2) {
                    return contact1.getName().compareTo(contact2.getName());
                }
            });
        }
    }

    private byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextFocusChangeListener(this);
        searchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ContactInfo extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            readContacts();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            setListAdapter();
        }
    }
}
