package oauh.test;

import android.app.Application;

public class OneWave extends Application {

	public OneWaveOAuth oAuth;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ClientSettings settings = new ClientSettings();
		settings.setConsumerToken(new Token("anonymous", "anonymous"));
		settings.setOAuthCallbackUrl("onewavetest://auth");

		oAuth = new OneWaveOAuth(settings);
		
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
