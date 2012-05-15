package ch.reevolt.metronome;

public class Constants {

	/*
	 * For MainActivity
	 */
	public enum State {
		PLAY, PAUSE
	}

	public enum Position {
		LEFT, RIGHT
	}
	
	/*
	 * final constants for Handler
	 */
	public static final int MSG_VIBRATE = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	/*
	 * For metronome
	 */
	public enum Tempo {
		BINARY, TERNARY
	}

	public enum Length {
		CROTCHET, QUAVER
	}

	

}
