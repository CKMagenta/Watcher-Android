/**
 * 
 */
package lis3306.WatcherAndroid;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

/**
 * @author pong0923
 * @reference http://www.androidpub.com/102370
 */
public class GPSService extends Service {
	private NotificationManager nm;
	private final int SERVICE_ID = 777;
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_VALUE = 3;
	
//	public class LocalBinder extends Binder {
//		GPSService getService() {
//			return GPSService.this;
//		}
//	}

	ArrayList<Messenger> clients = new ArrayList<Messenger>();
	
	static Messenger activityMessenger = null;
	class ServiceHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String text;
			
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
	
}
