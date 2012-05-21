package ch.reevolt.metronome.tools;

public class Listener implements Ticker.OnTickListener {

	private final int PRECISION = 3;

	private int[] measure;
	private int index = 0;
	private long time;
	private long lastTime;

	Ticker ticker = new Ticker(3000, false, true);

	public Listener() {

		measure = new int[PRECISION];

		ticker.setOnTickListener(this);

		resetArray();

	}

	public void resetArray() {
		System.out.println("reset array");
		for (int i = 0; i < PRECISION; i++)
			measure[i] = 0;
	}

	/**
	 * return the time between two tap. If number of tap is less than precision,
	 * it's return -1
	 */
	public int tick() {

		time = System.currentTimeMillis();

		ticker.reload();

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
		ticker.stop();
	}

	public void onTickTimeChanged(int time) {
	}

	public void onTickCanceled() {
	}

	public void onTickReloaded() {
		System.out.println("reloaded");
	}
}
