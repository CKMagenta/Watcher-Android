/**
 * 
 */
package lis3306.WatcherAndroid;

import lis3306.WatcherAndroid.MainActivity.ServiceHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.widget.Toast;

/**
 * @author pong0923
 * @reference http://www.androidpub.com/102370
 */
public class GPSService extends Service {
	private NotificationManager nm;
	private final int SERVICE_ID = 777;
//	public class LocalBinder extends Binder {
//		GPSService getService() {
//			return GPSService.this;
//		}
//	}

	class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			
			}
			
			Toast.makeText(GPSService.this, "", Toast.LENGTH_SHORT);
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		Toast.makeText(this, "GPSService is onCreate", Toast.LENGTH_SHORT).show();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "GPSService is onStartCommand with id "+startId, Toast.LENGTH_SHORT).show();
		//return START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		nm.cancel(SERVICE_ID);
		Toast.makeText(this, "GPSService is onDestroy", Toast.LENGTH_SHORT).show();
	}
	
	
	
	final Messenger serviceMessenger = new Messenger(new ServiceHandler());
	
	// private final IBinder binder = new LocalBinder();
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "GPSService is onBind", Toast.LENGTH_SHORT).show();
		// return binder;
		return serviceMessenger.getBinder();
	}
	
	private void showNotification() {
		CharSequence text = getText(R.string.remote_service_started);
		Notification n = new Notification(android.R.drawable.star_on, text, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		n.setLatestEventInfo(this, getText(R.string.remote_service_label), text, contentIntent);
		nm.notify(R.string.remote_service_started, n);
		
	}
}
