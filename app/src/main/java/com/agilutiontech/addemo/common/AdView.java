package com.agilutiontech.addemo.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.LruCache;

import com.agilutiontech.addemo.AppController;
import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

public class AdView extends NetworkImageView {

    Context context;
    SharedPreferences sharedPreferences;
    RequestQueue requestQueue;
    ImageLoader imageLoader;

    private AdView(Context context, AttributeSet attrs) {
        super(context, attrs);

        sharedPreferences = context.getSharedPreferences("AdPref", 0);

        // Called Api
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        ad(String.format(ServerUtility.appUrlAd, sharedPreferences.getInt("currIndex", 0)));
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 20000);

    }

    void ad(String url)
    {
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(url);
        if(entry != null){
            try {
                String data = new String(entry.data, "UTF-8");
                parsingData(data.toString());
                if(NetworkStatus.isOnline(context))
                {
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            url, null,
                            new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    //Log.d("Volly", response.toString());
                                    parsingData(response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //VolleyLog.d("Volly", "Error: " + error.getMessage());
                        }
                    });

                    AppController.getInstance().addToRequestQueue(jsonObjReq, url);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.d("Volly", response.toString());
                            parsingData(response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //VolleyLog.d("Volly", "Error: " + error.getMessage());
                }
            });
            AppController.getInstance().addToRequestQueue(jsonObjReq, url);
        }
    }

    void parsingData(String strResponce)
    {
        try {
            JSONObject jsonObject = new JSONObject(strResponce);
            if(jsonObject.getInt("success") == 1) {
                //Log.w("url", jsonObject.getString("url"));
                sharedPreferences.edit().putInt("currIndex", jsonObject.getInt("index")).commit();

                requestQueue = Volley.newRequestQueue(context);
                imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
                    public void putBitmap(String url, Bitmap bitmap) {
                        mCache.put(url, bitmap);
                    }
                    public Bitmap getBitmap(String url) {
                        return mCache.get(url);
                    }
                });

                setImageUrl(jsonObject.getString("url"), imageLoader);

            } else {
                //Toast.makeText(context, "Data not available", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
