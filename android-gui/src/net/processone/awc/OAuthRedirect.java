package net.processone.awc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class OAuthRedirect extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		OneWave ow = (OneWave) getApplication();
		String url = ow.getWaveAPI().getUrl();

		if (url == null) {
			System.out.println("Phone off or Internet access is not allowed");
			return;
		}

		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);

	}

	@Override
	public void onResume() {
		super.onResume();

	}

}