package ch.reevolt.metronome.tools;

public class MetronomeTicker extends Ticker {

	String[] tempoString = { "Largo", "Lento", "Adagio", "Andante", "Moderato",
			"Allegretto", "Allegro", "Presto", "Prestissimo" };

	public enum Tempo {
		BINARY, TERNARY
	}

	public enum Length {
		CROTCHET, QUAVER
	}

	Tempo mesuration = Tempo.BINARY;
	Length note = Length.CROTCHET;
	String tempo;

	public MetronomeTicker() {
		super();
	}

	public void setTempo(int tempo) {

		int intTempo = tempo;

		switch (note) {
		case CROTCHET:
			intTempo = tempo;
			break;
		case QUAVER:
			intTempo = 2 * tempo;
			break;
		}

		super.setTime((60 * 1000) / intTempo);

		if (tempo < 60)
			this.tempo = tempoString[0];
		else if (tempo < 68)
			this.tempo = tempoString[1];
		else if (tempo < 80)
			this.tempo = tempoString[2];
		else if (tempo < 100)
			this.tempo = tempoString[3];
		else if (tempo < 112)
			this.tempo = tempoString[4];
		else if (tempo < 128)
			this.tempo = tempoString[5];
		else if (tempo < 160)
			this.tempo = tempoString[6];
		else if (tempo < 200)
			this.tempo = tempoString[7];
		else
			this.tempo = tempoString[8];
	}

	public void setMesure(Tempo mesuration) {
		this.mesuration = mesuration;
	}

	public static int toBPM(int ms) {
		return (int) (60 / ((float) ms / 1000));
	}
	
	public String getTempoString(){
		return this.tempo;
	}

}
