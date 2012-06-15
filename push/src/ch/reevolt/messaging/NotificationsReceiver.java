package ch.reevolt.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationsReceiver extends C2DMBroadcastReceiver {
	
	@Override
	protected void onError(Context context, String error) {
		Toast.makeText(context, error, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onRegistration(Context context, String registrationId) {
		Toast.makeText(context, registrationId, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onUnregistration(Context context) {
		Toast.makeText(context, "disconnected to C2DM", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onMessageReceived(Context context, Intent intent) {

		String message = intent.getStringExtra("message"); // data.message contient le texte de la notification
		String title = "Push message";
		int iconId = R.drawable.icon;

		// création de la notification :
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(iconId, message, System.currentTimeMillis()); 

		// création de l'activité à démarrer lors du clic :
		Intent notifIntent = new Intent(context.getApplicationContext(), PushMessageActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifIntent, 0);

		// affichage de la notification dans le menu déroulant :
		notification.setLatestEventInfo(context, title, message, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL; // la notification disparaitra une fois cliquée

		// lancement de la notification :
		notificationManager.notify(1, notification);
	}

}
