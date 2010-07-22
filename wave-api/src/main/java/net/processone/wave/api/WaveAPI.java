package net.processone.wave.api;

import java.io.IOException;
import java.util.List;

import net.processone.oauth.OneWaveOAuth;

import com.google.wave.api.AbstractWave;
import com.google.wave.api.SearchResult.Digest;

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

	public List<Digest> search(String query) {
		try {
			return search(query, oauth.getSettings().getRpcHandler());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
