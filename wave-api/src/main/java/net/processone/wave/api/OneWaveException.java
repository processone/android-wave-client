package net.processone.wave.api;

public class OneWaveException extends Exception {

	private static final long serialVersionUID = -5216457217568309336L;

	public OneWaveException() {
		super();
	}

	public OneWaveException(String message, Throwable cause) {
		super(message, cause);
	}

	public OneWaveException(String message) {
		super(message);
	}

	public OneWaveException(Throwable cause) {
		super(cause);
	}
}
