package com.shockwavegames.qr.generator.activity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

    enum AdType{
        BANNER,
        INTERSTITIAL,
        REWARDED,
        NATIVE,
        APPOPEN
    }
public class AdStatusManager {

    private AdStatusManager() {
        // private constructor to prevent instantiation from outside
    }
    //Test Ad Ids
    static final String AM_TEST_BANNER="ca-app-pub-3940256099942544/6300978111";
    static final String AM_TEST_INTERSTITIAL="ca-app-pub-3940256099942544/8691691433";
    static final String AM_TEST_REWARDED="ca-app-pub-3940256099942544/5224354917";
    static final String AM_TEST_NATIVE="ca-app-pub-3940256099942544/1044960115";
    static final String AM_TEST_APP_OPEN="ca-app-pub-3940256099942544/3419835294";

    //---------------- Fields To Change---------------------------

    //Live Ad Ids -Place Live Ids here
    static final String AM_BANNER="banner id here";
    static final String AM_INTERSTITIAL="interstitial id here";
    static final String AM_REWARDED="rewarded id here";
    static final String AM_NATIVE="native id here";
    static final String AM_APP_OPEN="appopen id here";

    private static String userSignature="fKApYlwsc4Eg1k6TKzfN8l4cB7ri9YZNx3aVlEZSb3YEsvYh0O";
    private static String projectId="234";
    private static String version="1.1";
    private static String platform="Google";


    //----------------- End Fields To Change
    private static AdStatusManager instance;
    private  static boolean statusRunning=false;
    private  static boolean adStatusFetched=false;
    private  static boolean isAdActive=true;
    private static  boolean testAdActive=false;

    private  static boolean logsEnabled=false;


    //call this function to get ids & store it on your local script
    //call it like this if(FetchAdId
//                if(FetchAdId(AdType.BANNER)!=null){
//                      ShowBanner(FetchAdId(AdType.BANNER));// FetchAdId will return Banner Ad Id. similarly do  it for AppOpen etc
//                  }
    public  static String FetchAdId(AdType adType){
        if(!adStatusFetched) {
            ShowLogMessage("Test Ad Status Not Fetched");
            return null;
        }
        if(testAdActive){
            switch (adType) {
                case BANNER:
                    return AM_TEST_BANNER;
                case INTERSTITIAL:
                    return AM_TEST_INTERSTITIAL;
                case REWARDED:
                    return AM_TEST_REWARDED;
                case NATIVE:
                    return AM_TEST_NATIVE;
                case APPOPEN:
                    return AM_TEST_APP_OPEN;
            }
        }else {
            switch (adType) {
                case BANNER:
                    return AM_BANNER;
                case INTERSTITIAL:
                    return AM_INTERSTITIAL;
                case REWARDED:
                    return AM_REWARDED;
                case NATIVE:
                    return AM_NATIVE;
                case APPOPEN:
                    return AM_APP_OPEN;
            }
        }
        return null;
    }


    //call this function only once
     public static void FetchAdStatusFromServer(Context context) {
        if(statusRunning||adStatusFetched) {
            ShowLogMessage("Ad Status Already Received");
            return;
        }
         statusRunning=true;
         String url = "https://products.raveninteractive.io/gdk/fetchproject.php";
         StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                 response -> {
                     String responseLC = response.toLowerCase();
                     if (!responseLC.contains("failed") && !responseLC.contains("invalid")) {//fetched ad status
                         try {
                             JSONObject jsonObject = new JSONObject(response);
                             int testAds=Integer.parseInt(jsonObject.get("TestAds").toString());
                             int enableLogs=Integer.parseInt(jsonObject.get("EnableLog").toString());
                             int showAds = Integer.parseInt(jsonObject.get("ShowAds").toString());
                             logsEnabled=(enableLogs==0)?false:true;
                             testAdActive=(testAds==0)?false:true;
                             if (showAds == 0) {
                                 isAdActive = false;//only deactivate ad if done else always on
                                 ShowLogMessage("Ad Status: Active");
                             } else {
                                 ShowLogMessage("Ad Status: Disabled");
                             }

                         } catch (JSONException e) {
                             // Handle the JSONException
                         }

                     } else {//invalid paramaters provided
                         Log.d("AppDebugging", response);
                     }
                     adStatusFetched = true;
                     statusRunning=false;
                 },
                 error -> {
                     Log.d("AppDebugging", "Error Occurred");
                     adStatusFetched = true;
                     statusRunning=false;
                 }) {
             @Override
             protected Map<String, String> getParams() throws AuthFailureError {
                 Map<String, String> params = new HashMap<>();
                 params.put("Signature", userSignature);
                 params.put("ProjectId", projectId);
                 params.put("Version", version);
                 params.put("Platform", platform);
                 return params;
             }
         };

         RequestQueue requestQueue = Volley.newRequestQueue(context);
         requestQueue.add(stringRequest);
     }


    //ad will only be shown if it returns true
     public static boolean IsAdActive(){
        if(adStatusFetched){
            return isAdActive;
        }
        return false;
    }

    //if true show ads using test ads ids
    public static boolean TestAdsActive(){
        return testAdActive;
    }

    public static boolean LogsEnabled(){
        return logsEnabled;
    }

    public static void ShowLogMessage(String msg) {
        if (logsEnabled)
            Log.d("AppDebugging", msg);
    }
}
