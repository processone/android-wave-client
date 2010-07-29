package net.processone.wave.api;

import java.io.IOException;
import java.util.List;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;

import com.google.wave.api.AbstractWave;
import com.google.wave.api.SearchResult;
import com.google.wave.api.Wavelet;
import com.google.wave.api.SearchResult.Digest;

public class WaveAPI extends AbstractWave {

	private ClientSettings settings;

	private OneWaveOAuth oauth;

	private String url;

	public WaveAPI(ClientSettings settings) {

		this.settings = settings;

		oauth = new OneWaveOAuth(settings);

		url = oauth.getUserAuthorizationUrl();
	}

	public void start() {
		oauth.fetchAccessToken();
		setupOAuth(settings.getRequestToken().getPublicKey(), settings
				.getRequestToken().getSecret());
	}

	public void setupOAuth(String consumerKey, String consumerSecret,
			String rpcServerUrl) {
		super.setupOAuth(consumerKey, consumerSecret, rpcServerUrl);
	}

	@Override
	protected void setupVerificationToken(String verificationToken,
			String securityToken) {
		super.setupVerificationToken(verificationToken, securityToken);
	}

	public String send(String rpcHandler, String contentType, String jsonBody)
			throws IOException {
		return oauth.send(rpcHandler, contentType, jsonBody).readBodyAsString();
	}

	public SearchResult search(String query, int index, int numResults) {
		try {
			return search(query,index, numResults, settings.getRpcHandler());
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
		this.send(wavelet, settings.getRpcHandler());
	}

	public Wavelet fetchWavelet(WaveId deserialise, WaveletId waveletId)
			throws IOException {
		return this.fetchWavelet(deserialise, waveletId, settings
				.getRpcHandler());
	}
}
