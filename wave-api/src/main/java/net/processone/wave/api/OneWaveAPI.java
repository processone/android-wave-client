package net.processone.wave.api;

import java.util.Set;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.ClientWave;
import com.google.wave.api.ClientWaveException;
import com.google.wave.api.FolderAction;
import com.google.wave.api.SearchResult;
import com.google.wave.api.Wavelet;
import com.google.wave.api.ClientWave.JSONRpcHandler;
import com.google.wave.api.SearchResult.Digest;

/**
 * Facade class for the whole Wave API.
 */
public class OneWaveAPI {

	private static final String DOMAIN = "googlewave.com";

	private static final String JSON_MIME_TYPE = "application/json; charset=utf-8";

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

	public OneWaveAPI() {
		this(new ClientSettings());
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

	public void stop() {
		oauth = null;
	}

	public void send(Wavelet wavelet) throws OneWaveException {
		try {
			clientWave.submit(wavelet);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public Wavelet fetchWavelet(WaveId waveId) throws OneWaveException {
		return fetchWavelet(waveId, new WaveletId(DOMAIN, "conv+root"));
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
			return clientWave.newWave(DOMAIN, participants);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public void markAsRead(WaveId waveId) throws OneWaveException {
		try {
			clientWave.folder(FolderAction.MARK_AS_READ, waveId);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public void markAsUnread(WaveId waveId) throws OneWaveException {
		try {
			clientWave.folder(FolderAction.MARK_AS_UNREAD, waveId);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public void mute(WaveId waveId) throws OneWaveException {
		try {
			clientWave.folder(FolderAction.MUTE, waveId);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}

	public void archive(WaveId waveId) throws OneWaveException {
		try {
			clientWave.folder(FolderAction.ARCHIVE, waveId);
		} catch (ClientWaveException e) {
			throw new OneWaveException(e);
		}
	}
}
