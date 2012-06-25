package ch.reevolt.metronome.graphic;

import ch.reevolt.metronome.Constants;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.TranslateAnimation;

public class CursorAnimation extends TranslateAnimation {

	private int cursorPosition = Constants.LEFT;

	public CursorAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CursorAnimation(float fromXDelta, float toXDelta) {
		super(fromXDelta, toXDelta, 0, 0);
	}

	public CursorAnimation(int fromXType, float fromXValue, int toXType,
			float toXValue, int fromYType, float fromYValue, int toYType,
			float toYValue) {
		super(fromXType, fromXValue, toXType, toXValue, fromYType, 0, toYType, 0);
	}

	public void changeDirection() {
		cursorPosition = cursorPosition == Constants.LEFT ? Constants.RIGHT
				: Constants.LEFT;
	}

}
