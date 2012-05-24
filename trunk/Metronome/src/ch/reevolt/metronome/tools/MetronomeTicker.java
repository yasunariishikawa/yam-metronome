package ch.reevolt.metronome.tools;

public class MetronomeTicker extends Ticker implements Ticker.OnTickListener {

	public enum Note {
		HIGH, LOW
	}

	String[] tempoString = { "Largo", "Lento", "Adagio", "Andante", "Moderato",
			"Allegretto", "Allegro", "Presto", "Prestissimo" };

	private int tempo_int = 60;
	private int time = 1000;

	public int currentTickInMeasure = 0;
	public int timePerMeasure = 4;

	// Tempo mesuration = Tempo.BINARY;
	// Length note = Length.CROTCHET;
	String tempo_string;

	// listener
	OnMetronomeTickListener listener;

	public MetronomeTicker() {
		super(true);
		super.setOnTickListener(this);
	}

	public void setTempo(int tempo) {

		this.tempo_int = tempo;

		int tempo_temp = tempo;

		/*
		 * switch (note) { case CROTCHET: tempo_temp = tempo; break; case QUAVER:
		 * tempo_temp = 2 * tempo; break; }
		 */

		this.time = (60 * 1000) / tempo_temp;

		super.setTime(time);

		/*
		 * set the name of the selected tempo
		 */
		if (tempo < 60)
			this.tempo_string = tempoString[0];
		else if (tempo < 68)
			this.tempo_string = tempoString[1];
		else if (tempo < 80)
			this.tempo_string = tempoString[2];
		else if (tempo < 100)
			this.tempo_string = tempoString[3];
		else if (tempo < 112)
			this.tempo_string = tempoString[4];
		else if (tempo < 128)
			this.tempo_string = tempoString[5];
		else if (tempo < 160)
			this.tempo_string = tempoString[6];
		else if (tempo < 200)
			this.tempo_string = tempoString[7];
		else
			this.tempo_string = tempoString[8];
	}

	/*
	 * public void setMesure(Tempo mesuration) { this.mesuration = mesuration; }
	 */

	public static int toBPM(int ms) {
		return (int) (60 / ((float) ms / 1000));
	}

	public String getTempoName() {
		return this.tempo_string;
	}

	public int getTempo() {
		return tempo_int;
	}

	public int getTime() {
		return time;
	}

	/*
	 * Listener form superClass
	 */

	public void onTick(int time) {
		if (currentTickInMeasure == 0)
			listener.onTick(Note.HIGH);
		else
			listener.onTick(Note.LOW);
		currentTickInMeasure = (currentTickInMeasure + 1) % timePerMeasure;
	}

	public void onTickTimeChanged(int time) {
		currentTickInMeasure = 0;
		listener.onTickChanged(time);
	}

	public void onTickCanceled() {
		listener.onTickCanceled();
	}
	
	public void onTickReloaded() {
	}

	/*
	 * Listenet for Metronome Ticker
	 */
	public interface OnMetronomeTickListener {
		public void onTick(Note note);

		public void onTickChanged(int time);

		public void onTickCanceled();
	}

	public void setOnMetronomeTickListener(OnMetronomeTickListener listener) {
		this.listener = listener;
	}



}
