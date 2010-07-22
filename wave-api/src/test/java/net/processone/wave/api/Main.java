package net.processone.wave.api;

import java.util.List;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;
import net.processone.oauth.Token;

import org.junit.Ignore;

import com.google.wave.api.SearchResult.Digest;

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
		System.out
				.println("Navigate to this address in your browser. Press ENTER when you're done:");
		System.out.println(oauth.getUserAuthorizationUrl());

		// byte[] buffer = new byte[256];
		// int r = System.in.read(buffer);
		// String callbackUrl = new String(buffer, 0, r);
		System.in.read();

		System.out.println(settings.getRequestToken());

		oauth.fetchAccessToken();
		api.setupOAuth(settings.getRequestToken().getPublicKey(), settings
				.getRequestToken().getSecret());

		// Search.
		List<Digest> digests = api.search("in:inbox");

		for (Digest digest : digests)
			System.out.println(digest.getTitle());
	}
}
