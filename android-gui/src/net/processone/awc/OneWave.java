package net.processone.awc;

import java.io.IOException;
import java.util.List;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;
import net.processone.oauth.Token;
import net.processone.wave.api.WaveAPI;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import android.app.Application;

import com.google.wave.api.Wavelet;
import com.google.wave.api.SearchResult.Digest;

public class OneWave extends Application {

	private WaveAPI waveAPI;

	private ClientSettings setting;

	private OneWaveOAuth oAuth;

	@Override
	public void onCreate() {
		super.onCreate();

		setting = new ClientSettings();
		setting.setConsumerToken(new Token("anonymous", "anonymous"));
		setting.setOAuthCallbackUrl("onewavetest://auth");

		oAuth = new OneWaveOAuth(setting);

		waveAPI = new WaveAPI(oAuth);

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public WaveAPI getWaveAPI() {
		return waveAPI;
	}

	public String getUserAuthorizationUrl() {
		return oAuth.getUserAuthorizationUrl();
	}

	public void fetchAccessToken() {
		oAuth.fetchAccessToken();
		waveAPI.setupOAuth(setting.getRequestToken().getPublicKey(), setting
				.getRequestToken().getSecret());
	}

	public List<Digest> search(String query) {
		try {
			return waveAPI.search("in:inbox", setting.getRpcHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Wavelet fetchWavelet(WaveId waveId) throws IOException {
		return fetchWavelet(waveId,
				new WaveletId("googlewave.com", "conv+root"), null);
	}

	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId)
			throws IOException {
		return fetchWavelet(waveId, waveletId, null);
	}

	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId) throws IOException {

		Wavelet wavelet = waveAPI.fetchWavelet(waveId, waveletId, setting
				.getRpcHandler());

		return wavelet;
	}
}
