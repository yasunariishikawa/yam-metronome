package ch.reevolt.metronome;

import ch.reevolt.android.graphics.widget.ButtonImageView;
import ch.reevolt.android.graphics.widget.ButtonImageView.OnClickedListener;
import ch.reevolt.android.sound.SoundManager;
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
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import it.sephiroth.android.wheel.view.Wheel;
import it.sephiroth.android.wheel.view.Wheel.OnScrollListener;

public class MetronomeActivity extends Activity implements OnScrollListener,
		OnClickedListener, OnMetronomeTickListener {

	// used to catch asynchronous tasks
	private Handler handler;

	private MetronomeTicker ticker;

	private State state = State.PAUSE;

	private int cursorPosition = Constants.LEFT;

	// the tempo wheel
	Wheel wheel;

	//
	TextView tempo_view_int;
	TextView tempo_view_string;

	// tempo listener
	Listener tap_listener;

	// all buttons
	ButtonImageView button_state;
	ButtonImageView button_micro;
	ButtonImageView button_settings;

	// note button
	ButtonImageView button_note1;
	ButtonImageView button_note2;
	ButtonImageView button_note3;
	ButtonImageView button_note4;

	// meter button
	ButtonImageView button_binary;
	ButtonImageView button_ternary;

	// subdivision button
	ButtonImageView button_croche;
	ButtonImageView button_double;
	ButtonImageView button_triolet;

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
		button_micro = (ButtonImageView) findViewById(R.id.button_micro);
		button_micro.setOnClickedListener(this);
		button_micro.setBehavior(ButtonImageView.Behavior.PUSH);

		/**
		 * set note button
		 */
		button_note1 = (ButtonImageView) findViewById(R.id.button_first_note);
		button_note1.setOnClickedListener(this);
		button_note1.setAlphaValueDisabled(50);
		button_note1.setEnable(false);
		button_note2 = (ButtonImageView) findViewById(R.id.button_second_note);
		button_note2.setOnClickedListener(this);
		button_note2.setAlphaValueDisabled(50);
		button_note2.setEnable(false);
		button_note3 = (ButtonImageView) findViewById(R.id.button_third_note);
		button_note3.setOnClickedListener(this);
		button_note3.setAlphaValueDisabled(50);
		button_note3.setEnable(false);
		button_note4 = (ButtonImageView) findViewById(R.id.button_fourth_note);
		button_note4.setOnClickedListener(this);
		button_note4.setAlphaValueDisabled(50);
		button_note4.setEnable(false);

		/**
		 * set meter button
		 */
		button_binary = (ButtonImageView) findViewById(R.id.binary);
		button_binary.setOnClickedListener(this);
		button_binary.setAlphaValueDisabled(50);
		button_ternary = (ButtonImageView) findViewById(R.id.ternary);
		button_ternary.setOnClickedListener(this);
		button_ternary.setEnable(false);
		button_ternary.setAlphaValueDisabled(50);

		/**
		 * set subdivision buttons
		 */
		button_croche = (ButtonImageView) findViewById(R.id.button_sub_croche);
		button_croche.setOnClickedListener(this);
		button_croche.setAlphaValueDisabled(50);
		button_croche.setEnable(false);
		button_double = (ButtonImageView) findViewById(R.id.button_sub_double);
		button_double.setOnClickedListener(this);
		button_double.setAlphaValueDisabled(50);
		button_double.setEnable(false);
		button_triolet = (ButtonImageView) findViewById(R.id.button_sub_triolet);
		button_triolet.setOnClickedListener(this);
		button_triolet.setAlphaValueDisabled(50);
		button_triolet.setEnable(false);

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
		SoundManager.addSound(1, R.raw.tick);

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

		case R.id.binary:
			button_binary.setVisible(true);
			button_ternary.setVisible(false);
			break;

		case R.id.ternary:
			button_binary.setVisible(false);
			button_ternary.setVisible(true);
			break;
			
		case R.id.button_sub_croche:
			button_croche.setVisible(!button_croche.isVisible());
			button_double.setVisible(false);
			button_triolet.setVisible(false);
			break;
			
		case R.id.button_sub_double:
			button_croche.setVisible(false);
			button_double.setVisible(!button_double.isVisible());
			button_triolet.setVisible(false);
			break;
			
		case R.id.button_sub_triolet:
			button_croche.setVisible(false);
			button_double.setVisible(false);
			button_triolet.setVisible(!button_triolet.isVisible());
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

}
