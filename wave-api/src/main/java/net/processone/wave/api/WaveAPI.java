package net.processone.wave.api;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.processone.oauth.OneWaveOAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.wave.api.AbstractWave;

public class WaveAPI extends AbstractWave {

	private OneWaveOAuth oauth;

	public WaveAPI(OneWaveOAuth oauth) {
		this.oauth = oauth;
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

	public List<Wave> search(String query) {
		try {
			String ROBOT_SEARCH_OP_JSON = "{'id':'op1','method':'wave.robot.search','params':{'query':'%s'}}";

			String jsonBody = String.format(ROBOT_SEARCH_OP_JSON, query);

			String r = send(oauth.getSettings().getRpcHandler(),
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
