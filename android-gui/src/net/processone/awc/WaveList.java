package net.processone.awc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.google.wave.api.SearchResult;
import com.google.wave.api.SearchResult.Digest;

public class WaveList extends ListActivity {

	private static final String TAG = WaveList.class.getSimpleName();

	private static final int PROGRESS_DIALOG = 0;

	/**
	 * Number of waves to retrieve in each batch. Value 20.
	 */
	private static final int BATCH_SIZE = 20;

	private OneWave ow;

	private ProgressDialog progressDialog;

	private FetchWavesThread progressThread;

	/**
	 * Define the Handler that receives messages from the thread and update the
	 * UI.
	 */
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ow = (OneWave) getApplication();

		showDialog(PROGRESS_DIALOG);

		this.registerForContextMenu(getListView()); // context menu, activated
		// by long pressing
		handler = new Handler() {
			public void handleMessage(Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				ArrayList<Digest> waveList = (ArrayList<Digest>) msg.obj;
				if (waveList.size() < BATCH_SIZE) {
					// It must be an ArrayList.
					setListAdapter(new WaveAdapter(WaveList.this, waveList));
				} else {
					// It must be an ArrayList.
					setListAdapter(new WaveAsyncAdapter(waveList));
				}
			}
		};
	}

	/**
	 * This is for creating context menu of the items in the wave list. remove
	 * form inbox, show, reply? etc.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wave_context_menu, menu);
		Log.i(TAG, menuInfo.getClass().toString());
	}

	/**
	 * User selects one option of the conext menu
	 */
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.itemFollowWave:
			return true;
		case R.id.itemRemoveFromInbox:
			return true;
		case R.id.itemMarkAsRead:
			return true;
		case R.id.itemView:
			onListItemClick(getListView(), info.targetView, info.position,
					info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
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
			progressThread = new FetchWavesThread();
			progressThread.start();
			return progressDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position,
			long id) {
		Digest wave = (Digest) listView.getItemAtPosition(position);
		String waveId = wave.getWaveId();

		Intent i = new Intent(this, WaveletList.class);
		i.putExtra("waveId", waveId);

		startActivity(i);
	}

	/**
	 * Background task to retrieve all Waves.
	 */
	private class FetchWavesThread extends Thread {

		public void run() {
			SearchResult r = ow.search("in:inbox", 0, BATCH_SIZE);
			// It must be an ArrayList, to be able to use it in the ArrayAdapter
			ArrayList<Digest> waveList = new ArrayList<Digest>(r.getDigests());

			Message msg = handler.obtainMessage();
			msg.obj = waveList;
			Log.i(TAG, "Reported num of results:" + r.getNumResults());
			Log.i(TAG, "Retrieved results:" + r.getDigests().size());

			handler.sendMessage(msg);
		}
	}

	/**
	 * Adapter class to transform Wave bean representation in to Android
	 * structure.
	 */
	public static class WaveAdapter extends ArrayAdapter<Digest> {

		public WaveAdapter(Context c, ArrayList<Digest> list) {
			// Second parameter won't be used, we redefine getView.
			super(c, 0, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				// Make up a new view
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.wavelist, null);
			}

			Digest wave = getItem(position);

			// wave.getLastModified();
			// wave.getSnippet();
			// <- may be a good idea is to put that info, that we already had
			// available to the user in some way, altough no idea how, we
			// don't have much space.

			TextView title = (TextView) convertView
					.findViewById(R.id.titleWave);
			title.setText(wave.getTitle());

			TextView unread = (TextView) convertView.findViewById(R.id.unread);

			int unreadCount = wave.getUnreadCount();

			unread.setVisibility(unreadCount == 0 ? View.INVISIBLE
					: View.VISIBLE);

			unread.setText(String.valueOf(unreadCount));

			TextView participants = (TextView) convertView
					.findViewById(R.id.participants);
			StringBuilder b = new StringBuilder();
			Iterator<String> it = wave.getParticipants().iterator();
			while (it.hasNext()) { // keep only the username
				String contact = it.next();
				String[] splited = contact.split("@");
				b.append(splited[0]);
				if (it.hasNext())
					b.append(", ");
			}

			participants.setText(b.toString());

			return convertView;
		}
	}

	class WaveAsyncAdapter extends EndlessAdapter {
		private RotateAnimation rotate = null;

		private List<Digest> waveCache;

		public WaveAsyncAdapter(ArrayList<Digest> list) {
			super(new WaveAdapter(WaveList.this, list));
			rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);
		}

		@Override
		protected void appendCachedData() {
			ArrayAdapter<Digest> a = (ArrayAdapter<Digest>) getWrappedAdapter();
			for (Digest item : waveCache) {
				a.add(item);
			}
			waveCache = null;
			Log.i(TAG, "New size: " + a.getCount());
		}

		@Override
		/*
		 * Note that this is executed in a separate thread, we don't need to
		 * spawn it ourselves
		 */
		protected boolean cacheInBackground() {
			int newIndex = this.getWrappedAdapter().getCount();
			Log.i(TAG, "New Index to query for: " + newIndex);

			SearchResult r = ow.search("in:inbox", newIndex, BATCH_SIZE);
			waveCache = r.getDigests();
			Log.i(TAG, "Reported num of results:" + r.getNumResults());
			Log.i(TAG, "Retrieved results:" + r.getDigests().size());
			// If there were less returned, then no more messages exists.
			return (r.getNumResults() == BATCH_SIZE);
		}

		@Override
		protected View getPendingView(ViewGroup parent) {
			View row = getLayoutInflater().inflate(R.layout.wavelist, null);
			// View child=row.findViewById(R.id.titleWave);
			View child = row.findViewById(R.id.waveItemRow);
			child.setVisibility(View.GONE);
			child = row.findViewById(R.id.throbber);
			child.setVisibility(View.VISIBLE);
			child.startAnimation(rotate);
			return (row);
		}

		@Override
		protected void rebindPendingView(int position, View row) {
			View child = row.findViewById(R.id.titleWave);
			child.setVisibility(View.VISIBLE);
			// MMM... this is probably wrong.. but didn't manage to test it.
			// Probably it must be
			// analog to what WaveAdapter do with his view
			((TextView) child).setText(getWrappedAdapter().getItem(position)
					.toString());
			child = row.findViewById(R.id.throbber);
			child.setVisibility(View.GONE);
			child.clearAnimation();
		}
	}
}
