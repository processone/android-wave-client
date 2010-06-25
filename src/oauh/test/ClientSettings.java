package oauh.test;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/
public class ClientSettings {
	private Token userSpecificToken;
	private Token generalToken;
	private Token consumerToken;
	private String webServiceUrl = WaveConstants.WEB_SERVICE_BASE_URL;
	private String oAuthRequestTokenUrl = WaveConstants.OAUTH_REQUEST_TOKEN_URL;
	private String oAuthAuthorizeUrl = WaveConstants.OAUTH_AUTHORIZE_URL;
	private String oAuthAccessTokenUrl = WaveConstants.OAUTH_ACCESS_TOKEN_URL;
	private String oAuthCallbackUrl = null;

	public Token getConsumerToken() {
		return consumerToken;
	}

	public void setConsumerToken(Token t) {
		this.consumerToken = t;
	}

	public Token getUserSpecificToken() {
		if (userSpecificToken == null) {
			userSpecificToken = new Token();
		}
		return userSpecificToken;
	}

	public void setUserSpecificToken(Token t) {
		this.userSpecificToken = t;
	}

	public Token getGeneralToken() {
		if (generalToken == null) {
			generalToken = new Token();
		}

		return generalToken;
	}

	public void setGeneralToken(Token t) {
		this.generalToken = t;
	}

	public String getWebServiceUrl() {
		return webServiceUrl;
	}

	public void setWebServiceUrl(String webServiceUrl) {
		this.webServiceUrl = webServiceUrl;
	}

	public String getOAuthRequestTokenUrl() {
		return oAuthRequestTokenUrl;
	}

	public void setOAuthRequestTokenUrl(String authRequestTokenUrl) {
		oAuthRequestTokenUrl = authRequestTokenUrl;
	}

	public String getOAuthAuthorizeUrl() {
		return oAuthAuthorizeUrl;
	}

	public void setOAuthAuthorizeUrl(String authAuthorizeUrl) {
		oAuthAuthorizeUrl = authAuthorizeUrl;
	}

	public String getOAuthAccessTokenUrl() {
		return oAuthAccessTokenUrl;
	}

	public void setOAuthAccessTokenUrl(String authAccessTokenUrl) {
		oAuthAccessTokenUrl = authAccessTokenUrl;
	}

	public String getOAuthCallbackUrl() {
		return oAuthCallbackUrl;
	}

	public void setOAuthCallbackUrl(String u) {
		this.oAuthCallbackUrl = u;
	}

}
