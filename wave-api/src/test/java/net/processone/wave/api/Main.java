package net.processone.wave.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.waveprotocol.wave.model.id.WaveId;

import com.google.wave.api.Wavelet;
import com.google.wave.api.SearchResult.Digest;

@Ignore
public class Main {

	public static void main(String[] args) throws Exception {

		OneWaveAPI api = new OneWaveAPI();

		// Authorization.
		System.out
				.println("Navigate to this address in your browser. Press ENTER when you're done:");
		System.out.println(api.getUrl());

		// byte[] buffer = new byte[256];
		// int r = System.in.read(buffer);
		// String callbackUrl = new String(buffer, 0, r);
		System.in.read();

		api.start();

		Set<String> participants = new HashSet<String>();
		participants.add("mschonaker@googlewave.com");
		participants.add("pablo.polvorin@googlewave.com");
		participants.add("vidalsantiagomartinez@googlewave.com");
		Wavelet wavelet = api.newWavelet(participants);
		wavelet.setTitle("Created programmatically");
		wavelet
				.getRootBlip()
				.appendMarkup(
						String
								.format(
										"<h1 style=\"color:blue\">Checkout class <span style=\"color:red\">%s</span>!</h1>",
										Main.class.getName()));
		api.send(wavelet);

		api.stop();
	}

	public static void fetchAndReply(OneWaveAPI api, String title,
			String message) throws OneWaveException {
		// Search.
		List<Digest> digests = api.search("title:\"" + title + "\"", 0, 10)
				.getDigests();

		if (digests.size() != 1) {
			for (Digest digest : digests)
				System.out.println(digest.getTitle());
			return;
		}

		Digest digest = digests.get(0);

		Wavelet wavelet = api.fetchWavelet(WaveId.deserialise(digest
				.getWaveId()));

		wavelet.reply("\n" + message);
		api.send(wavelet);
	}

	public static void markRead(OneWaveAPI api) throws OneWaveException {
		List<Digest> digests = api.search("title:\"tagman\"", 0, 10)
				.getDigests();

		WaveId id = WaveId.deserialise(digests.get(0).getWaveId());

		api.markAsRead(id);

		api.stop();
	}
}
