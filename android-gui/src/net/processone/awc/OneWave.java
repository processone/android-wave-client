package net.processone.awc;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;
import net.processone.oauth.Token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import android.app.Application;

import com.google.wave.api.Wavelet;

public class OneWave extends Application {

	private static final String ROBOT_SEARCH_OP_JSON = "{'id':'op1','method':'wave.robot.search','params':{'query':'%s'}}";

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

	public List<Wave> search(String query) {

		try {
			//TODO implement it as a GSON transform class, if its possible
			String jsonBody = String.format(ROBOT_SEARCH_OP_JSON, query);

			String r = waveAPI.send(setting.getRpcHandler(),
					"application/json", jsonBody);

			JSONObject resp = new JSONObject(r);

			JSONArray array = resp.getJSONObject("data").getJSONObject(
					"searchResults").getJSONArray("digests");

			List<Wave> waves = new LinkedList<Wave>();

			for (int i = 0; i < array.length(); i++) {
				JSONObject jwave = array.getJSONObject(i);
				waves.add(new Wave(jwave.getString("waveId"), jwave
						.getString("title")));
			}

			return waves;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
