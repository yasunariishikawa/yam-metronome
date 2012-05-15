package ch.reevolt.metronome;

import ch.reevolt.metronome.graphic.ButtonImageView;
import ch.reevolt.metronome.graphic.ButtonImageView.OnClickedListener;
import ch.reevolt.metronome.tools.Listener;
import ch.reevolt.metronome.tools.MetronomeTicker;
import ch.reevolt.metronome.tools.Ticker.OnTickListener;
import ch.reevolt.sound.SoundManager;
import ch.reevolt.metronome.Constants.*;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import it.sephiroth.android.wheel.view.Wheel;
import it.sephiroth.android.wheel.view.Wheel.OnScrollListener;

public class MetronomeActivity extends Activity implements OnScrollListener,
		OnClickedListener, OnClickListener, OnTickListener {

	// used to catch asynchronous tasks
	private Handler handler;

	private MetronomeTicker ticker;

	private State state = State.PAUSE;

	private int cursorPosition = Constants.LEFT;

	// the wheel
	Wheel wheel;

	//
	TextView tempo_view_int;
	TextView tempo_view_string;

	// tempo listener
	Listener tap_listener;

	// all buttons
	ImageView button_plus;
	ImageView button_minus;
	ButtonImageView button_state;
	ButtonImageView button_micro;
	ButtonImageView button_settings;

	// cursor layout
	FrameLayout layout_cursor_container;
	ImageView cursor;
	TranslateAnimation animLeft2Right;
	TranslateAnimation animRight2Left;

	// size of the parent of cursor
	int parentWidth = 0;

	// vibrator
	Vibrator vibrator;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// remove notification bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// set in fullscreen mode
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		/**
		 * get vibrator
		 */
		vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

		/**
		 * The handler to execute asynchronous tasks
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);

				switch (msg.what) {
				case Constants.MSG_VIBRATE:
					vibrator.vibrate(10);
					break;
				case Constants.LEFT:
					cursor.startAnimation(animLeft2Right);
					break;
				case Constants.RIGHT:
					cursor.startAnimation(animRight2Left);
					break;
				}
			}
		};

		/**
		 * The wheel
		 */
		wheel = (Wheel) findViewById(R.id.wheel);
		wheel.setOnScrollListener(this);

		/**
		 * Tempo visualizer
		 */
		tempo_view_int = (TextView) findViewById(R.id.tempo_int);
		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/digital.ttf");
		tempo_view_int.setTypeface(tf);

		tempo_view_string = (TextView) findViewById(R.id.tempo_txt);

		/**
		 * set listener for play/pause button
		 */
		button_state = (ButtonImageView) findViewById(R.id.button_state);
		button_state.setOnClickedListener(this);

		/**
		 * set listener for plus and minus button
		 */
		button_plus = (ImageView) findViewById(R.id.button_plus);
		button_plus.setOnClickListener(this);
		button_minus = (ImageView) findViewById(R.id.button_minus);
		button_minus.setOnClickListener(this);
		button_micro = (ButtonImageView) findViewById(R.id.button_micro);
		button_micro.setOnClickedListener(this);

		/**
		 * cursor layout
		 */
		layout_cursor_container = (FrameLayout) findViewById(R.id.layout_cursor_container);
		cursor = (ImageView) findViewById(R.id.layout_cursor);

		/**
		 * The metronome ticker
		 */
		ticker = new MetronomeTicker();
		ticker.setOnTickListener(this);

		/**
		 * Sound manager
		 */
		SoundManager.getInstance();
		SoundManager.initSounds(this);
		SoundManager.loadSounds();

		/**
		 * The tempo listener
		 */
		tap_listener = new Listener();

		/**
		 * Initialize tempo
		 */
		setTempo(0);
	}

	public void initializeAnimation() {
		/**
		 * Animations for cursor
		 */
		animLeft2Right = new TranslateAnimation(0, parentWidth, 0, 0);
		animLeft2Right.setInterpolator(new LinearInterpolator());
		animLeft2Right.setDuration(ticker.getTime());

		animRight2Left = new TranslateAnimation(parentWidth, 0, 0, 0);
		animRight2Left.setInterpolator(new LinearInterpolator());
		animRight2Left.setDuration(ticker.getTime());
	}

	/*
	 * 
	 */
	public void setTempo(int tempo) {

		// increment or decrement tempo
		ticker.setTempo((tempo * 115 / 36) + 155);

		// display tempo
		tempo_view_string.setText("" + ticker.getTempoName());
		tempo_view_int.setText("" + ticker.getTempo());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.reevolt.metronome.graphic.ButtonImageView.OnClickedListener#onButtonClick
	 * (android.view.View)
	 */
	public void onButtonClick(View v) {
		switch (v.getId()) {
		case R.id.button_state:
			if (state == State.PAUSE) {
				// set layout parent width
				if (parentWidth == 0) {
					parentWidth = layout_cursor_container.getWidth();
					initializeAnimation();
				}

				// raplace play button by pause button
				button_state.setImageDrawable(getResources().getDrawable(
						R.drawable.pause));

				// change state
				state = State.PLAY;

				// disable autotempo button
				button_micro.setEnable(true);

				// run ticker
				ticker.start();

			} else {
				button_state.setImageDrawable(getResources().getDrawable(
						R.drawable.play));
				// stop ticker
				button_micro.setEnable(false);
				state = State.PAUSE;
				ticker.stop();

				// replace cursor at the right emplacement
				//cursorPosition = cursorPosition == Constants.LEFT ? Constants.RIGHT
					//	: Constants.LEFT;

			}
			break;
		case R.id.button_micro:
			setTempo((MetronomeTicker.toBPM(tap_listener.tick()) - 155) * 36 / 115);
			break;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.button_plus:
			wheel.rotate(Constants.RIGHT);

			break;
		case R.id.button_minus:
			wheel.rotate(Constants.LEFT);
			break;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.Ticker.OnTickListener#onTick(int)
	 */
	public void onTick(int time) {
		// start sound
		// ....
		
		cursorPosition = cursorPosition == Constants.LEFT ? Constants.RIGHT
				: Constants.LEFT;
		
		handler.sendEmptyMessage(cursorPosition);

		/*if (cursorPosition == Constants.RIGHT) {
			cursorPosition = Constants.LEFT;
			handler.sendEmptyMessage(Constants.LEFT);
		} else {
			cursorPosition = Constants.RIGHT;
			handler.sendEmptyMessage(Constants.RIGHT);
		}*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.Ticker.OnTickListener#onTickCanceled()
	 */
	public void onTickCanceled() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.Ticker.OnTickListener#onTickTimeChanged()
	 */
	public void onTickTimeChanged(int time) {
		animRight2Left.setDuration(time);
		animLeft2Right.setDuration(time);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.sephiroth.android.wheel.view.Wheel.OnScrollListener#onScrollStarted
	 * (it.sephiroth.android.wheel.view.Wheel, float, int)
	 */
	public void onScrollStarted(Wheel view, float value, int roundValue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.sephiroth.android.wheel.view.Wheel.OnScrollListener#onScroll(it.sephiroth
	 * .android.wheel.view.Wheel, float, int)
	 */
	public void onScroll(Wheel view, float value, int roundValue) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.sephiroth.android.wheel.view.Wheel.OnScrollListener#onScrollFinished
	 * (it.sephiroth.android.wheel.view.Wheel, float, int)
	 */
	public void onScrollFinished(Wheel view, float value, int roundValue) {
		setTempo(roundValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.sephiroth.android.wheel.view.Wheel.OnScrollListener#onScrollValueChanged
	 * (it.sephiroth.android.wheel.view.Wheel, float, int)
	 */
	public void onScrollValueChanged(Wheel view, float value, int roundValue) {
		// SoundManager.playSound(1, 1);
		setTempo(roundValue);
	}

}
