package com.agilutiontech.addemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.agilutiontech.addemo.common.Addvertise;
import com.agilutiontech.addemo.common.NetworkStatus;
import com.agilutiontech.addemo.common.ServerUtility;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Context context;

    ImageView imageView;
    RequestQueue requestQueue;
    ImageLoader imageLoader;
    FrameLayout container;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private String uuid = "";
    SharedPreferences sharedPreferences;
    private DBHandler db;
    List<Addvertise> Ads = new ArrayList<>();
    private String SendtoServerUrl = "http://julfikarmahmud.16mb.com/adApp/ws/offline.php";
    private int permissionCheck = 0;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        sharedPreferences = context.getSharedPreferences("AdPref", 0);

        /*** database ***/

        db = new DBHandler(this);
        db.getReadableDatabase();
        db.getWritableDatabase();

/*** Reading all Ads from table for offline***/
        Ads = db.getAllAds();

        final TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uuid = sharedPreferences.getString("uuid", "");
/*** unique device id***/

        /***check permission for marshmallow***/
        if (Build.VERSION.SDK_INT >= 23) {
            permissionCheck = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 0);
            }
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (TextUtils.isEmpty(uuid)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    uuid = tManager.getDeviceId();
                }
            } else {
                uuid = tManager.getDeviceId();
            }
            sharedPreferences.edit().putString("uuid", uuid).commit();
        }
        imageView = (ImageView) findViewById(R.id.imageViewAd);

        /*** Called Api loop for Ads***/
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        if (!TextUtils.isEmpty(uuid)) {
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    uuid = tManager.getDeviceId();
                                }
                            } else {
                                uuid = tManager.getDeviceId();
                            }
                            ad(String.format(ServerUtility.appUrlAd, sharedPreferences.getInt("currIndex", 0), uuid));
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000);

        /*** time delay in milliseconds ,upload ads to server after it back online***/

        Handler removeAds = new Handler();
        removeAds.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (NetworkStatus.isOnline(context)) {
                    if (Ads.size() > 0) {
                        for (int i = 0; i < Ads.size(); i++) {
                            SendOfflineAdsToServer(Ads.get(i).getIndex(), Ads.get(i).getTime());
                        }
                        db.removeAdsFromDatabase();
                    }
                }
            }
        }, 10000);


    }
/***Called APi for the Ads***/
    void ad(String url) {
        System.out.println("-->"+url);
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if (entry != null) {
            try {
                String data = new String(entry.data, "UTF-8");
                parsingData(data.toString());
                if (NetworkStatus.isOnline(context)) {
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            url, null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("Volly", response.toString());
                                    parsingData(response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d("Volly", "Error: " + error.getMessage());
                        }
                    });

                    AppController.getInstance().addToRequestQueue(jsonObjReq, url);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Volly", response.toString());
                            parsingData(response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("Volly", "Error: " + error.getMessage());
                }
            });
            AppController.getInstance().addToRequestQueue(jsonObjReq, url);
        }
    }


   /*** parse data to server online mode***/
    void parsingData(String strResponce) {
        try {
            JSONObject jsonObject = new JSONObject(strResponce);
            if (jsonObject.getInt("success") == 1) {
                Log.w("url", jsonObject.getString("url"));
                sharedPreferences.edit().putInt("currIndex", jsonObject.getInt("index")).commit();

                ImageLoader imageLoader = AppController.getInstance()
                        .getImageLoader();

                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText("" + jsonObject.getString("index"));

//                imageView.setImageUrl(jsonObject.getString("url"), imageLoader);

                String url = jsonObject.getString("url");
                if (!NetworkStatus.isOnline(context)) {
                    Addvertise addvertise = new Addvertise();
                    addvertise.setIndex(String.valueOf(jsonObject.getInt("index")));
                    String time = parseDateToYYYYMMDD(Calendar.getInstance().getTime().toString());
                    addvertise.setTime(time);
                    db.insertAdd(addvertise);
                }

                // We first check for cached request
                Cache cache = AppController.getInstance().getRequestQueue().getCache();
                Cache.Entry entry = cache.get(url);
                if (entry != null) {

                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(entry.data, 0, entry.data.length));

                    // check if internet is there and make fresh request
                    if (NetworkStatus.isOnline(context)) {

                        // making fresh volley request and getting json
                        ImageRequest request = new ImageRequest(url,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        VolleyLog.d("Volley", "Response: ");
                                        imageView.setImageBitmap(bitmap);
                                    }
                                }, 0, 0, null,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                });

                        AppController.getInstance().addToRequestQueue(request);

                    }

                } else {
                    // Retrieves an image specified by the URL, displays it in the UI.
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    VolleyLog.d("Volley", "Response: ");
                                    imageView.setImageBitmap(bitmap);
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                }
                            });
                    AppController.getInstance().addToRequestQueue(request);
                }

            } else {
                //Toast.makeText(context, "Data not available", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


   /*** send Ads to server when it come back from offline to online***/
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void SendOfflineAdsToServer(String index, String time) {
        URL obj = null;
        try {
            obj = new URL(SendtoServerUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String params = "uid=" + uuid;
            params = params + "&index[]=" + index + "&time[]=" + time;
//            params= URLEncoder.encode(params);

            byte[] postData = params.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;

            // Send post request
            con.setDoOutput(true);
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            con.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + SendtoServerUrl);
            System.out.println("Post parameters : " + params);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
/***date format***/
    public String parseDateToYYYYMMDD(String time) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = formatter.format(Date.parse(time));
        return date;
    }
}
