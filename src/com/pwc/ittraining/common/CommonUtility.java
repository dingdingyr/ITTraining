package com.pwc.ittraining.common;

import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.pwc.ittraining.R;

public class CommonUtility {
	
	/**
	 * Get current device ID.
	 * @param context the current activity
	 * @return device ID as String
	 */
	public static String GetDeviceID(Context context){
		String deviceID="";

		deviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		/*
		final TelephonyManager tm = 
				(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		deviceID = tm.getDeviceId();
		*/		
		return deviceID;		
	}
	
	/**
	 * Get the value with token from SharedPreference named "AndroidSSO"
	 * @param context the current activity
	 * @param token the name of the Preference to retrieve
	 * @return the value of the Preference; if the Preference does not exist, return ""
	 */
	public static String GetStringFromSP(Context context, String token){
        try {
            //con = createPackageContext("com.android.androidsso", 0);
            SharedPreferences pref = context.getSharedPreferences("AndroidSSO", 0);
            String dataShared = pref.getString(token, "");
            
            return dataShared;
        }
        catch (Exception e) {
            Log.e("No shared data", e.toString());
            return "";
        }
	}

	/**
	 * Get the HttpClient that uses a SSL connection
	 * @return A HttpClient object using SSL connection 
	 */
	public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);

            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();

            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();

            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
	
	/**
	 * Convert a date object to String with "yyyy-MM-dd HH:mm:ss" format
	 * @param date A date object
	 * @return String with "yyyy-MM-dd HH:mm:ss" format
	 */
	public static String getFormatDate(Date date){
		java.text.DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(date);
	}
	
	/**
	 * Convert a String to date object. 
	 * @param date The String with "yyyy-MM-dd HH:mm:ss" format
	 * @return A date object
	 */
	public static Date parseDate(String date){
		try{
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return df.parse(date);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the installed pwc apps 
	 * @param context The current service
	 * @return
	 */
	public static String GetInstalledApps(Context context){
		StringBuilder appList = new StringBuilder(); 

		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0); 

		String packageName = "";
		for(int i=0;i<packages.size();i++) { 
			PackageInfo packageInfo = packages.get(i); 	
			packageName=packageInfo.packageName.toLowerCase();
			
			if(packageName.contains("pwc")){
				appList.append(packageInfo.packageName.split("\\.")[2]);
				appList.append(";");
			}				
		}
		
		return appList.toString();
	}
	
	/**
	 * Collapse the input keyboard 
	 * @param context the current activity
	 */
	public static void collapseKeyboard(Context context){
		InputMethodManager imm = (InputMethodManager)
				context.getSystemService(Context.INPUT_METHOD_SERVICE);   
		if (imm.isActive()) { 
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 
					InputMethodManager.HIDE_NOT_ALWAYS);  
		}
		 
	}

	public static Bitmap getImage(byte[] pic){
		Bitmap image = null;
		BitmapFactory.Options options = new Options(); 
		options.inDither = true;
		options.inPreferredConfig = null; //the best way to decode
		
		options.inSampleSize = 2;
		
		if(pic != null){
			image = BitmapFactory.decodeByteArray(pic, 0, pic.length , options);
		}
		
		return image;
	}
	
	public static Bitmap getLargeImage(byte[] pic){
		Bitmap image = null;
		BitmapFactory.Options options = new Options(); 
		options.inDither = true;
		options.inPreferredConfig = null; //the best way to decode
		
		//options.inSampleSize = 2;
		
		if(pic != null){
			image = BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
		}
		return image;
	}
	
	public static void setTopBar(Context context, ActionBar actionbar){		
		actionbar.setDisplayShowHomeEnabled(false);
		actionbar.setDisplayShowTitleEnabled(true);
		
		actionbar.setBackgroundDrawable(
				new ColorDrawable(context.getResources().getColor(R.color.tangerine)));
	}
	
	public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
               return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
               View listItem = listAdapter.getView(i, null, listView);
               listItem.measure(0, 0);  

               totalHeight += listItem.getMeasuredHeight();  
               
               

        }

        

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));

    
        listView.setLayoutParams(params);

	}

}

