package lis3306.WatcherAndroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity {
	private boolean isBound = false;
	private final ServiceConnection sc = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			GPSService gps = ((GPSService.LocalBinder)service).getService();
			
			Toast.makeText(MainActivity.this, "Connected to GPSService", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
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
				
				isBound = isActive;
				
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
	
	void doBindService() {
		bindService(new Intent(MainActivity.this, GPSService.class), sc, Context.BIND_AUTO_CREATE);
	}
	
	void doUnbindService() {
		if(isBound) {
			unbindService(sc);
		}
	}

}
