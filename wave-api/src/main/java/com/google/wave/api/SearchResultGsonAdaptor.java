package com.google.wave.api;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.wave.api.SearchResult.Digest;

/**
 * Gson adaptor to serialize and deserialize {@link SearchResult}.
 */
public class SearchResultGsonAdaptor implements InstanceCreator<SearchResult>,
		JsonSerializer<SearchResult>, JsonDeserializer<SearchResult> {

	public static final String QUERY_TAG = "query";
	public static final String NUM_RESULTS_TAG = "numResults";
	public static final String DIGESTS_TAG = "digests";

	public SearchResult createInstance(Type type) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public JsonElement serialize(SearchResult src, Type typeOfSrc,
			JsonSerializationContext context) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public SearchResult deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObj = json.getAsJsonObject();
		
		String query = jsonObj.get(QUERY_TAG).getAsString();
		// Unused.
		//int numResults = jsonObj.get(NUM_RESULTS_TAG).getAsInt();
		
		SearchResult result = new SearchResult(query);
		
		JsonArray digestsArray = jsonObj.get(DIGESTS_TAG).getAsJsonArray();
		
		for(JsonElement e : digestsArray) {
			Digest digest = context.deserialize(e, Digest.class);
			result.addDigest(digest);
		}
		
		return result;
	}
}
