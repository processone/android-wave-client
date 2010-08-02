package net.processone.awc;

import org.waveprotocol.wave.model.id.WaveId;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.wave.api.Blip;
import com.google.wave.api.Wavelet;

public class WaveletList extends ListActivity {

	static final int PROGRESS_DIALOG = 0;

	private OneWave ow;

	private WaveletAdapter waveletAdapter;

	private ProgressDialog progressDialog;

	private FetchWaveletThread progressThread;

	private static Wavelet wavelet;
	private static Blip[] arrayBlip;

	private WaveId waveId;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ow = (OneWave) getApplication();

		String wId = (String) getIntent().getExtras().get("waveId");

		waveId = WaveId.deserialise(wId);

		showDialog(PROGRESS_DIALOG);

		waveletAdapter = new WaveletAdapter(this);

		handler = new Handler() {
			public void handleMessage(Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				setListAdapter(waveletAdapter);
			}
		};

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(WaveletList.this);
			progressDialog.setMessage("Fetching wavelet. Please wait...");
			progressThread = new FetchWaveletThread();
			progressThread.start();
			return progressDialog;
		default:
			return null;
		}
	}

	private class FetchWaveletThread extends Thread {

		public FetchWaveletThread() {

		}

		public void run() {
			wavelet = ow.fetchWavelet(waveId);
			
			if (wavelet == null)
				return;
			
			int size = wavelet.getBlips().size();

			// This is not ok, blips aren't sorted neither nested, so we can't
			// guarantee an order
			arrayBlip = wavelet.getBlips().values().toArray(new Blip[size]);

			Message msg = handler.obtainMessage();

			handler.sendMessage(msg);
		}
	}

	/**
	 * Adapter class to transform Wave bean representation in to Android
	 * structure
	 */
	public static class WaveletAdapter extends BaseAdapter {
		private Context context;

		public WaveletAdapter(Context c) {
			context = c;
		}

		public int getCount() {
			return wavelet.getBlips().size();
		}

		public Blip getItem(int position) {
			return arrayBlip[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				// Make up a new view
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.waveletlist, null);
			} else {
				// Use convertView if it is available
				view = convertView;
			}
			
			TextView t = (TextView) view.findViewById(R.id.titleWavelet);
			t.setText(arrayBlip[position].getContent());
			return view;
		}
	}
}
