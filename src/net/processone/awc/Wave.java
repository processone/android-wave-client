package net.processone.awc;

public class Wave {
	private static final long serialVersionUID = -6553020195675162361L;
	private String waveId;
	private String title;

	public Wave() {

	}

	public Wave(String waveId, String title) {
		this.waveId = waveId;
		this.title = title;
	}

	public String getWaveId() {
		return waveId;
	}

	public void setWaveId(String waveId) {
		this.waveId = waveId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}

/*
 * {"id":"op1", "data":{ "searchResults":{ "digests":[
 * {"title":"Welcome to Google Wave", "waveId":"googlewave.com!w12345",
 * "snippet"
 * :"Hello foo! My name is Dr. Wave. I am going to show you how to use Google Wave!"
 * , "participants":["foo@googlewave.com","bar@googlewave.com"]},
 * {"title":"Hello world!", "waveId":"googlewave.com!w12346",
 * "snippet":"Have you seen Google Wave?",
 * "participants":["foo@googlewave.com","baz@googlewave.com"]}],
 * "query":"in:inbox", "numResults":2}}}
 */