package com.google.wave.api;

import junit.framework.Assert;

import org.junit.Test;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.wave.api.ClientWave.JSONRpcHandler;

public class ClientWaveTest {

	@Test
	public void testEmptyDeserialize() throws Exception {

		ClientWave wave = new ClientWave(new JSONRpcHandler() {

			public String request(String jsonBody) throws Exception {

				Assert
						.assertEquals(
								"[{\"method\":\"wave.robot.fetchWave\",\"id\":\"op1\",\"params\":{\"waveId\":\"googlewave.com!testwaveid\",\"waveletId\":\"googlewave.com!conv+root\"}}]",
								jsonBody);

				return "[{\"id\":\"op1\",\"data\":{}}]";
			}
		});

		Wavelet wavelet = wave.fetchWavelet(new WaveId("googlewave.com",
				"testwaveid"), new WaveletId("googlewave.com", "conv+root"));

		Assert.assertNull(wavelet);
	}
}
