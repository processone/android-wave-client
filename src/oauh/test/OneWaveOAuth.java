package oauh.test;

import java.util.HashMap;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/

public class OneWaveOAuth {
	private HttpClient httpClient;
	private OAuthClient authClient;
	private OAuthServiceProvider authProvider;
	private ClientSettings settings;
	private Token requestToken;

	public enum HTTPMethods {
		GET, POST, PUT, DELETE
	}
	
	public OneWaveOAuth() {
		this(new DefaultHttpClient(), new ClientSettings());
	}

	public OneWaveOAuth(ClientSettings cs) {
		this(new DefaultHttpClient(), cs);

	}

	public OneWaveOAuth(final AbstractHttpClient hClient) {
		this(hClient, new ClientSettings());
	}

	public OneWaveOAuth(final AbstractHttpClient hClient, ClientSettings cs) {
		httpClient = hClient;
		settings = cs;
	}

	protected OAuthAccessor createOAuthAccessor() {

		Token consumerToken = settings.getConsumerToken();

		String oauthCallBackUrl = settings.getOAuthCallbackUrl();

		authProvider = new OAuthServiceProvider(settings
				.getOAuthRequestTokenUrl(), settings.getOAuthAuthorizeUrl(),
				settings.getOAuthAccessTokenUrl());

		OAuthConsumer consumer = new OAuthConsumer(oauthCallBackUrl,
				consumerToken.getPublicKey(), consumerToken.getSecret(),
				authProvider);

		// if (this.compressionEnabled) {
		// c.setProperty(OAuthClient.ACCEPT_ENCODING,
		// HttpMessageDecoder.ACCEPTED);
		// }

		OAuthAccessor accesor = new OAuthAccessor(consumer);

		return accesor;

	}

	protected OAuthClient getOAuthClient() {

		if (authClient == null) {
			// Çthis.pool
			authClient = new OAuthClient(new HttpClient4());
		}

		return authClient;
	}

	protected Token getGeneralAccessToken() {
		return settings.getGeneralToken();
	}

	// protected/* Response */void sendFireEagleRequest(String method,
	// String service, java.util.Map<String, String> params, Token token) {
	//
	// checkUserAccessToken();
	//
	// String xml = sendHttpRequest(settings.getWebServiceUrl() + "/"
	// + service, method, params, token);
	//
	// /*
	// * Response r; // = fromXml(xml);
	// *
	// * return r;
	// */
	//
	// }

	protected void checkUserAccessToken() {
		if (hasValidUserAccessToken() == false) {

			if (requestToken.isValid()) {
				fetchAccessToken();
			} else {
				// String userAuthorizationUrl = getUserAuthorizationUrl();

				// throw new UserAuthorizationRequiredException(
				// userAuthorizationUrl);
			}
		}
	}

	public void fetchAccessToken() {

		// System.out.println("fetchAccessToken: requestToken=" +
		// this.getRequestToken());

		OAuthClient client = getOAuthClient();

		OAuthMessage responseMsg = null;

		OAuthAccessor access = this.createOAuthAccessor();

		try {

			// request the Access token

			access.accessToken = requestToken.getPublicKey();
			access.requestToken = requestToken.getPublicKey();
			access.tokenSecret = requestToken.getSecret();

			responseMsg = client.invoke(access, "GET", settings
					.getOAuthAccessTokenUrl(), null);

			Token accessToken = settings.getUserSpecificToken();
			accessToken.setPublicKey(responseMsg.getParameter("oauth_token"));
			accessToken.setSecret(responseMsg
					.getParameter("oauth_token_secret"));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getUserAuthorizationUrl() {

		try {


			OAuthAccessor access = createOAuthAccessor();

			access.requestToken = null;
			access.accessToken = null;
			access.tokenSecret = null;

			// send GET request to the Request Token URL
			OAuthClient client = getOAuthClient();
			client.getRequestToken(access);

			// store the Request Token values
			requestToken = new Token();
			requestToken.setPublicKey(access.requestToken);
			requestToken.setSecret(access.tokenSecret);

			// build user authorization URL
			String url = settings.getOAuthAuthorizeUrl() + "?oauth_token="
					+ requestToken.getPublicKey();

			if (settings.getOAuthCallbackUrl() != null) {
				url = url + "&oauth_callback=" + settings.getOAuthCallbackUrl();
			}

			return url;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	protected String sendHttpRequest(String baseUrl, HTTPMethods method,
			java.util.Map<String, String> params, Token token) {



		OAuthAccessor access = this.createOAuthAccessor();

		access.accessToken = token.getPublicKey();
		access.tokenSecret = token.getSecret();

		if (params == null) {
			params = new HashMap<String, String>();
		}

		OAuthClient client = getOAuthClient();
		OAuthMessage responseMsg = null;
		try {

			responseMsg = client.invoke(access, method.toString(), baseUrl, params
					.entrySet());

			return responseMsg.readBodyAsString();

		} catch (Exception e) {

		}

		return null;

	}

	public void setClientSettings(ClientSettings s) {
		this.settings = s;
	}

	protected boolean hasValidUserAccessToken() {
		return settings.getUserSpecificToken().isValid();
	}

	protected boolean generalAccessTokenIsValid() {
		return settings.getGeneralToken().isValid();
	}

	public void shutdown() {
		try {
			httpClient.getConnectionManager().shutdown();
		} catch (Exception ignore) {
			// ignored
		}

	}

	public void setUserAgent(String ua) {
		httpClient.getParams().setParameter(AllClientPNames.USER_AGENT, ua);
	}

	public void setConnectionTimeout(int milliseconds) {
		httpClient.getParams().setIntParameter(
				AllClientPNames.CONNECTION_TIMEOUT, milliseconds);
	}

	public void setSocketTimeout(int milliseconds) {
		httpClient.getParams().setIntParameter(AllClientPNames.SO_TIMEOUT,
				milliseconds);
	}

	// public void setCompressionEnabled(boolean b) {
	// this.compressionEnabled = b;
	// }
	//
	// public boolean getCompressionEnabled() {
	// return this.compressionEnabled;
	// }

	public Token getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(Token token) {
		requestToken = token;
	}

//	 client.access(OAuthMessage.POST, serviceURL //
//             , OAuth.newList("method", "anymeta.predicates.set" //
//                     , "id", objectId //
//                     , "field", "text.body" //
//                     , "value", "edited " + new Date() + " via Java desktop application" //
//             ));

}
//public class RequestInspector {
//
//	  public static String getJsonInfo(OAuthSigner signer, Request request, Token accessToken) {
//	    StringBuilder info = new StringBuilder();
//	    // Main
//	    info.append("{");
//	    String toSign = signer.getStringToSign(request, CallType.RESOURCE);
//	    addField("stringToSign", toSign, info);
//	    addField("signature", signer.getSignature(toSign, accessToken.getSecret()), info);
//
//	    // HttpRequest
//	    info.append("\"httpRequest\":{");
//	    addField("verb", request.getVerb().name(), info);
//	    addField("url", request.getUrl(), info);
//	    addField("body", request.getBodyContents(), info);
//
//	    // HttpRequestHeaders
//	    info.append("\"headers\":[");
//	    for (Map.Entry<String, String> entry : request.getHeaders().entrySet())
//	      addObject(entry.getKey(), entry.getValue().replace("\"", "'"), info);
//
//	    info.deleteCharAt(info.length() - 2); // WTF: Remove trailing ','
//	    info.append("]");
//	    info.append("}");
//	    info.append("}");
//	    return info.toString();
//	  }
//
//	  private static final void addField(String key, String val, StringBuilder info) {
//	    info.append("\"" + key + "\":\"" + val + "\"");
//	    info.append(",");
//	  }
//
//	  private static final void addObject(String key, String val, StringBuilder info) {
//	    info.append("{");
//	    addField(key, val, info);
//	    info.append("}");
//	  }
//	}

