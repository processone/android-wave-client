package oauh.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.oauth.OAuthConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

public class CallbackOAuth extends Activity {
	private static final String TAG = CallbackOAuth.class.getSimpleName();

	@Override
	public void onResume() {
		super.onResume();
		// extract the OAUTH access token if it exists
		Uri uri = this.getIntent().getData();
		if (uri != null) {
			
			String access_token = uri.getQueryParameter("oauth_token");
			String access_token_secret = uri.getQueryParameter("oauth_token_secret");
			
			System.out.println(access_token);
			OneWave ow = (OneWave) getApplication();

			java.util.Map<String, String> params = new HashMap<String, String>();
			// String m = ow.oAuth.sendHttpRequest(
			// "https://www-opensocial.googleusercontent.com/api/rpc",
			// "GET", params, ow.oAuth.getGeneralAccessToken());

			//			
			// HttpMessage mes = new HttpMessage("POST", new
			// URL("https://www-opensocial.googleusercontent.com/api/rpc"));
			// ow.oAuth.getOAuthClient().getHttpClient().execute(mes, );

//			try {
//				HttpPost post = new HttpPost(
//						"https://www-opensocial.googleusercontent.com/api/rpc");
//				post
//						.setEntity(new StringEntity(
//								"{'id':'op1','method':'wave.robot.search','params':{'query':'in:inbox'}}"));
//				post.setHeader(new BasicHeader("Content-Type",
//						"application/json"));
//
//				HttpClient client = new DefaultHttpClient();
//				
//				ow.oAuth.getOAuthClient().ge;
//				
//				OAuthConsumer consumer;
//
//				consumer.consumerKey = ow.oAuth.getRequestToken().getPublicKey();
//				consumer.consumerSecret = ow.oAuth.getRequestToken().getSecret();
//				
//				consumer.sign(post);
//				
				
				
				
				
//				private void sign(HttpPost post) throws OAuthMessageSignerException,
//				OAuthExpectationFailedException, OAuthCommunicationException {
//			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
//					OneWave.Constants.CONSUMER_KEY,
//					OneWave.Constants.CONSUMER_SECRET);
	//
//			consumer.setTokenWithSecret(((OneWave) getApplication()).token,
//					((OneWave) getApplication()).token_secret);
//			consumer.sign(post);
//		}

				
				
				
//				
//				
//				HttpResponse response = client.execute(post);
//
//				JSONObject resp = new JSONObject(streamToString(response
//						.getEntity().getContent()));
//
//				Log.d(TAG, "Inbox: " + resp);
//				response.getEntity().consumeContent();
//
//				List<Map<String, Object>> s = new ArrayList<Map<String, Object>>();
//				JSONArray array = resp.getJSONObject("data").getJSONObject(
//						"searchResults").getJSONArray("digests");
//
//				for (int i = 0; i < array.length(); i++) {
//					JSONObject wavelet = array.getJSONObject(i);
//					Map<String, Object> m = new TreeMap<String, Object>();
//					m.put("title", wavelet.getString("title"));
//					m.put("waveId", wavelet.getString("waveId"));
//					s.add(m);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

			// m.

			// HttpPost post = new HttpPost(
			// "https://www-opensocial.googleusercontent.com/api/rpc");
			// post
			// .setEntity(new StringEntity(
			// "{'id':'op1','method':'wave.robot.search','params':{'query':'in:inbox'}}"));
			// post.setHeader(new BasicHeader("Content-Type",
			// "application/json"));
			//
			// // sign(post);
			//
			// // ProgressDialog dialog = ProgressDialog.show(this, "",
			// // "Loading. Please wait...", true);
			// // dialog.show();
			//
			// HttpClient client = new DefaultHttpClient();
			// HttpResponse response = client.execute(post);
			//
			// JSONObject resp = new JSONObject(streamToString(response
			// .getEntity().getContent()));

			// if (m == null)
			// return;
			//
			// try {
			// JSONObject resp = new JSONObject(m);
			// Log.d(TAG, "Inbox: " + resp);
			//
			// List<Map<String, Object>> s = new ArrayList<Map<String,
			// Object>>();
			// JSONArray array = resp.getJSONObject("data").getJSONObject(
			// "searchResults").getJSONArray("digests");
			//
			// for (int i = 0; i < array.length(); i++) {
			// JSONObject wavelet = array.getJSONObject(i);
			// Map<String, Object> map = new TreeMap<String, Object>();
			// map.put("title", wavelet.getString("title"));
			// map.put("waveId", wavelet.getString("waveId"));
			// s.add(map);
			// }
			// } catch (JSONException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

		}
	}



	private String streamToString(InputStream stream) throws IOException {

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = reader.readLine()) != null)
			sb.append(line).append("\n");

		return sb.toString();
	}

	// String token = data.getStringExtra("oauth_token");
	// System.out.println("Token " + token);

	// String access_token = uri.getQueryParameter("oauth_token");
	// System.out.println(access_token);
	//

}
