package net.processone.awc;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.commonsware.cwac.endless.EndlessAdapter;
import com.google.wave.api.SearchResult;
import com.google.wave.api.SearchResult.Digest;

public class WaveList extends ListActivity {
	
	static final String TAG = WaveList.class.getSimpleName();

	static final int PROGRESS_DIALOG = 0;

	private OneWave ow;


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
		
		handler = new Handler() {
			public void handleMessage(Message msg) {
				dismissDialog(PROGRESS_DIALOG);
				ArrayList<Digest> waveList = (ArrayList<Digest>)msg.obj; //(ArrayList<Digest>)msg.peekData().get(WAVE_LIST_KEY);
				setListAdapter(new WaveAsyncAdapter(waveList)); //it must be an ArrayList.
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
		Digest wave = (Digest) listView.getItemAtPosition(position);
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
			SearchResult r = ow.search("in:inbox", 0, 20);
			ArrayList<Digest> waveList = new ArrayList<Digest>(r.getDigests()); //it must be an ArrayList, to be able to use it in the ArrayAdapter
		
			Message msg = handler.obtainMessage();
			msg.obj = waveList; //TODO:  it has a bundle object..
			Log.i(TAG, "Reported num of results:" + r.getNumResults());
			Log.i(TAG, "Retrieved results:" + r.getDigests().size());
			
			handler.sendMessage(msg);
		}
	}

	/**
	 * Adapter class to transform Wave bean representation in to Android structure
	 * 
	 * 
	 */
	public static class WaveAdapter extends ArrayAdapter<Digest> {

		public WaveAdapter(Context c, ArrayList<Digest> list) {
			super(c,0, list); //second parameter won't be used, we redefine getView
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				// Make up a new view
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.wavelist, null);
			} else {
				// Use convertView if it is available
				view = convertView;
			}
			
			Digest wave = getItem(position);

			
			// wave.getParticipants();
			// wave.getLastModified();
			// wave.getSnippet();
			
			
			TextView t = (TextView) view.findViewById(R.id.titleWave);
			t.setText(wave.getTitle());
			return view;
		}
	}
	
	
	
	
	class WaveAsyncAdapter extends EndlessAdapter {
		private RotateAnimation rotate=null;
		
		private List<Digest> waveCache;
		
		public WaveAsyncAdapter(ArrayList<Digest> list) {
			super(new WaveAdapter(WaveList.this, list));
			rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,	0.5f);
			rotate.setDuration(600);
			rotate.setRepeatMode(Animation.RESTART);
			rotate.setRepeatCount(Animation.INFINITE);
		}

		@Override
		protected void appendCachedData() {
			ArrayAdapter<Digest> a=(ArrayAdapter<Digest>)getWrappedAdapter();
			for (Digest item : waveCache) {
				a.add(item);
			}
			waveCache = null;
			Log.i(TAG, "New size: " + a.getCount());
		}

		@Override
		/**
		 * Note that this is executed in a separate thread, we don't need to spawn it ourselves
		 */
		protected boolean cacheInBackground() {
			int newIndex = this.getWrappedAdapter().getCount();
			Log.i(TAG, "New Index to query for: " + newIndex);
		
			SearchResult r = ow.search("in:inbox", newIndex, 20);
			waveCache = r.getDigests();
			Log.i(TAG, "Reported num of results:" + r.getNumResults());
			Log.i(TAG, "Retrieved results:" + r.getDigests().size());
			return(r.getNumResults() == 20);  // if there were less returned, then no more messages exists.
		}

		@Override
		protected View getPendingView(ViewGroup parent) {
			View row=getLayoutInflater().inflate(R.layout.wavelist, null);
			View child=row.findViewById(R.id.titleWave);
			child.setVisibility(View.GONE);
			child=row.findViewById(R.id.throbber);
			child.setVisibility(View.VISIBLE);
			child.startAnimation(rotate);
			return(row);
		}

		@Override
		protected void rebindPendingView(int position, View row) {
			View child=row.findViewById(R.id.titleWave);
			child.setVisibility(View.VISIBLE);
			((TextView)child).setText(getWrappedAdapter().getItem(position).toString());
			child=row.findViewById(R.id.throbber);
			child.setVisibility(View.GONE);
			child.clearAnimation();
			
		}
		
	}

}
