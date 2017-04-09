package com.agilutiontech.addemo.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkStatus {
	static ConnectivityManager connectivityManager;
	static boolean connected = false;

	public static boolean isOnline(Context con) {

		try {
			connectivityManager = (ConnectivityManager) con
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();

			if (networkInfo != null) {
				if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
					if (networkInfo.isConnectedOrConnecting())
						connected = true;
				if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
					if (networkInfo.isConnectedOrConnecting())
						connected = true;
			}
			return connected;
		} catch (Exception e) {
			System.out
					.println("CheckConnectivity Exception: " + e.getMessage());
			Log.v("connectivity", e.toString());
		}
		return connected;

	}

	public static String getResponce(String strUrl) throws IOException, JSONException {
		//String json = "{\"key\":1}";
		URL url = new URL(strUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("GET");

		// read the response
		InputStream in = new BufferedInputStream(conn.getInputStream());
		String result = IOUtils.toString(in, "UTF-8");

		Log.w("Res", result);

		JSONObject jsonObject = new JSONObject(result);

		in.close();
		conn.disconnect();

		return result;
	}

}
