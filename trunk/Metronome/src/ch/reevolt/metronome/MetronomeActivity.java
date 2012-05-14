package ch.reevolt.metronome;

import ch.reevolt.metronome.graphic.ButtonImageView;
import ch.reevolt.metronome.graphic.ButtonImageView.OnClickedListener;
import ch.reevolt.metronome.tools.Listener;
import ch.reevolt.metronome.tools.MetronomeTicker;
import ch.reevolt.metronome.tools.Ticker.OnTickListener;
import ch.reevolt.sound.SoundManager;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import it.sephiroth.android.wheel.view.Wheel;
import it.sephiroth.android.wheel.view.Wheel.OnScrollListener;

public class MetronomeActivity extends Activity implements OnScrollListener,
		OnClickedListener, OnClickListener, OnTickListener {

	// used to catch asynchronous tasks
	private Handler handler;

	private int tempo = 60;
	private int time = 1000;

	private MetronomeTicker ticker;

	private enum State {
		PLAY, PAUSE
	}

	private State state = State.PAUSE;

	private enum Position {
		LEFT, RIGHT
	}

	private Position cursorPosition = Position.LEFT;

	// the wheel
	Wheel wheel;

	//
	TextView tempo_view;
	TextView tempo_txt;

	Listener listener;

	// all buttons
	ImageView button_plus;
	ImageView button_minus;
	ButtonImageView button_state;
	ButtonImageView button_micro;
	ButtonImageView button_settings;

	// cursor layout
	ImageView cursor;
	TranslateAnimation animLeft2Right;
	TranslateAnimation animRight2Left;

	// size of the parent of cursor
	int parentWidth;

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
		 * The handler to execute asynchronus taks
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// start cursor animation
				if(msg.obj instanceof String){
					tempo_txt.setTag("" + msg.obj);
				}
				else{
					if (cursorPosition == Position.LEFT)
						cursor.startAnimation(animLeft2Right);
					else
						cursor.startAnimation(animRight2Left);
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
		tempo_view = (TextView) findViewById(R.id.tempo_txt);
		Typeface tf = Typeface
				.createFromAsset(getAssets(), "fonts/digital.ttf");
		tempo_view.setTypeface(tf);

		tempo_txt = (TextView) findViewById(R.id.tempo_txt);

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
		cursor = (ImageView) findViewById(R.id.layout_cursor);
		parentWidth = ((ImageView) findViewById(R.id.layout_cursor_mask))
				.getMeasuredWidth();

		parentWidth = 600;

		/**
		 * Animations for cursor
		 */
		animLeft2Right = new TranslateAnimation(0, parentWidth, 0, 0);
		animLeft2Right.setInterpolator(new LinearInterpolator());
		animLeft2Right.setDuration(time);

		animRight2Left = new TranslateAnimation(parentWidth, 0, 0, 0);
		animRight2Left.setInterpolator(new LinearInterpolator());
		animRight2Left.setDuration(time);

		/**
		 * Sound manager
		 */
		SoundManager.getInstance();
		SoundManager.initSounds(this);
		SoundManager.loadSounds();

		/**
		 * The tempo listener
		 */
		listener = new Listener();

		/**
		 * The metronome ticker
		 */
		ticker = new MetronomeTicker();
		ticker.setOnTickListener(this);

		/**
		 * Initialize tempo
		 */
		setTempo(0);
	}

	/*
	 * 
	 */
	public void setTempo(int tempo) {

		// increment or decrement tempo
		this.tempo = (tempo * 115 / 36) + 155;

		// display tempo
		tempo_view.setText("" + this.tempo);

		// update ticker speed
		ticker.setTempo(this.tempo);
		
		Message msg = new Message();
		msg.obj = ticker.getTempoString();
		handler.sendMessage(msg);
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
				cursorPosition = cursorPosition == Position.LEFT ? Position.RIGHT
						: Position.LEFT;

			}
			break;
		case R.id.button_micro:
			setTempo((MetronomeTicker.toBPM(listener.tick()) - 155) * 36 / 115);
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
			wheel.rotate(270);
			break;
		case R.id.button_minus:
			wheel.rotate(-270);
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

		if (cursorPosition == Position.RIGHT) {
			cursorPosition = Position.LEFT;
		} else {
			cursorPosition = Position.RIGHT;
		}

		handler.sendEmptyMessage(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.Ticker.OnTickListener#onTickCanceled()
	 */
	public void onTickCanceled() {
		// change time to restart anim
		time = -1;
		// stop the cursor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.reevolt.metronome.tools.Ticker.OnTickListener#onTickTimeChanged()
	 */
	public void onTickTimeChanged(int time) {
		animRight2Left.setDuration(time);
		animLeft2Right.setDuration(time);
		
		Message msg = new Message();
		msg.obj = ticker.getTempoString();
		handler.sendMessage(msg);
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
