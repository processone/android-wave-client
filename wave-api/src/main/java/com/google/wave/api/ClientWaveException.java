package com.google.wave.api;

public class ClientWaveException extends Exception {

	private static final long serialVersionUID = 1193479443766223140L;

	public ClientWaveException() {
		super();
	}

	public ClientWaveException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientWaveException(String message) {
		super(message);
	}

	public ClientWaveException(Throwable cause) {
		super(cause);
	}
}
