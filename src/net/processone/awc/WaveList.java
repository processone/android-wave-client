package net.processone.awc;

import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WaveList extends ListActivity {

	static final int PROGRESS_DIALOG = 0;

	private OneWave ow;

	private static List<Wave> waveList;

	private WaveAdapter waveAdapter;

	private ProgressDialog progressDialog;

	private FetchWavesThread progressThread;

	// Define the Handler that receives messages from the thread and update the
	// UI
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ow = (OneWave) getApplication();

		showDialog(PROGRESS_DIALOG);

		waveAdapter = new WaveAdapter(this);

		handler = new Handler() {
			public void handleMessage(Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				setListAdapter(waveAdapter);
			}
		};

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(WaveList.this);
			progressDialog.setMessage("Loading waves. Please wait...");
			progressThread = new FetchWavesThread(handler);
			progressThread.start();
			return progressDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		Wave wave = (Wave) listView.getItemAtPosition(position);
		String waveId = wave.getWaveId();

		Intent i = new Intent(this, WaveletList.class);
		i.putExtra("waveId", waveId);
		
		startActivity(i);

	}

	
	/**
	 * Background task to retrieve all Waves
	 */
	private class FetchWavesThread extends Thread {

		public FetchWavesThread(Handler h) {
			
		}

		public void run() {
			waveList = ow.search("in:inbox");
		
			Message msg = handler.obtainMessage();
			
			handler.sendMessage(msg);
		}
	}

	/**
	 * Adapter class to transform Wave bean representation in to Android structure
	 */
	public static class WaveAdapter extends BaseAdapter {
		private Context context;

		public WaveAdapter(Context c) {
			context = c;
		}

		public int getCount() {
			return waveList.size();
		}

		public Wave getItem(int position) {
			return waveList.get(position);
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
				view = inflater.inflate(R.layout.wavelist, null);
			} else {
				// Use convertView if it is available
				view = convertView;
			}
			Wave wave = (Wave) waveList.get(position);
			TextView t = (TextView) view.findViewById(R.id.titleWave);
			t.setText(wave.getTitle());
			return view;
		}
	}

}
