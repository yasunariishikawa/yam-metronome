package ch.reevolt.metronome.graphic;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

public class Lighter extends Timer {

	private Handler handler;
	private TimerTask timerTask;

	public Lighter(Handler handler) {
		this.handler = handler;
	}

	public void start() {
		timerTask = new TimerTask() {
			
			byte value = (byte) 0;

			@Override
			public void run() {
				Message msg = new Message();
				msg.arg1 = value++;
				handler.sendMessage(msg);
				if (value == 100)
					this.cancel();
			}
		};
		this.scheduleAtFixedRate(timerTask, 0, 2);
	}

}
