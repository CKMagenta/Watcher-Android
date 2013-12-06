/**
 * 
 */
package lis3306.WatcherAndroid;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * @author pong0923
 * @reference http://www.androidpub.com/102370
 */
public class GPSService extends Service {
	private final int SERVICE_ID = 777;
	
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_VALUE = 3;
	
	static final float MIN_CRITERIA_DISTANCE = 0;//500;			// in meters
	static final long MIN_CRITERIA_TIME = 0;//300 * 1000;				// in ms
	
	private static final String url = "http://playbook.cafe24.com/test_mysql.php";
	
	/**
	 * HTTP Connection
	 */
	public InputStream sendFormDataOnConnection(ArrayList<BasicNameValuePair> formData) {
		InputStream content = null;
		StringBuilder sb = new StringBuilder();
        //adding some data to send along with the request to the server
		Iterator<BasicNameValuePair> it = formData.iterator();
		sb.append("dummy=dummy");
		
		while (it.hasNext()) {
			BasicNameValuePair pair = it.next();
			sb.append("&" + pair.getName() + "=" + pair.getValue());	
		}
		
		URL url;
		try {
			url = new URL(GPSService.url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			// this is were we're adding post data to the request
			wr.write(sb.toString());
			wr.flush();
			content = conn.getInputStream();
			wr.close();
		} catch (Exception e) {
			//handle the exception !
			Log.d("SERVICE",e.getMessage());
		}
        return content;
	}
	public String sendFormData(ArrayList<BasicNameValuePair> formData) {
		String content = "";
		try {
//			ArrayList<BasicNameValuePair> formData = new ArrayList<BasicNameValuePair>();
//			formData.add(new BasicNameValuePair("name", "anthony"));
			
			HttpPost request = new HttpPost(GPSService.url);
			request.setEntity(new UrlEncodedFormEntity(formData, "utf-8"));
			
			
			DefaultHttpClient httpClient = new DefaultHttpClient();
			ResponseHandler handler = new BasicResponseHandler();
			String response = httpClient.execute(request, handler);
			content = response;
		} catch (Exception e) {
			 
		}
		
		return content;
	}
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			ArrayList<BasicNameValuePair> formData = new ArrayList<BasicNameValuePair>();
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			formData.add(new BasicNameValuePair("lat", ""+lat) );
			formData.add(new BasicNameValuePair("lon", ""+lon) );
			formData.add(new BasicNameValuePair("TS", ""+System.currentTimeMillis() / 1000L));
			formData.add(new BasicNameValuePair("action", "putGPS"));
			
			// sendFormData(formData);
			sendFormDataOnConnection(formData);
			
			message("GPS("+lat+","+lon+")");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};
	
	
	/**
	 * Service Utility
	 */
	private NotificationManager nm;
	private LocationManager locationManager = null;		// Acquire a reference to the system Location Manager
	void willRegisterService(boolean willRegister) {
		
		if(willRegister == true) {
			nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			CharSequence text = getText(R.string.remote_service_started);
			Notification n = new Notification(android.R.drawable.star_on, text, System.currentTimeMillis());
			PendingIntent contentIntent = PendingIntent.getActivity(GPSService.this, 0, new Intent(GPSService.this, MainActivity.class), 0);
			n.setLatestEventInfo(GPSService.this, getText(R.string.remote_service_label), text, contentIntent);
			nm.notify(SERVICE_ID, n);
			
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//			Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_CRITERIA_TIME, MIN_CRITERIA_DISTANCE, locationListener);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_CRITERIA_TIME, MIN_CRITERIA_DISTANCE, locationListener);
		} else {
			nm.cancel(SERVICE_ID);
			locationManager.removeUpdates(locationListener);
		}
	}
	
	void message(String message) {
		Log.e("SERVICE", message);
		Toast.makeText(GPSService.this, message, Toast.LENGTH_SHORT).show();
	}
	
	
	/**
	 * Service Bind Cycle
	 */
	final Messenger serviceMessenger = new Messenger(new ServiceHandler());
	// private final IBinder binder = new LocalBinder();
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		message("GPSService is onBind");
		return serviceMessenger.getBinder();// return binder;
	}
	
	//ArrayList<Messenger> clients = new ArrayList<Messenger>();
	static Messenger activityMessenger = null;
	class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String text;
			message("HandleMessage : " + msg.what);
			switch(msg.what) {
				case MSG_REGISTER_CLIENT :
					activityMessenger = msg.replyTo;
					// mClients.add(msg.replyTo);
					text = "MSG_REGISTER_CLIENT";
					
					break;
				case MSG_UNREGISTER_CLIENT :
					text = "MSG_UNREGISTER_CLIENT";
					break;
				case MSG_SET_VALUE :
					text = "MSG_SET_VALUE : " + Integer.toString(msg.arg1);
					if(activityMessenger == null) break;
					try {
						activityMessenger.send(Message.obtain(null, MSG_SET_VALUE, msg.arg1, 0));
					} catch (RemoteException e) {}
					break;
					
				default :
					super.handleMessage(msg);
					text = "Default";
			}
			message("HandleMessage : " + text);
		}
	}
	
	
	/**
	 * Service Life Cycle
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		android.os.Debug.waitForDebugger(); 
		message("GPSService is onCreate");
		willRegisterService(true);
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		message("GPSService is onStartCommand with id " + startId);
		return super.onStartCommand(intent, flags, startId);		//return START_STICKY;
	}
	@Override
	public void onDestroy() {
		message("GPSService is onDestroy");
		willRegisterService(false);
		super.onDestroy();
	}
	
	
}
