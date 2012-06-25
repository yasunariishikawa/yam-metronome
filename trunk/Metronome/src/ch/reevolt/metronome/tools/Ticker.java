package ch.reevolt.metronome.tools;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * A dynamic Timer inheriting of Java.util.Timer. It's able to
 * start/stop/reload time, change the tick time during clicking synchronously or
 * asynchronously. It's including a OnTickListener interface to callback.
 * 
 * @author Romain Cherix
 * 
 */
public class Ticker extends Timer {

	private int targetTime;
	private int time = targetTime;
	private boolean timeMustBeUpdated = false;
	private boolean synchronousTime;
	private Task task;
	private OnTickListener listener;
	private boolean isCreated = false;
	private boolean startAfterTime = false;

	/**
	 * Constructor
	 * 
	 * @param synchronous
	 *            specify if the time must change synchronously with tick
	 */
	public Ticker(boolean synchronous) {
		super();
		this.synchronousTime = synchronous;
	}

	/**
	 * Constructor
	 * 
	 * @param time
	 *            the tick time
	 * @param synchronous
	 *            specify if the time must change synchronously with tick
	 */
	public Ticker(int time, boolean synchronous) {
		super();
		this.setTime(time);
		this.synchronousTime = synchronous;
	}

	/**
	 * Constructor
	 * 
	 * @param time
	 *            the tick time
	 * @param synchronous
	 *            specify if the time must change synchronously with tick
	 * @param startAfterTime
	 *            specify if first tick is called on timer start or after the
	 *            first tick time
	 */
	public Ticker(int time, boolean synchronous, boolean startAfterTime) {
		super();
		this.setTime(time);
		this.synchronousTime = synchronous;
		this.startAfterTime = startAfterTime;
	}

	/**
	 * Start the timer
	 */
	public void start() {
		task = new Task();
		if (startAfterTime)
			scheduleAtFixedRate(task, targetTime, targetTime);
		else
			scheduleAtFixedRate(task, 0, targetTime);
		isCreated = true;
	}

	/**
	 * Stop the timer
	 */
	public void stop() {
		task.cancel();
		isCreated = false;
		listener.onTickCanceled();
	}

	/**
	 * Set time tick time
	 * 
	 * @param ms
	 *            the time to tick
	 */
	public void setTime(int ms) {
		targetTime = ms;
		if (synchronousTime)
			timeMustBeUpdated = true;
		else
			changeTime();
	}

	/**
	 * Reload the timer
	 */
	public void reload() {
		if (isCreated)
			stop();
		start();
		if (listener != null)
			listener.onTickReloaded();
	}

	

	/**
	 * Interface that implements method called on timer behavior
	 * 
	 * @author romain
	 * 
	 */
	public interface OnTickListener {
		/**
		 * 
		 * @param time
		 *            tick time
		 */
		public void onTick(int time);

		/**
		 * 
		 * @param time
		 *            new tick time
		 */
		public void onTickTimeChanged(int time);

		/**
		 * called when timer is cancelled
		 */
		public void onTickCanceled();

		/**
		 * called when timer is reloaded
		 */
		public void onTickReloaded();
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
	
	private void changeTime() {
		time = targetTime;
		timeMustBeUpdated = false;
		if (listener != null)
			listener.onTickTimeChanged(time);
		if (isCreated) {
			stop();
			start();
		}
	}

}
