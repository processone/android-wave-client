package net.processone.wave.api;

import java.io.IOException;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.ClientWave;
import com.google.wave.api.SearchResult;
import com.google.wave.api.Send;
import com.google.wave.api.Wavelet;

public class OneWaveAPI implements Send {

	private ClientSettings settings;

	private OneWaveOAuth oauth;

	private String url;

	private ClientWave clientWave;

	public OneWaveAPI(ClientSettings settings) {

		this.settings = settings;

		clientWave = new ClientWave(this);

		oauth = new OneWaveOAuth(settings);

		url = oauth.getUserAuthorizationUrl();
	}

	public void start() {
		oauth.fetchAccessToken();
	}

	public String request(String rpcHandler, String contentType, String jsonBody)
			throws IOException {
		return oauth.send(rpcHandler, contentType, jsonBody).readBodyAsString();
	}

	public SearchResult search(String query, int index, int numResults) {
		try {
			return clientWave.search(query,index, numResults, settings.getRpcHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public String getUrl() {
		return url;
	}

	public void stop() {
		oauth = null;
		settings = null;
		url = null;
	}

	public void send(Wavelet wavelet) {
		clientWave.send(wavelet, settings.getRpcHandler());
	}

	public Wavelet fetchWavelet(WaveId deserialise, WaveletId waveletId)
			throws IOException {
		return clientWave.fetchWavelet(deserialise, waveletId, settings
				.getRpcHandler());
	}
}
