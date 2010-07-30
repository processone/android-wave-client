package net.processone.awc;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class CallbackOAuth extends Activity {

	private OneWave ow;

	@Override
	public void onResume() {
		super.onResume();

		// extract the OAUTH access token if it exists
		Uri uri = this.getIntent().getData();

		if (uri != null) {
			ow = (OneWave) getApplication();

			// This should be here, instead a filter may be a better option
			ow.start();
			
			startActivity(new Intent(this, WaveList.class));
			finish(); //TODO: this is ugly

		}
	}
}
