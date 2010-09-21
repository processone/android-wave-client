package net.processone.awc;

import java.util.LinkedList;
import java.util.List;

import org.waveprotocol.wave.model.id.WaveId;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

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

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wavelet_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemAddParticipant:

			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private class FetchWaveletThread extends Thread {

		public FetchWaveletThread() {

		}

		public void run() {
			wavelet = ow.fetchWavelet(waveId);

			if (wavelet == null)
				return;

			List<Blip> sorted = new LinkedList<Blip>();

			dfs(wavelet.getRootBlip(), sorted);

			arrayBlip = sorted.toArray(new Blip[sorted.size()]);

			Message msg = handler.obtainMessage();

			handler.sendMessage(msg);
		}

		private void dfs(Blip blip, List<Blip> sorted) {

			sorted.add(blip);

			List<Blip> children = blip.getChildBlips();
			int totalChildren = children.size();

			if (totalChildren == 0)
				return;

			if (totalChildren == 1)
				dfs(children.get(0), sorted);

			for (int i = 1; i < totalChildren; i++)
				dfs(children.get(i), sorted);
			dfs(children.get(0), sorted);
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
