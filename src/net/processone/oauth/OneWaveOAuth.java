package net.processone.oauth;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpMessage;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/

public class OneWaveOAuth {
	private OAuthClient authClient;

	private ClientSettings settings;

	public enum HTTPMethods {
		GET, POST, PUT, DELETE
	}

	public OneWaveOAuth() {
		this(new ClientSettings());
	}

	public OneWaveOAuth(ClientSettings cs) {
		this(new OAuthClient(new HttpClient4()), cs);

	}

	public OneWaveOAuth(final OAuthClient hClient) {
		this(hClient, new ClientSettings());
	}

	public OneWaveOAuth(final OAuthClient hClient, ClientSettings cs) {
		authClient = hClient;
		settings = cs;
	}

	public OAuthAccessor createOAuthAccessor() {

		OAuthServiceProvider authProvider = new OAuthServiceProvider(settings
				.getOAuthRequestTokenUrl(), settings.getOAuthAuthorizeUrl(),
				settings.getOAuthAccessTokenUrl());

		Token consumerToken = settings.getConsumerToken();

		String oauthCallBackUrl = settings.getOAuthCallbackUrl();

		OAuthConsumer consumer = new OAuthConsumer(oauthCallBackUrl,
				consumerToken.getPublicKey(), consumerToken.getSecret(),
				authProvider);

		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.requestToken = settings.getRequestToken().getPublicKey();
		accessor.accessToken = settings.getRequestToken().getPublicKey();
		accessor.tokenSecret = settings.getRequestToken().getSecret();

		return accessor;

	}

	public OAuthClient getOAuthClient() {
		return authClient;
	}

	public void fetchAccessToken() {

		OAuthAccessor accessor = createOAuthAccessor();

		try {

			OAuthMessage msg = authClient.getAccessToken(accessor, "GET", OAuth
					.newList("oauth_token", accessor.requestToken));

			settings.getRequestToken().setPublicKey(
					msg.getParameter("oauth_token"));
			settings.getRequestToken().setSecret(
					msg.getParameter("oauth_token_secret"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getUserAuthorizationUrl() {

		try {

			// Request token is not null, but access token is. In this step,
			// assume
			// that the request token has been authorized, and can be exchanged
			// with
			// access token. This is step 3 in the OAuth dance.

			OAuthAccessor accessor = createOAuthAccessor();

			accessor.requestToken = null;
			accessor.accessToken = null;
			accessor.tokenSecret = null;

			// send GET request to the Request Token URL
			authClient.getRequestToken(accessor);

			// store the Request Token values
			settings.getRequestToken().setPublicKey(accessor.requestToken);
			settings.getRequestToken().setSecret(accessor.tokenSecret);

			// build user authorization URL
			String url = settings.getOAuthAuthorizeUrl() + "?oauth_token="
					+ settings.getRequestToken().getPublicKey();

			if (settings.getOAuthCallbackUrl() != null) {
				url = url + "&oauth_callback=" + settings.getOAuthCallbackUrl()
						+ "&hd=default";
			}

			return url;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	protected String sendHttpRequest(String baseUrl, HTTPMethods method,
			java.util.Map<String, String> params, Token token) {

		OAuthAccessor access = createOAuthAccessor();

		access.accessToken = token.getPublicKey();
		access.tokenSecret = token.getSecret();

		if (params == null) {
			params = new HashMap<String, String>();
		}

		try {

			OAuthMessage responseMsg = authClient.invoke(access, method
					.toString(), baseUrl, params.entrySet());

			return responseMsg.readBodyAsString();

		} catch (Exception e) {

		}

		return null;

	}

	public OAuthMessage send(String url, String contentType, String body) {
		try {

			OAuthAccessor accessor = createOAuthAccessor();

			// Prepare the request.
			InputStream streamBody = new ByteArrayInputStream(body
					.getBytes("UTF-8"));
			OAuthMessage message = accessor.newRequestMessage("POST", url,
					null, streamBody);
			message.getHeaders().add(
					new Entry(HttpMessage.CONTENT_TYPE, contentType));

			// Make the HTTP call.
			OAuthMessage response = authClient.access(message,
					ParameterStyle.BODY);

			return response;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void setClientSettings(ClientSettings s) {
		this.settings = s;
	}

	public void shutdown() {
	}

	private static class Entry implements Map.Entry<String, String> {
		private String key;
		private String value;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String setValue(String value) {
			this.value = value;
			return this.value;
		}
	}
}