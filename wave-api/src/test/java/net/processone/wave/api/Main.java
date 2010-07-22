package net.processone.wave.api;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;
import net.processone.oauth.Token;

@Ignore
public class Main {

	public static void main(String[] args) throws Exception {

		// Component setup.
		ClientSettings settings = new ClientSettings();
		settings.setConsumerToken(new Token("anonymous", "anonymous"));
		settings.setOAuthCallbackUrl("onewavetest://auth");

		OneWaveOAuth oauth = new OneWaveOAuth(settings);
		WaveAPI api = new WaveAPI(oauth);

		// Authorization.
		System.out.println("Navigate to this address in your browser. Press ENTER when you're done:");
		System.out.println(oauth.getUserAuthorizationUrl());
		byte[] buffer = new byte[256];
		int r = System.in.read(buffer);
		//String callbackUrl = new String(buffer, 0, r);
		
		System.out.println(settings.getRequestToken());

		oauth.fetchAccessToken();
		api.setupOAuth(settings.getRequestToken().getPublicKey(), settings
				.getRequestToken().getSecret());

		// Search.
		List<Wave> waves = api.search("in:inbox");

		for (Wave wave : waves)
			System.out.println(wave);

	}
}
