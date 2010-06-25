package oauh.test;

import java.net.URLEncoder;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/
public class WaveConstants {
	public static final String API_VERSION = "1.0";
	public static final String WEB_SERVICE_BASE_URL = "http://wave.googleusercontent.com/api/rpc";
	public static final String WEB_SITE_URL = "http://wave.google.com/";
	public static final String OAUTH_REQUEST_TOKEN_URL = "https://www.google.com/accounts/OAuthGetRequestToken?scope="
			+ URLEncoder.encode(WEB_SERVICE_BASE_URL);
	public static final String OAUTH_AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken";
	public static final String OAUTH_ACCESS_TOKEN_URL = "https://www.google.com/accounts/OAuthGetAccessToken";

}
