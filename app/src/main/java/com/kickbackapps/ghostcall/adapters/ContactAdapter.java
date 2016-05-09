package com.kickbackapps.ghostcall.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.kickbackapps.ghostcall.R;
import com.kickbackapps.ghostcall.customview.CircularImageView;
import com.kickbackapps.ghostcall.model.ContactItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vikash on 1/26/2016.
 */
public class ContactAdapter extends ArrayAdapter<ContactItem> implements Filterable {

    private Context context;
    private List<ContactItem> contacts, filterList;
    private LayoutInflater inflater;
    private ContactFilter filter;

    String filterText = "";

    public ContactAdapter(Context context, List<ContactItem> contacts) {
        super(context, R.layout.list_contact_numbers, contacts);
        this.contacts = contacts;
        this.context = context;
        filterList = new ArrayList<>();
        this.filterList.addAll(contacts);
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new ContactFilter();
        return filter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_contact_numbers, parent, false);
        } else {
            view = convertView;
        }
        viewHolder.name = (TextView) view.findViewById(R.id.name);
        viewHolder.photo = (CircularImageView) view.findViewById(R.id.photo);
        viewHolder.number = (TextView) view.findViewById(R.id.phone);

        String itemName = contacts.get(position).getName();
        if (!filterText.equals("") && contacts.get(position).getName().toLowerCase().contains(filterText.toLowerCase())) {

            int startPos = itemName.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            int endPos = startPos + filterText.length();

            Spannable spannable = new SpannableString(itemName);
            ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{context.getResources().getColor(R.color.bg_register)});
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            viewHolder.name.setText(spannable);
        }else{
            viewHolder.name.setText(contacts.get(position).getName());
        }

        String itemContact = contacts.get(position).getPhone();
        if (!filterText.equals("") && contacts.get(position).getPhone().toLowerCase().contains(filterText.toLowerCase())) {

            int startPos = itemContact.toLowerCase(Locale.US).indexOf(filterText.toLowerCase(Locale.US));
            int endPos = startPos + filterText.length();

            Spannable spannable = new SpannableString(itemContact);
            ColorStateList blueColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{Color.BLUE});
            TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.BOLD, -1, blueColor, null);

            spannable.setSpan(highlightSpan, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            viewHolder.number.setText(spannable);
        }else{
            viewHolder.number.setText(contacts.get(position).getPhone());
        }

        if ((contacts.get(position).getContactImage()) != null) {
            Bitmap contactImage = getContactImage(contacts.get(position).getContactImage());
            viewHolder.photo.setImageBitmap(contactImage);
        } else {
            viewHolder.photo.setImageResource(R.drawable.bg_nav);
        }
        return view;
    }

    private Bitmap getContactImage(byte[] photo) {
        int targetW = 50, targetH = 50;
        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
        options.inJustDecodeBounds = true;
        int imageW = options.outWidth;
        int imageH = options.outHeight;
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(imageW / targetW, imageH / targetH);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeByteArray(photo, 0, photo.length, options);
    }


    public class ViewHolder {
        CircularImageView photo;
        TextView name, number;
    }

    private class ContactFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String data = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            if (data.length() > 0) {
                List<ContactItem> filteredList = new ArrayList<>(filterList);
                List<ContactItem> nList = new ArrayList<>();
                int count = filteredList.size();
                for (int i = 0; i < count; i++) {
                    ContactItem item = filteredList.get(i);
                    String name = item.getName().toLowerCase();
                    String phone = item.getPhone().toLowerCase();
                    if (name.contains(data) || phone.contains(data))
                        nList.add(item);
                }
                results.count = nList.size();
                results.values = nList;
            } else {
                List<ContactItem> list = new ArrayList<>(filterList);
                results.count = list.size();
                results.values = list;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filterText = constraint.toString().toLowerCase();
            contacts = (ArrayList<ContactItem>) results.values;
            clear();
            if (contacts != null) {
                for (int i = 0; i < contacts.size(); i++) {
                    ContactItem item = contacts.get(i);
                    add(item);
                    notifyDataSetChanged();
                }
            }

        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}