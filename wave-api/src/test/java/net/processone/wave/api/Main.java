package net.processone.wave.api;

import java.util.List;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.OneWaveOAuth;
import net.processone.oauth.Token;

import org.junit.Ignore;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.Wavelet;
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
		List<Digest> digests = api.search("title:\"solo para ir probando\"");

		if (digests.size() != 1) {
			for (Digest digest : digests)
				System.out.println(digest.getTitle());
			return;
		}

		Digest digest = digests.get(0);

		Wavelet wavelet = api.fetchWavelet(WaveId.deserialise(digest
				.getWaveId()), new WaveletId("googlewave.com", "conv+root"),
				settings.getRpcHandler());

		wavelet.reply("\nhola!");
		
		api.send(wavelet, settings.getRpcHandler());
	}
}
