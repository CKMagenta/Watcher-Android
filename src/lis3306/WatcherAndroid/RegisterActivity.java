package lis3306.WatcherAndroid;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private boolean             _doubleBackToExitPressedOnce    = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//setContentView(R.layout.register_activity);
		
		LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout ll = (LinearLayout)li.inflate(R.layout.register_activity, null, false);
		
		final EditText phonenumberET = (EditText)ll.findViewById(R.id.phonenumber);
		
		final Handler registerHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				String json = (String)msg.obj;
				if(json != null && json.length() > 0 && !json.equals("")) {
					try {
						JSONObject obj = new JSONObject(json);
						boolean success = (Integer.parseInt( obj.getString("success") )) > 0 ? true : false;
						if(success) {
							Intent intent = new Intent();
							Bundle b = new Bundle();
							b.putString("phonenumber", phonenumberET.getText().toString());
							intent.putExtras(b);
							setResult(RESULT_OK, intent);
							finish();
						} else {
							Intent intent = new Intent();
							Bundle b = new Bundle();
							b.putString("message", obj.getString("message"));
							intent.putExtras(b);
							setResult(RESULT_CANCELED, intent);
							finish();
						}
					} catch (JSONException e) {
						Toast.makeText(RegisterActivity.this, "JSON Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						Toast.makeText(RegisterActivity.this, "Something wrong in register precess : " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		};
		
		
		Button submitB = (Button)ll.findViewById(R.id.submit);
		submitB.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Thread registerThread = new Thread() {
					@Override
					public void run() {
						super.run();
						String phonenumber = phonenumberET.getText().toString();
						ArrayList<BasicNameValuePair> formData = new ArrayList<BasicNameValuePair>();
						formData.add(new BasicNameValuePair("action", "registerChildren"));
						formData.add(new BasicNameValuePair("phonenumber", phonenumber));
						String result = GPSService.sendFormData(formData);
						
						registerHandler.sendMessage(registerHandler.obtainMessage(0, result));
					}
				};
				registerThread.start();
			}
		});
		
		setContentView(ll);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		
		/*
	    if (_doubleBackToExitPressedOnce) {
	        super.onBackPressed();
	        finish();
	        System.exit(0);
	        return;
	    }
	    this._doubleBackToExitPressedOnce = true;
	    Toast.makeText(this, "Press again to quit", Toast.LENGTH_SHORT).show();
	    new Handler().postDelayed(new Runnable() {
	        @Override
	        public void run() {
	 
	            _doubleBackToExitPressedOnce = false;
	        }
	    }, 2000);
	    */
	}
}
