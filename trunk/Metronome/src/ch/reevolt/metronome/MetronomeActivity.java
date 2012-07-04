package ch.reevolt.metronome;

<<<<<<< .mine
=======

>>>>>>> .r35
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
<<<<<<< .mine
import ch.reevolt.android.graphics.widget.ButtonImageView;
import ch.reevolt.android.graphics.widget.ButtonImageView.OnClickedListener;
import ch.reevolt.android.sound.SoundManager;
=======
import ch.reevolt.metronome.graphic.ButtonImageView;
import ch.reevolt.metronome.graphic.ButtonImageView.OnClickedListener;
>>>>>>> .r35
import ch.reevolt.metronome.tools.Listener;
import ch.reevolt.metronome.tools.MetronomeTicker;
import ch.reevolt.metronome.tools.MetronomeTicker.Note;
import ch.reevolt.metronome.tools.MetronomeTicker.OnMetronomeTickListener;
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
		OnClickedListener, OnClickListener, OnMetronomeTickListener,
		OnWheelChangedListener {

	// used to catch asynchronous tasks
	private Handler handler;

	private MetronomeTicker ticker;

	private State state = State.PAUSE;

	private int cursorPosition = Constants.LEFT;

	// the tempo wheel
	Wheel wheel;

	// the meter wheel
	WheelView wheel_meter;

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

	// note button
	ButtonImageView button_note1;
	ButtonImageView button_note2;
	ButtonImageView button_note3;
	ButtonImageView button_note4;

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
				default:
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
		button_micro.setBehavior(ButtonImageView.Behavior.PUSH);

		/**
		 * set note button
		 */
		button_note1 = (ButtonImageView) findViewById(R.id.button_first_note);
		button_note1.setOnClickedListener(this);
		button_note2 = (ButtonImageView) findViewById(R.id.button_second_note);
		button_note2.setOnClickedListener(this);
		button_note3 = (ButtonImageView) findViewById(R.id.button_third_note);
		button_note3.setOnClickedListener(this);
		button_note4 = (ButtonImageView) findViewById(R.id.button_fourth_note);
		button_note4.setOnClickedListener(this);

		/**
		 * cursor layout
		 */
		layout_cursor_container = (FrameLayout) findViewById(R.id.layout_cursor_container);
		cursor = (ImageView) findViewById(R.id.layout_cursor);

		/**
		 * The metronome ticker
		 */
		ticker = new MetronomeTicker();
		ticker.setOnMetronomeTickListener(this);

		/**
		 * Sound manager
		 */
		SoundManager.getInstance();
		SoundManager.initSounds(this);
		SoundManager.addSound(0, R.raw.tick_down);
		SoundManager.addSound(0, R.raw.tick);

		/**
		 * The tempo listener
		 */
		tap_listener = new Listener();

		/**
		 * Initialize tempo
		 */
		setTempo(125);
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
		ticker.setTempo(tempo);

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
				button_micro.setEnable(false);

				cursorPosition = Constants.LEFT;

				cursor.setVisibility(ImageView.VISIBLE);

				// run ticker
				ticker.start();

			} else {
				button_state.setImageDrawable(getResources().getDrawable(
						R.drawable.play));
				// stop ticker
				button_micro.setEnable(true);
				state = State.PAUSE;
				ticker.stop();
				cursor.setVisibility(ImageView.INVISIBLE);
			}
			break;

		case R.id.button_micro:

			int time = tap_listener.tick();
			if (time != -1)
				setTempo(MetronomeTicker.toBPM(time));
			break;

		case R.id.button_first_note:
			button_note1.setVisible(!button_note1.isVisible());
			System.out.println("first note");
			break;

		case R.id.button_second_note:
			button_note2.setVisible(!button_note2.isVisible());
			break;

		case R.id.button_third_note:
			button_note3.setVisible(!button_note3.isVisible());
			break;

		case R.id.button_fourth_note:
			button_note4.setVisible(!button_note4.isVisible());
			break;

		default:
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
		default:

		}
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
		setTempo(roundValue * 85 / 36 + 125);
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
		setTempo(roundValue * 85 / 36 + 125);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.reevolt.metronome.tools.MetronomeTicker.OnMetronomeTickListener#onTick
	 * (ch.reevolt.metronome.tools.MetronomeTicker.Note)
	 */
	public void onTick(Note note) {
		// start sound
		if (note == Note.HIGH)
			SoundManager.playSound(0, 0);
		else
			SoundManager.playSound(1, 0);

		cursorPosition = cursorPosition == Constants.LEFT ? Constants.RIGHT
				: Constants.LEFT;

		handler.sendEmptyMessage(cursorPosition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.MetronomeTicker.OnMetronomeTickListener#
	 * onTickChanged(int)
	 */
	public void onTickChanged(int time) {
		animRight2Left.setDuration(time);
		animLeft2Right.setDuration(time);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.MetronomeTicker.OnMetronomeTickListener#
	 * onTickCanceled()
	 */
	public void onTickCanceled() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kankan.wheel.widget.OnWheelChangedListener#onChanged(kankan.wheel.widget
	 * .WheelView, int, int)
	 */
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		switch (wheel.getId()) {
		case 0:
			ticker.timePerMeasure = newValue;
			break;
		default:

		}
	}

}
