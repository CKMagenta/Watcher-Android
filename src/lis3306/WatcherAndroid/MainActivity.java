package lis3306.WatcherAndroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity {
	
	
	class ActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case GPSService.MSG_SET_VALUE:
					Toast.makeText(MainActivity.this, "Received from service : " + msg.arg1, Toast.LENGTH_SHORT).show();
					break;
				default :
					super.handleMessage(msg);
			}
			
		}
	}
	
	
	final Messenger activityMessenger = new Messenger(new ActivityHandler());
	Messenger serviceMessenger = null;
	private final ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			serviceMessenger = new Messenger(service);
			try {
				Message msg = Message.obtain(null, GPSService.MSG_REGISTER_CLIENT);
				msg.replyTo = activityMessenger;
				serviceMessenger.send(msg);
				
				msg = Message.obtain(null, GPSService.MSG_SET_VALUE, this.hashCode(), 0);
				serviceMessenger.send(msg);
			} catch (RemoteException e) {}
			
			// GPSService gps = ((GPSService.LocalBinder)service).getService();

			Toast.makeText(MainActivity.this, "Connected to GPSService", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			serviceMessenger = null;
			Toast.makeText(MainActivity.this, "Disconnected to GPSService", Toast.LENGTH_SHORT).show();
		}
		
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences sp = getSharedPreferences("watcherPref", Activity.MODE_PRIVATE);
		isBound = sp.getBoolean("activity", false);
		
		Preference activityPref = findPreference("activity");
		activityPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				
				boolean isActive = (Boolean)newValue;
				
				if(isActive) {
					doBindService();
				} else {
					doUnbindService();
				}
				
				// isBound = isActive;
				
				SharedPreferences sp = getSharedPreferences("watcherPref", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putBoolean("activity", isActive);
				editor.commit();
				return true;
			}
		});
		
		Preference periodPref = findPreference("period");
		periodPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences sp = getSharedPreferences("watcherPref", Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("period", (Integer)newValue);
				editor.commit();
				return true;
			}
		});
	}

	private boolean isBound = false;
	
	void doBindService() {
		isBound = bindService(new Intent(MainActivity.this, GPSService.class), sc, Context.BIND_AUTO_CREATE);
	}
	
	void doUnbindService() {
		if(isBound) {
			unbindService(sc);
			isBound = false;
		}
	}

}
