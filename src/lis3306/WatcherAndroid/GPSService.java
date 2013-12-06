/**
 * 
 */
package lis3306.WatcherAndroid;

import java.util.ArrayList;

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
	private NotificationManager nm;
	private final int SERVICE_ID = 777;
	static final int GPS_ON = 1;
	static final int GPS_OFF = 2;
	
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_VALUE = 3;
	
	static final float CRITERIA_DISTANCE = 300 * 1000;	// in ms
	static final long CRITERIA_TIME = 500;		// in meters
	
	
	// Acquire a reference to the system Location Manager
	private LocationManager locationManager = null;
	
	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
//			geoCoder = new Geocoder(GPSService.this, Locale.KOREAN);		
//			location.getLatitude();
//			location.getLongitude();
//			speed = (float)(location.getSpeed() * 3.6);
//			List<Address> addresses = geoCorder.getFromLocation(lat, lan, 1);
//			for(Address addr : addresses) {
//				int index = addr.getMaxAddressLineInded();
//				for(int i=0; i<=index; i++) {
//					juso.append(addr.getAddressLine(i));
//					juso.append(" ");
//				}
//				juso.append("\n");
//			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};
	  
	
	ArrayList<Messenger> clients = new ArrayList<Messenger>();
	
	void log(String str) {
		Log.e("SERVICE", str);
	}
	
	static Messenger activityMessenger = null;
	class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String text;
			log("handleMessage");
			switch(msg.what) {
				case MSG_REGISTER_CLIENT :
					activityMessenger = msg.replyTo;
					// mClients.add(msg.replyTo);
					text = "MSG_REGISTER_CLIENT";
					
					break;
				case MSG_UNREGISTER_CLIENT :
					// mClients.remove(msg.replyTo);
					text = "MSG_UNREGISTER_CLIENT";
					break;
				case MSG_SET_VALUE :
//					for (int i=clients.size()-1; i>=0; i--) {
//						try {
//							clients.get(i).send(Message.obtain(null, MSG_SET_VALUE, msg.arg1, 0));
//						} catch(RemoteException e) {
//							clients.remove(i);
//						}
//					}
//					break;
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
			log("hM : " + text);
			Toast.makeText(GPSService.this, text, Toast.LENGTH_SHORT).show();
		}
	}
	
	final Messenger serviceMessenger = new Messenger(new ServiceHandler());
	
	// private final IBinder binder = new LocalBinder();
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		log("GPSService is onBind");
		Toast.makeText(GPSService.this, "GPSService is onBind", Toast.LENGTH_SHORT).show();
		// return binder;
		return serviceMessenger.getBinder();
	}
	
	private void showNotification() {
		CharSequence text = getText(R.string.remote_service_started);
		Notification n = new Notification(android.R.drawable.star_on, text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(GPSService.this, 0, new Intent(GPSService.this, MainActivity.class), 0);
		n.setLatestEventInfo(GPSService.this, getText(R.string.remote_service_label), text, contentIntent);
		nm.notify(R.string.remote_service_started, n);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		log("GPSService is onCreate");
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		Toast.makeText(GPSService.this, "GPSService is onCreate", Toast.LENGTH_SHORT).show();
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		// Register the listener with the Location Manager to receive location updates
//		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, CRITERIA_TIME, CRITERIA_DISTANCE, locationListener);
//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CRITERIA_TIME, CRITERIA_DISTANCE, locationListener);
//		locationManager.removeUpdates(locationListener);

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("GPSService is onStartCommand with id " + startId);
		Toast.makeText(GPSService.this, "GPSService is onStartCommand with id "+startId, Toast.LENGTH_SHORT).show();
		//return START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		log("GPSService is onDestroy");
		nm.cancel(SERVICE_ID);
		Toast.makeText(GPSService.this, "GPSService is onDestroy", Toast.LENGTH_SHORT).show();
	}
	
}
