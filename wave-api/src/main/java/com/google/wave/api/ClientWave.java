package com.google.wave.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.OperationRequest.Parameter;
import com.google.wave.api.SearchResult.Digest;
import com.google.wave.api.impl.GsonFactory;
import com.google.wave.api.impl.WaveletData;

public class ClientWave {

	// Constants.
	private static final String ACTIVE_API_OPERATION_NAMESPACE = "wave";
	private static final Gson SERIALIZER_FOR_ACTIVE_API;

	// Static constructor.
	// Required to extends Gson serialization.
	static {
		GsonFactory factory = new GsonFactory();
		factory.registerTypeAdapter(SearchResult.class,
				new SearchResultGsonAdaptor());
		factory.registerTypeAdapter(Digest.class, new DigestGsonAdaptor());
		SERIALIZER_FOR_ACTIVE_API = factory
				.create(ACTIVE_API_OPERATION_NAMESPACE);
	}

	/**
	 * Logger for this class.
	 */
	private static final Logger LOG = Logger.getLogger(ClientWave.class
			.getName());

	/**
	 * Callback that provides JSON requests.
	 */
	public static interface JSONRpcHandler {

		public String request(String jsonBody) throws Exception;

	}

	/**
	 * Instance for the JSON handler.
	 */
	private final JSONRpcHandler jsonHandler;

	public ClientWave(JSONRpcHandler jsonHandler) {
		this.jsonHandler = jsonHandler;
	}

	/**
	 * Submits the pending operations associated with this {@link Wavelet}.
	 * 
	 * @param wavelet
	 *            the bundle that contains the operations to be submitted.
	 * @return a list of {@link JsonRpcResponse} that represents the responses
	 *         from the server for all operations that were submitted.
	 * 
	 * @throws ClientWaveException
	 *             if there is a problem submitting the operations.
	 */
	public List<JsonRpcResponse> submit(Wavelet wavelet)
			throws ClientWaveException {
		OperationQueue opQueue = wavelet.getOperationQueue();
		List<JsonRpcResponse> responses = makeRpc(opQueue);
		wavelet.getOperationQueue().clear();
		return responses;
	}

	/**
	 * Returns an empty/blind stub of a wavelet with the given wave id and
	 * wavelet id.
	 * 
	 * Call this method if you would like to apply wavelet-only operations
	 * without fetching the wave first.
	 * 
	 * The returned wavelet has its own {@link OperationQueue}. It is the
	 * responsibility of the caller to make sure this wavelet gets submitted to
	 * the server, either by calling AbstractRobot#submit(Wavelet, String) or by
	 * calling {@link Wavelet#submitWith(Wavelet)} on the new wavelet, to join
	 * its queue with another wavelet, for example, the event wavelet.
	 * 
	 * @param waveId
	 *            the id of the wave.
	 * @param waveletId
	 *            the id of the wavelet.
	 * @return a stub of a wavelet.
	 */
	public Wavelet blindWavelet(WaveId waveId, WaveletId waveletId)
			throws ClientWaveException {
		return blindWavelet(waveId, waveletId, null);
	}

	/**
	 * Returns an empty/blind stub of a wavelet with the given wave id and
	 * wavelet id.
	 * 
	 * Call this method if you would like to apply wavelet-only operations
	 * without fetching the wave first.
	 * 
	 * The returned wavelet has its own {@link OperationQueue}. It is the
	 * responsibility of the caller to make sure this wavelet gets submitted to
	 * the server, either by calling AbstractRobot#submit(Wavelet, String) or by
	 * calling {@link Wavelet#submitWith(Wavelet)} on the new wavelet, to join
	 * its queue with another wavelet, for example, the event wavelet.
	 * 
	 * @param waveId
	 *            the id of the wave.
	 * @param waveletId
	 *            the id of the wavelet.
	 * @param proxyForId
	 *            the proxying information that should be set on the operation
	 *            queue. Please note that this parameter should be properly
	 *            encoded to ensure that the resulting participant id is valid
	 *            (see {@link Util#checkIsValidProxyForId(String)} for more
	 *            details).
	 * @return a stub of a wavelet.
	 */
	public Wavelet blindWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId) throws ClientWaveException {
		return blindWavelet(waveId, waveletId, proxyForId,
				new HashMap<String, Blip>());
	}

	/**
	 * Returns an empty/blind stub of a wavelet with the given wave id and
	 * wavelet id.
	 * 
	 * Call this method if you would like to apply wavelet-only operations
	 * without fetching the wave first.
	 * 
	 * The returned wavelet has its own {@link OperationQueue}. It is the
	 * responsibility of the caller to make sure this wavelet gets submitted to
	 * the server, either by calling AbstractRobot#submit(Wavelet, String) or by
	 * calling {@link Wavelet#submitWith(Wavelet)} on the new wavelet, to join
	 * its queue with another wavelet, for example, the event wavelet.
	 * 
	 * @param waveId
	 *            , String rpcServerUrl the id of the wave.
	 * @param waveletId
	 *            the id of the wavelet.
	 * @param proxyForId
	 *            the proxying information that should be set on the operation
	 *            queue. Please note that this parameter should be properly
	 *            encoded to ensure that the resulting participant id is valid
	 *            (see {@link Util#checkIsValidProxyForId(String)} for more
	 *            details).
	 * @param blips
	 *            a collection of blips that belong to this wavelet.
	 * @return a stub of asend wavelet.
	 */
	public Wavelet blindWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId, Map<String, Blip> blips)
			throws ClientWaveException {
		Util.checkIsValidProxyForId(proxyForId);
		Map<String, String> roles = new HashMap<String, String>();
		return new Wavelet(waveId, waveletId, null, Collections
				.<String> emptySet(), roles, blips, new OperationQueue(
				proxyForId));
	}

	/**
	 * Creates a new wave with a list of participants on it.
	 * 
	 * The root wavelet of the new wave is returned with its own
	 * {@link OperationQueue}. It is the responsibility of the caller to make
	 * sure this wavelet gets submitted to the server, either by calling
	 * AbstractRobot#submit(Wavelet, String) or by calling
	 * {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param domain
	 *            the domain to create the wavelet on. In general, this should
	 *            correspond to the domain of the incoming event wavelet, except
	 *            when the robot is calling this method outside of an event or
	 *            when the server is handling multiple domains.
	 * @param participants
	 *            the initial participants on the wave. The robot, as the
	 *            creator of the wave, will be added by default. The order of
	 *            the participants will be preserved.
	 */
	public Wavelet newWave(String domain, Set<String> participants)
			throws ClientWaveException {
		return newWave(domain, participants, null);
	}

	/**
	 * Creates a new wave with a list of participants on it.
	 * 
	 * The root wavelet of the new wave is returned with its own
	 * {@link OperationQueue}. It is the responsibility of the caller to make
	 * sure this wavelet gets submitted to the server, either by calling
	 * AbstractRobot#submit(Wavelet, String) or by calling
	 * {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param domain
	 *            the domain to create the wavelet on. In general, this should
	 *            correspond to the domain of the incoming event wavelet, except
	 *            when the robot is calling this method outside of an event or
	 *            when the server is handling multiple domains.
	 * @param participants
	 *            the initial participants on the wave. The robot, as the
	 *            creator of the wave, will be added by default. The order of
	 *            the participants will be preserved.
	 * @param proxyForId
	 *            the proxy id that should be used to create the new wave. If
	 *            specified, the creator of the wave would be
	 *            robotid+<proxyForId>@appspot.com. Please note that this
	 *            parameter should be properly encoded to ensure that the
	 *            resulting participant id is valid (see
	 *            {@link Util#checkIsValidProxyForId(String)} for more details).
	 */
	public Wavelet newWave(String domain, Set<String> participants,
			String proxyForId) throws ClientWaveException {
		return newWave(domain, participants, "", proxyForId);
	}

	/**
	 * Creates a new wave with a list of participants on it.
	 * 
	 * The root wavelet of the new wave is returned with its own
	 * {@link OperationQueue}. It is the responsibility of the caller to make
	 * sure this wavelet gets submitted to the server, either by calling
	 * AbstractRobot#submit(Wavelet, String) or by calling
	 * {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param domain
	 *            the domain to create the wavelet on. In general, this should
	 *            correspond to the domain of the incoming event wavelet, except
	 *            when the robot is calling this method outside of an event or
	 *            when the server is handling multiple domains.
	 * @param participants
	 *            the initial participants on the wave. The robot, as the
	 *            creator of the wave, will be added by default. The order of
	 *            the participants will be preserved.
	 * @param msg
	 *            the message that will be passed back to the robot when
	 *            WAVELET_CREATED event is fired as a result of this operation.
	 * @param proxyForId
	 *            the proxy id that should be used to create the new wave. If
	 *            specified, the creator of the wave would be
	 *            robotid+<proxyForId>@appspot.com. Please note that this
	 *            parameter should be properly encoded to ensure that the
	 *            resulting participant id is valid (see
	 *            {@link Util#checkIsValidProxyForId(String)} for more details).
	 */
	public Wavelet newWave(String domain, Set<String> participants, String msg,
			String proxyForId) throws ClientWaveException {
		Util.checkIsValidProxyForId(proxyForId);
		return new OperationQueue(proxyForId).createWavelet(domain,
				participants, msg);
	}

	/**
	 * Creates a new wave with a list of participants on it.
	 * 
	 * The root wavelet of the new wave is returned with its own
	 * {@link OperationQueue}. It is the responsibility of the caller to make
	 * sure this wavelet gets submitted to the server, either by calling
	 * AbstractRobot#submit(Wavelet, String) or by calling
	 * {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param domain
	 *            the domain to create the wavelet on. In general, this should
	 *            correspond to the domain of the incoming event wavelet, except
	 *            when the robot is calling this method outside of an event or
	 *            when the server is handling multiple domains.
	 * @param participants
	 *            the initial participants on the wave. The robot, as the
	 *            creator of the wave, will be added by default. The order of
	 *            the participants will be preserved.
	 * @param msg
	 *            the message that will be passed back to the robot when
	 *            WAVELET_CREATED event is fired as a result of this operation.
	 * @param proxyForId
	 *            the proxy id that should be used to create the new wave. If
	 *            specified, the creator of the wave would be
	 *            robotid+<proxyForId>@appspot.com. Please note that this
	 *            parameter should be properly encoded to ensure that the
	 *            resulting participant id is valid (see
	 *            {@link Util#checkIsValidProxyForId(String)} for more details).
	 * 
	 * @throws IOException
	 *             if there is a problem submitting the operation to the server,
	 *             when {@code submit} is {@code true}.
	 */
	public Wavelet newWaveWithSubmit(String domain, Set<String> participants,
			String msg, String proxyForId) throws ClientWaveException {
		Util.checkIsValidProxyForId(proxyForId);
		OperationQueue opQueue = new OperationQueue(proxyForId);
		Wavelet newWavelet = opQueue.createWavelet(domain, participants, msg);

		JsonRpcResponse response = submit(newWavelet).get(1);
		if (response.isError()) {
			throw new ClientWaveException(response.getErrorMessage());
		}
		WaveId waveId = WaveId.deserialise((String) response.getData().get(
				ParamsProperty.WAVE_ID));
		WaveletId waveletId = WaveletId.deserialise((String) response.getData()
				.get(ParamsProperty.WAVELET_ID));
		String rootBlipId = (String) response.getData().get(
				ParamsProperty.BLIP_ID);

		Map<String, Blip> blips = new HashMap<String, Blip>();
		Map<String, String> roles = new HashMap<String, String>();

		newWavelet = new Wavelet(waveId, waveletId, rootBlipId, participants,
				roles, blips, opQueue);

		blips.put(rootBlipId, new Blip(rootBlipId, "", null, newWavelet));

		return newWavelet;
	}

	/**
	 * Fetches a wavelet using the active API.
	 * 
	 * The returned wavelet contains a snapshot of the state of the wavelet at
	 * that point. It can be used to modify the wavelet, but the wavelet might
	 * change in between, so treat carefully.
	 * 
	 * Also, the returned wavelet has its own {@link OperationQueue}. It is the
	 * responsibility of the caller to make sure this wavelet gets submitted to
	 * the server, either by calling AbstractRobot#submit(Wavelet, String) or by
	 * calling {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param waveId
	 *            the id of the wave to fetch.
	 * @param waveletId
	 *            the id of the wavelet to fetch.
	 * 
	 * @throws IOException
	 *             if there is a problem fetching the wavelet.
	 */
	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId)
			throws ClientWaveException {
		return fetchWavelet(waveId, waveletId, null);
	}

	/**
	 * Fetches a wavelet using the active API.
	 * 
	 * The returned wavelet contains a snapshot of the state of the wavelet at
	 * that point. It can be used to modify the wavelet, but the wavelet might
	 * change in between, so treat carefully.
	 * 
	 * Also, the returned wavelet has its own {@link OperationQueue}. It is the
	 * responsibility of the caller to make sure this wavelet gets submitted to
	 * the server, either by calling AbstractRobot#submit(Wavelet, String) or by
	 * calling {@link Wavelet#submitWith(Wavelet)} on the new wavelet.
	 * 
	 * @param waveId
	 *            the id of the wave to fetch.
	 * @param waveletId
	 *            the id of the wavelet to fetch.
	 * @param proxyForId
	 *            the proxy id that should be used to fetch this wavelet. Please
	 *            note that this parameter should be properly encoded to ensure
	 *            that the resulting participant id is valid (see
	 *            {@link Util#checkIsValidProxyForId(String)} for more details).
	 * 
	 * @throws IOException
	 *             if there is a problem fetching the wavelet.
	 */
	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId) throws ClientWaveException {
		Util.checkIsValidProxyForId(proxyForId);
		OperationQueue opQueue = new OperationQueue(proxyForId);
		opQueue.fetchWavelet(waveId, waveletId);

		// Get the response for the robot.fetchWavelet() operation, which is the
		// second operation, since makeRpc prepends the robot.notify()
		// operation.
		JsonRpcResponse response = makeRpc(opQueue).get(0);
		if (response.isError()) {
			throw new ClientWaveException(response.getErrorMessage());
		}

		// Deserialize wavelet.
		opQueue.clear();
		WaveletData waveletData = (WaveletData) response.getData().get(
				ParamsProperty.WAVELET_DATA);
		Map<String, Blip> blips = new HashMap<String, Blip>();
		Wavelet wavelet = Wavelet.deserialize(opQueue, blips, waveletData);

		// Deserialize blips.
		@SuppressWarnings("unchecked")
		Map<String, BlipData> blipDatas = (Map<String, BlipData>) response
				.getData().get(ParamsProperty.BLIPS);
		for (Entry<String, BlipData> entry : blipDatas.entrySet()) {
			blips.put(entry.getKey(), Blip.deserialize(opQueue, wavelet, entry
					.getValue()));
		}

		return wavelet;
	}

	/**
	 * Submits the given operations.
	 * 
	 * @param opQueue
	 *            the operation queue to be submitted.
	 * @param rpcServerUrl
	 *            the active gateway to send the operations to.
	 * @return a list of {@link JsonRpcResponse} that represents the responses
	 *         from the server for all operations that were submitted.
	 * @throws IOException
	 * 
	 * @throws IllegalStateException
	 *             if this method is called prior to setting the proper consumer
	 *             key, secret, and handler URL.
	 * @throws IOException
	 *             if there is a problem submitting the operations.
	 */
	private List<JsonRpcResponse> makeRpc(OperationQueue opQueue)
			throws ClientWaveException {

		String body = SERIALIZER_FOR_ACTIVE_API.toJson(opQueue
				.getPendingOperations(),
				new TypeToken<List<OperationRequest>>() {
				}.getType());

		LOG.info("JSON request to be sent: " + body);

		String responseString;
		try {
			responseString = jsonHandler.request(body);
		} catch (Exception e) {
			throw new ClientWaveException(e);
		}

		LOG.info("Response returned: " + responseString);

		List<JsonRpcResponse> responses = null;
		if (responseString.startsWith("[")) {
			Type listType = new TypeToken<List<JsonRpcResponse>>() {
			}.getType();
			responses = SERIALIZER_FOR_ACTIVE_API.fromJson(responseString,
					listType);
		} else {
			responses = new ArrayList<JsonRpcResponse>(1);
			responses.add(SERIALIZER_FOR_ACTIVE_API.fromJson(responseString,
					JsonRpcResponse.class));
		}
		return responses;
	}

	/**
	 * Performs a full-text search for waves.
	 * 
	 * @param query
	 *            the query. For example: "in:inbox" or "tile:hello".
	 * @param index
	 *            the index to start from (paging support).
	 * @param numResults
	 *            the total results to fetch (paging support).
	 * @return a search results instance.
	 * @throws ClientWaveException
	 *             if an error happens during json rpc.
	 */
	public SearchResult search(String query, int index, int numResults)
			throws ClientWaveException {

		OperationQueue opQueue = new OperationQueue();
		opQueue.appendOperation(OperationType.ROBOT_SEARCH, Parameter.of(
				ParamsProperty.QUERY, query), Parameter.of(
				ParamsProperty.INDEX, index), Parameter.of(
				ParamsProperty.NUM_RESULTS, numResults));

		JsonRpcResponse response = makeRpc(opQueue).get(0);
		if (response.isError()) {
			throw new ClientWaveException(response.getErrorMessage());
		}

		opQueue.clear();

		SearchResult result = (SearchResult) response.getData().get(
				ParamsProperty.SEARCH_RESULTS);
		return result;
	}
}
