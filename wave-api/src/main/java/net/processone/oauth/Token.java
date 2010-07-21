package net.processone.oauth;

import java.io.Serializable;

/**
 * Most of the code based on http://code.google.com/p/jfireeagle/
 **/
public class Token implements Serializable {
	private static final long serialVersionUID = 204005630692236417L;
	private String publicKey;
	private String secret;

	public Token(){
		
	}
	
	public Token(String publicKey, String secret){
		this.publicKey = publicKey;
		this.secret = secret;
	}
	
	public String getPublicKey() {
		return publicKey;
	}

	public String getSecret() {
		return secret;
	}

	public void setPublicKey(String value) {
		this.publicKey = value;
	}

	public void setSecret(String value) {
		this.secret = value;
	}

	public boolean isValid() {
		if (this.getPublicKey() == null) {
			return false;
		} else if (this.getPublicKey().trim().length() == 0) {
			return false;
		} else if (this.getSecret() == null) {
			return false;
		} else if (this.getSecret().trim().length() == 0) {
			return false;
		} else {
			return true;
		}

	}

	public String toString() {
		return "public='" + this.getPublicKey() + "', secret='"
				+ this.getSecret() + "'";
	}

}
