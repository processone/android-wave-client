package net.processone.oauth;


/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/
public class ClientSettings {
	private String webServiceUrl = WaveConstants.WEB_SERVICE_BASE_URL;

	private String oAuthRequestTokenUrl = WaveConstants.OAUTH_REQUEST_TOKEN_URL;

	private String oAuthAuthorizeUrl = WaveConstants.OAUTH_AUTHORIZE_URL;

	private String oAuthAccessTokenUrl = WaveConstants.OAUTH_ACCESS_TOKEN_URL;

	private String rpcUrl = WaveConstants.RPC_HANDLER;
	
	private String oAuthCallbackUrl = "onewavetest://auth";
	
	private Token consumerToken = new Token("anonymous", "anonymous");

	private Token requestToken;

	public ClientSettings() {
		requestToken = new Token();
	}

	public Token getConsumerToken() {
		return consumerToken;
	}

	public void setConsumerToken(Token t) {
		this.consumerToken = t;
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

	public Token getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(Token requestToken) {
		this.requestToken = requestToken;
	}

	public void setRpcHandler(String rpcUrl) {
		this.rpcUrl = rpcUrl;
	}

	public String getRpcUrl() {
		return rpcUrl;
	}

}
