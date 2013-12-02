/**
 * 
 */
package lis3306.WatcherAndroid;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

/**
 * @author pong0923
 * @reference http://www.androidpub.com/102370
 */
public class GPSService extends Service {
	private final IBinder binder = new LocalBinder();
	private NotificationManager nm;
	private final int SERVICE_ID = 777;
	public class LocalBinder extends Binder {
		GPSService getService() {
			return GPSService.this;
		}
	}

	@Override
	public void onCreate() {
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
		Toast.makeText(this, "GPSService is onCreate", Toast.LENGTH_SHORT).show();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "GPSService is onStartCommand with id "+startId, Toast.LENGTH_SHORT).show();
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		nm.cancel(SERVICE_ID);
		Toast.makeText(this, "GPSService is onDestroy", Toast.LENGTH_SHORT).show();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(this, "GPSService is onBind", Toast.LENGTH_SHORT).show();
		return binder;
	}
	
	private void showNotification() {
		Notification n = new Notification()
		
	}
}
