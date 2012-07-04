package ch.reevolt.metronome.tools;

import ch.reevolt.java.Ticker;
import ch.reevolt.java.Ticker.OnTickListener;

public class Listener extends Ticker implements OnTickListener {

	private final int PRECISION = 3;

	private int[] measure;
	private int index = 0;
	private long time;
	private long lastTime;

	public Listener() {

		super(3000, false, true);

		this.setOnTickListener(this);

		measure = new int[PRECISION];

		resetArray();
	}

	public void resetArray() {
		for (int i = 0; i < PRECISION; i++)
			measure[i] = 0;
	}

	/**
	 * return the time between two tap. If number of tap is less than precision,
	 * it's return -1
	 */
	public int tick() {

		time = System.currentTimeMillis();

		this.reload();

		/**
		 * computation
		 */
		if (lastTime == 0) {
			measure[index] = 0;
		} else {
			measure[index] = (int) (time - lastTime);
		}

		lastTime = time;

		index = (index + 1) % (PRECISION);

		/**
		 * compute the value to return
		 */
		int retval = 0;
		for (int i = 0; i < PRECISION; i++) {
			if (measure[i] != 0)
				retval += measure[i];
			else
				return -1;
		}

		return retval / (PRECISION);
	}

	public void onTick(int time) {
		resetArray();
		this.stop();
	}
}
