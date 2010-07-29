package net.processone.wave.api;

import java.util.List;

import net.processone.oauth.ClientSettings;
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

		OneWaveAPI api = new OneWaveAPI(settings);

		// Authorization.
		System.out
				.println("Navigate to this address in your browser. Press ENTER when you're done:");
		System.out.println(api.getUrl());

		// byte[] buffer = new byte[256];
		// int r = System.in.read(buffer);
		// String callbackUrl = new String(buffer, 0, r);
		System.in.read();

		System.out.println(settings.getRequestToken());

		api.start();

		// Search.
		List<Digest> digests = api.search("title:\"solo para ir probando\"", 0 , 10).getDigests();

		if (digests.size() != 1) {
			for (Digest digest : digests)
				System.out.println(digest.getTitle());
			return;
		}

		Digest digest = digests.get(0);

		Wavelet wavelet = api.fetchWavelet(WaveId.deserialise(digest
				.getWaveId()), new WaveletId("googlewave.com", "conv+root"));

		wavelet.reply("\nhola!");

		api.send(wavelet);

		api.stop();
	}
}
