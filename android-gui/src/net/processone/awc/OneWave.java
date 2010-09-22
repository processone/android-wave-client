package net.processone.awc;

import java.util.Set;

import net.processone.oauth.ClientSettings;
import net.processone.oauth.Token;
import net.processone.wave.api.OneWaveAPI;
import net.processone.wave.api.OneWaveException;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import android.app.Application;
import android.util.Log;

import com.google.wave.api.SearchResult;
import com.google.wave.api.Wavelet;
import com.google.wave.api.SearchResult.Digest;

public class OneWave extends Application {

	private OneWaveAPI waveAPI;

	private ClientSettings setting;

	@Override
	public void onCreate() {
		super.onCreate();

		setting = new ClientSettings();
		setting.setConsumerToken(new Token("anonymous", "anonymous"));
		setting.setOAuthCallbackUrl("onewavetest://auth");

		waveAPI = new OneWaveAPI(setting);

	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public OneWaveAPI getWaveAPI() {
		return waveAPI;
	}

	public void start() {
		waveAPI.start();
	}

	public SearchResult search(String query, int index, int count) {
		try {
			return waveAPI.search("in:inbox", index, count);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}
		return null;
	}

	public void createWave(String subject, Set<String> participants,
			String message) {

		try {
			Wavelet w = waveAPI.newWavelet(participants);
			w.setTitle(subject);
			w.getRootBlip().appendMarkup(message);
			waveAPI.send(w);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}

	}

	public Wavelet fetchWavelet(WaveId waveId) {

		try {
			return waveAPI.fetchWavelet(waveId);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}

		return null;
	}

	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId) {

		try {
			return waveAPI.fetchWavelet(waveId, waveletId);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}

		return null;
	}

	public void markAsRead(String waveId){
		WaveId w = WaveId.deserialise(waveId);
		markAsRead(w);
	}

	
	public void markAsRead(WaveId waveId){
		try {
			waveAPI.markAsRead(waveId);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}
	}
	
	public void removeWave(String waveId) {
		WaveId w = WaveId.deserialise(waveId);
		removeWave(w);
	}

	public void removeWave(WaveId waveId) {
		try {
			/*
			 * TODO: Remove method is not implemented at API level yet, so this
			 * method will "remove" your wave from your inbox but it won't be
			 * placed into trash folder
			 */
			waveAPI.archive(waveId);
		} catch (OneWaveException e) {
			Log.e(getClass().getName(), e.getLocalizedMessage(), e);
		}
	}

	public void removeWave(Digest digest) {
		removeWave(digest.getWaveId());
	}
}
