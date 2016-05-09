package com.kickbackapps.ghostcall;

import com.kickbackapps.ghostcall.objects.AreaCodeObject;
import com.kickbackapps.ghostcall.objects.CallData;
import com.kickbackapps.ghostcall.objects.CreditPackagesData;
import com.kickbackapps.ghostcall.objects.NumberPackagesData;
import com.kickbackapps.ghostcall.objects.backgroundeffects.BackgroundEffectsData;
import com.kickbackapps.ghostcall.objects.numbers.ExtendObject;
import com.kickbackapps.ghostcall.objects.numbers.Message;
import com.kickbackapps.ghostcall.objects.soundeffects.SoundEffectsData;
import com.kickbackapps.ghostcall.user.CallStatus;
import com.kickbackapps.ghostcall.user.UserData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.RestMethod;

/**
 * Created by Ynott on 7/28/15.
 */
public interface GhostCallAPIInterface {

    @GET("/credit_packages")
    List<CreditPackagesData> getCreditPackagesData();

    @GET("/effects")
    List<SoundEffectsData> getsoundEffectsList();

    @FormUrlEncoded
    @POST("/effects")
    void sendEffects(@Field("effect_item_id") String effectID, @Field("resource_id") String resourceID, Callback<Response> soundEffectStatus);

    @GET("/packages?type=new")
    List<NumberPackagesData> getNewNumberPackages();

    @GET("/packages?type=extend")
    List<NumberPackagesData> getExtendNumberPackages();

    @GET("/backgrounds")
    List<BackgroundEffectsData> getBackgroundEffectsList();

    @GET("/user")
    UserData getUserData();

    @GET("/call_status")
    void getCallStatus(@Query("resource_id") String resourceID, Callback<CallStatus> statusCallback);

    @GET("/user")
    void getUserData(Callback<UserData> callBack);

    @FormUrlEncoded
    @POST("/hangup")
    void hangUpCall(@Field("resource_id") String resourceID, Callback<Response> hangUpResponse);

    @FormUrlEncoded
    @POST("/calls")
    void makeCall(@Field("to") String to, @Field("number_id") String numberID, @Field("background_item_id") String backgroundID, @Field("voicechanger") String voicechanger,
                  @Field("record") String record, @Field("use_verified_number") String verified, @Field("method") String method, Callback<CallData> callback);

    @GET("/playback/call/{callID}/mp3")
    void getMP3(@Path("callID") String callID, @Query("api_key") String api_key, Callback<Response> responseCallback);

    @FormUrlEncoded
    @POST("/messages")
    void sendText(@Field("to") String to, @Field("number_id") String numberID, @Field("text") String text, Callback<Response> response);

    @GET("/messages/{numberID}/{timestamp}")
    List<Message> getMessages(@Path("numberID") String numberID, @Path("timestamp") String timeStamp);

    @GET("/available_area_code/{area_code}")
    AreaCodeObject getAreaCodeStatus(@Path("area_code") String areaCode);

    @FormUrlEncoded
    @POST("/purchase")
    void purchaseCredits(@Field("type") String type, @Field("item") String item, @Field("token") String token, @Field("transaction_id") String transactionID, Callback<Response> response);

    @FormUrlEncoded
    @POST("/purchase")
    void purchaseNewNumber(@Field("type") String type, @Field("item") String item, @Field("name") String nickName, @Field("area_code") String areaCode, @Field("token") String token, @Field("transaction_id") String transactionID, Callback<Response> response);

    @FormUrlEncoded
    @POST("/purchase")
    void extendNumber(@Field("type") String type, @Field("item") String item, @Field("number_id") String numberID, @Field("token") String token, @Field("transaction_id") String transactionID, Callback<ExtendObject> extendObject);

    // This is necessary if there is parameter for DELETE method.
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @RestMethod(value = "DELETE", hasBody = true)
    public @interface BODY_DELETE {
        String value();
    }

    @FormUrlEncoded
    @BODY_DELETE("/numbers/{numberID}")
    void deleteNumber(@Path("numberID") String numberID, @Field("number_id") String id, Callback<Response> cb);

    @FormUrlEncoded
    @PUT("/settings/{numberID}")
    void changeSettings(@Path("numberID") String numberID, @Field("name") String name, @Field("disable_calls") boolean calls, @Field("disable_messages") boolean messages,
                         @Field("voicemail") boolean voicemail, Callback<Response> response);

    @FormUrlEncoded
    @PUT("/settings/{numberID}")
    void changeNumberName(@Path("numberID") String numberID, @Field("name") String name, Callback<Response> response);

}
