package com.google.wave.api;

import java.io.IOException;

public interface Send {

	public String request(String rpcHandler, String contentType, String jsonBody)
			throws IOException;
}
