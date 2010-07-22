package com.google.wave.api;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

public class DigestGsonAdaptor implements InstanceCreator<Digest>,
		JsonSerializer<Digest>, JsonDeserializer<Digest> {
	
	public static final String TITLE_TAG = "title";
	public static final String SNIPPET_TAG = "snippet";
	public static final String WAVE_ID_TAG = "waveId";
	public static final String PARTICIPANTS_TAG = "participants";
	public static final String LAST_MODIFIED_TAG = "lastModified";
	public static final String UNREAD_COUNT_TAG = "unreadCount";
	public static final String BLIP_COUNT_TAG = "blipCount";

	public Digest createInstance(Type type) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public JsonElement serialize(Digest src, Type typeOfSrc,
			JsonSerializationContext context) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Digest deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		
		String title = null;
		String snippet = null;
		String waveId = null;
		long lastModified = 0L;
		int unreadCount = 0;
		int blipCount = 0;
		List<String> participants = new ArrayList<String>();

		JsonObject jsonObject = json.getAsJsonObject();
		if (jsonObject.has(TITLE_TAG))
			title = jsonObject.get(TITLE_TAG).getAsString();
		
		if (jsonObject.has(SNIPPET_TAG))
			snippet = jsonObject.get(SNIPPET_TAG).getAsString();
		
		if (jsonObject.has(WAVE_ID_TAG))
			waveId = jsonObject.get(WAVE_ID_TAG).getAsString();
		
		if (jsonObject.has(PARTICIPANTS_TAG)) {
			JsonArray participantsArray = jsonObject.get(PARTICIPANTS_TAG).getAsJsonArray();
			for (JsonElement e : participantsArray) {
				participants.add(e.getAsString());
			}
		}
		
		if (jsonObject.has(LAST_MODIFIED_TAG))
			lastModified = jsonObject.get(LAST_MODIFIED_TAG).getAsLong();
		
		if (jsonObject.has(BLIP_COUNT_TAG))
			blipCount = jsonObject.get(BLIP_COUNT_TAG).getAsInt();
		
		if (jsonObject.has(UNREAD_COUNT_TAG))
			unreadCount = jsonObject.get(UNREAD_COUNT_TAG).getAsInt();

		return new Digest(title, snippet, waveId, participants, lastModified, unreadCount, blipCount);
	}
}
