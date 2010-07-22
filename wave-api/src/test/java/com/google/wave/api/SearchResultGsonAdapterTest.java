package com.google.wave.api;

import junit.framework.Assert;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.SearchResult.Digest;
import com.google.wave.api.impl.GsonFactory;

public class SearchResultGsonAdapterTest {

	@Test
	public void deserialize() {

		String input = "{\"id\":\"op1\", \"data\":{ \"searchResults\":{ \"digests\":[{\"title\":\"Welcome to Google Wave\", \"blipCount\":2, \"unreadCount\":1, \"lastModified\":1279316226457, \"waveId\":\"googlewave.com!w12345\", \"snippet\":\"Hello foo! My name is Dr. Wave. I am going to show you how to use Google Wave!\", \"participants\":[\"foo@googlewave.com\",\"bar@googlewave.com\"]},{\"title\":\"Hello world!\", \"waveId\":\"googlewave.com!w12346\", \"snippet\":\"Have you seen Google Wave?\", \"participants\":[\"foo@googlewave.com\",\"baz@googlewave.com\"]}], \"query\":\"in:inbox\", \"numResults\":2}}}";

		GsonFactory factory = new GsonFactory();
		factory.registerTypeAdapter(SearchResult.class,
				new SearchResultGsonAdaptor());
		factory.registerTypeAdapter(Digest.class, new DigestGsonAdaptor());
		Gson gson = factory.create("wave");

		JsonRpcResponse response = gson.fromJson(input, JsonRpcResponse.class);

		SearchResult result = (SearchResult) response.getData().get(
				ParamsProperty.SEARCH_RESULTS);

		Assert.assertEquals("in:inbox", result.getQuery());
		Assert.assertEquals(2, result.getNumResults());
		Digest digest = result.getDigests().get(0);
		Assert.assertEquals("Welcome to Google Wave", digest.getTitle());
		Assert.assertEquals("googlewave.com!w12345", digest.getWaveId());
		Assert
				.assertEquals(
						"Hello foo! My name is Dr. Wave. I am going to show you how to use Google Wave!",
						digest.getSnippet());
		Assert.assertNotNull(digest.getParticipants());
		Assert.assertEquals(2, digest.getParticipants().size());
		Assert.assertEquals("foo@googlewave.com", digest.getParticipants().get(
				0));
		Assert.assertEquals(2, digest.getBlipCount());
		Assert.assertEquals(1, digest.getUnreadCount());
		Assert.assertEquals(1279316226457L, digest.getLastModified());
	}
}
