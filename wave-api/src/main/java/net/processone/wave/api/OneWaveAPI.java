package net.processone.wave.api;

import java.util.Set;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.ClientWave;
import com.google.wave.api.ClientWaveException;
import com.google.wave.api.SearchResult;
import com.google.wave.api.Wavelet;
import com.google.wave.api.ClientWave.JSONRpcHandler;

/**
 * Facade class for the whole Wave API.
 */
public class OneWaveAPI {

	public static final String JSON_MIME_TYPE = "application/json; charset=utf-8";

	private OneWaveOAuth oauth;

	private ClientWave clientWave;

	public OneWaveAPI(final ClientSettings settings) {

		clientWave = new ClientWave(new JSONRpcHandler() {

			public String request(String jsonBody) throws Exception {

				return oauth.send(settings.getRpcUrl(), JSON_MIME_TYPE,
						jsonBody).readBodyAsString();
			}
		});

		oauth = new OneWaveOAuth(settings);
	}

	public void start() {
		oauth.fetchAccessToken();
	}

	public SearchResult search(String query, int index, int numResults)
			throws OneWaveException {
		try {
			return clientWave.search(query, index, numResults);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public String getUrl() {
		return oauth.getUserAuthorizationUrl();
	}

	public void stop() throws OneWaveException {
		oauth = null;
	}

	public void send(Wavelet wavelet) throws OneWaveException {
		try {
			clientWave.submit(wavelet);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId)
			throws OneWaveException {
		try {
			return clientWave.fetchWavelet(waveId, waveletId);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public Wavelet newWavelet(Set<String> participants) throws OneWaveException {
		try {
			return clientWave.newWave("googlewave.com", participants);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}
}
