package ch.reevolt.metronome.tools;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * Dynamic Timer, including OnTickListener. Able to set the time dynamically.
 * 
 * @author romain
 * 
 */
public class Ticker extends Timer {

	private int targetTime = 1000;

	private int time = targetTime;

	private boolean timeMustBeUpdated = false;

	private Task task;

	private OnTickListener listener;

	private boolean isCreated = false;

	public Ticker() {
		super();
	}

	public void start() {
		task = new Task();
		scheduleAtFixedRate(task, 0, targetTime);
		isCreated = true;
	}

	public void stop() {
		task.cancel();
		isCreated = false;
		listener.onTickCanceled();
	}

	public void setTime(int ms) {
		targetTime = ms;
		timeMustBeUpdated = true;
	}

	public void changeTime() {
		time = targetTime;
		timeMustBeUpdated = false;
		listener.onTickTimeChanged(time);
		if (isCreated) {
			stop();
			start();
		}

	}

	/**
	 * Interface for callback
	 * 
	 * @author romain
	 * 
	 */
	public interface OnTickListener {
		public void onTick(int time);

		public void onTickTimeChanged(int time);

		public void onTickCanceled();
	}

	public void setOnTickListener(OnTickListener listener) {
		this.listener = listener;
	}

	/**
	 * The task to perform
	 * 
	 * @author romain
	 * 
	 */
	private class Task extends TimerTask {
		@Override
		public void run() {
			if (timeMustBeUpdated)
				changeTime();
			else {
				time = targetTime;
				listener.onTick(time);
			}
		}
	}

}
