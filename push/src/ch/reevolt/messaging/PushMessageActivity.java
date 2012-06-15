package ch.reevolt.messaging;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PushMessageActivity extends Activity implements OnClickListener {

	final private String server_address = "romain.cherix@gmail.com";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.button_connect).setOnClickListener(this);
		findViewById(R.id.button_disconnect).setOnClickListener(this);
	}

	private void registerPush() {
		Intent registrationIntent = new Intent(
				"com.google.android.c2dm.intent.REGISTER");
		registrationIntent.putExtra("app", PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(), 0));
		registrationIntent.putExtra("sender", server_address);
		getApplicationContext().startService(registrationIntent);
	}

	private void unregisterPush() {
		Intent unregistrationIntent = new Intent(
				"com.google.android.c2dm.intent.UNREGISTER");
		unregistrationIntent.putExtra("app", PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(), 0));
		getApplicationContext().startService(unregistrationIntent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_connect:
			registerPush();
			break;
		case R.id.button_disconnect:
			unregisterPush();
			break;
		}
	}
}