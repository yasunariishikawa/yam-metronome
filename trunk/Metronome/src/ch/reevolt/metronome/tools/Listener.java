package ch.reevolt.metronome.tools;

public class Listener {

	int[] measure;
	final int precision = 3;
	int index = 0;
	long time;
	long lastTime = 0;

	public Listener() {
		measure = new int[precision];

		for (int i = 0; i < precision; i++) {
			measure[i] = 0;
		}
	}

	public int tick() {

		time = System.currentTimeMillis();

		/**
		 * computation
		 */
		if (lastTime == 0) {
			measure[index] = 0;
		} else {
			measure[index] = (int) (time - lastTime);
		}

		lastTime = time;

		index = (index + 1) % (precision);

		/**
		 * returned value
		 */
		int retval = 0;
		for (int i = 0; i < precision; i++) {
			retval += measure[i];
		}
		
		System.out.println("value is " + retval/precision);

		return retval / (1000* precision);
	}
}
