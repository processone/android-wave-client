package net.processone.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/
public class WaveConstants {
	
	public static final String WEB_SERVICE_BASE_URL = "http://wave.googleusercontent.com/api/rpc";
	public static final String RPC_HANDLER = "https://www-opensocial.googleusercontent.com/api/rpc";
	public static final String OAUTH_REQUEST_TOKEN_URL;
	public static final String OAUTH_AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken";
	public static final String OAUTH_ACCESS_TOKEN_URL = "https://www.google.com/accounts/OAuthGetAccessToken";

	static {
		try {
			OAUTH_REQUEST_TOKEN_URL = "https://www.google.com/accounts/OAuthGetRequestToken?scope="
					+ URLEncoder.encode(WEB_SERVICE_BASE_URL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}
}
