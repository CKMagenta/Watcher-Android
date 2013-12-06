/**
 * 
 */
package lis3306.WatcherAndroid;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
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
	
	/**
	 * HTTP Connection
	 */
	public InputStream getInputStreamFromUrl(String url) {
		InputStream content = null;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			List nameValuePairs = new ArrayList(1);
			//this is where you add your data to the post method
			nameValuePairs.add(new BasicNameValuePair("name", "anthony"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httpPost);
			content = response.getEntity().getContent();
			
			return content;
		} catch (Exception e) {
			return null; 
		}
	}
	
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			double lat = location.getLatitude();
			double lon = location.getLongitude();
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
