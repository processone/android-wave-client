package net.processone.awc;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewWave extends Activity {

	private OneWave ow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ow = (OneWave) getApplication();

		setContentView(R.layout.newwave);

		Button b = (Button) findViewById(R.id.create);

		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText textView = (EditText) findViewById(R.id.participants);
				String[] p = textView.getText().toString().split(";"); //TODO improve this

				Set<String> participants = new HashSet<String>();

				for (String s : p)
					participants.add(s.trim());

				EditText subject = (EditText) findViewById(R.id.subject);

				EditText message = (EditText) findViewById(R.id.message);

				ow.createWave(subject.getText().toString(), participants,
						message.getText().toString());
				
				finish();
			}
		});

	}
	
}
