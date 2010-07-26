package com.google.wave.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.SimpleOAuthValidator;
import net.oauth.signature.OAuthSignatureMethod;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.OperationRequest.Parameter;
import com.google.wave.api.SearchResult.Digest;
import com.google.wave.api.event.AnnotatedTextChangedEvent;
import com.google.wave.api.event.BlipContributorsChangedEvent;
import com.google.wave.api.event.BlipSubmittedEvent;
import com.google.wave.api.event.DocumentChangedEvent;
import com.google.wave.api.event.Event;
import com.google.wave.api.event.EventHandler;
import com.google.wave.api.event.FormButtonClickedEvent;
import com.google.wave.api.event.GadgetStateChangedEvent;
import com.google.wave.api.event.OperationErrorEvent;
import com.google.wave.api.event.WaveletBlipCreatedEvent;
import com.google.wave.api.event.WaveletBlipRemovedEvent;
import com.google.wave.api.event.WaveletCreatedEvent;
import com.google.wave.api.event.WaveletFetchedEvent;
import com.google.wave.api.event.WaveletParticipantsChangedEvent;
import com.google.wave.api.event.WaveletSelfAddedEvent;
import com.google.wave.api.event.WaveletSelfRemovedEvent;
import com.google.wave.api.event.WaveletTagsChangedEvent;
import com.google.wave.api.event.WaveletTitleChangedEvent;
import com.google.wave.api.impl.EventMessageBundle;
import com.google.wave.api.impl.GsonFactory;
import com.google.wave.api.impl.WaveletData;

public abstract class AbstractWave implements EventHandler {

	/** Some mime types. */
	public static final String JSON_MIME_TYPE = "application/json; charset=utf-8";
	public static final String TEXT_MIME_TYPE = "text/plain; charset=utf-8";
	public static final String XML_MIME_TYPE = "application/xml";

	/** Some constants for encoding. */
	public static final String UTF_8 = "UTF-8";
	public static final String SHA_1 = "SHA-1";
	public static final String OAUTH_BODY_HASH = "oauth_body_hash";
	public static final String OAUTH_CONSUMER_KEY_DOMAIN = "google.com";

	public static final String POST = "POST";

	/** The query parameter to specify custom profile request. */
	public static final String NAME_QUERY_PARAMETER_KEY = "name";

	/** The query parameter for security token. */
	public static final String SECURITY_TOKEN_PARAMETER_KEY = "st";

	private static final Logger LOG = Logger.getLogger(AbstractWave.class
			.getName());
	private static final String ACTIVE_API_OPERATION_NAMESPACE = "wave";

	/** Serializer to serialize events and operations in the event-based mode. */
	private static final Gson SERIALIZER = new GsonFactory().create();

	static {
		GsonFactory factory = new GsonFactory();
		factory.registerTypeAdapter(SearchResult.class,
				new SearchResultGsonAdaptor());
		factory.registerTypeAdapter(Digest.class, new DigestGsonAdaptor());
		SERIALIZER_FOR_ACTIVE_API = factory
				.create(ACTIVE_API_OPERATION_NAMESPACE);
	}

	/** Serializer to serialize events and operations in active mode. */
	private static final Gson SERIALIZER_FOR_ACTIVE_API;

	/** A map of RPC server URL to its consumer data object. */
	// private final Map<String, ConsumerData> consumerData = new
	// HashMap<String, ConsumerData>();
	/** The token used to verify author during the registration process. */
	private String verificationToken;

	/** The token that is checked when handling verification token request. */
	private String securityToken;

	private boolean allowUnsignedRequests = true;

	/**
	 * Submits the pending operations associated with this {@link Wavelet}.
	 * 
	 * @param wavelet
	 *            the bundle that contains the operations to be submitted.
	 * @param rpcServerUrl
	 *            the active gateway to send the operations to.
	 * @return a list of {@link JsonRpcResponse} that represents the responses
	 *         from the server for all operations that were submitted.
	 * 
	 * @throws IllegalStateException
	 *             if this method is called prior to setting the proper consumer
	 *             key, secret, and handler URL.
	 * @throws IOException
	 *             if there is a problem submitting the operations.
	 */
	public List<JsonRpcResponse> submit(Wavelet wavelet, String rpcServerUrl)
			throws IOException {
		OperationQueue opQueue = wavelet.getOperationQueue();
		List<JsonRpcResponse> responses = makeRpc(opQueue, rpcServerUrl);
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
	public Wavelet blindWavelet(WaveId waveId, WaveletId waveletId) {
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
			String proxyForId) {
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
	 *            the id of the wave.
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
	 * @return a stub of a wavelet.
	 */
	public Wavelet blindWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId, Map<String, Blip> blips) {
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
	public Wavelet newWave(String domain, Set<String> participants) {
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
			String proxyForId) {
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
			String proxyForId) {
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
	 * @param rpcServerUrl
	 *            if specified, this operation will be submitted immediately to
	 *            this active gateway, that will return immediately the actual
	 *            wave id, the id of the root wavelet, and id of the root blip.
	 * 
	 * @throws IOException
	 *             if there is a problem submitting the operation to the server,
	 *             when {@code submit} is {@code true}.
	 */
	public Wavelet newWave(String domain, Set<String> participants, String msg,
			String proxyForId, String rpcServerUrl) throws IOException {
		Util.checkIsValidProxyForId(proxyForId);
		OperationQueue opQueue = new OperationQueue(proxyForId);
		Wavelet newWavelet = opQueue.createWavelet(domain, participants, msg);

		if (rpcServerUrl != null && !(rpcServerUrl.length() == 0)) {
			// Get the response for the robot.fetchWavelet() operation, which is
			// the
			// second operation, since makeRpc prepends the robot.notify()
			// operation.
			JsonRpcResponse response = this.submit(newWavelet, rpcServerUrl)
					.get(1);
			if (response.isError()) {
				throw new IOException(response.getErrorMessage());
			}
			WaveId waveId = WaveId.deserialise((String) response.getData().get(
					ParamsProperty.WAVE_ID));
			WaveletId waveletId = WaveletId.deserialise((String) response
					.getData().get(ParamsProperty.WAVELET_ID));
			String rootBlipId = (String) response.getData().get(
					ParamsProperty.BLIP_ID);

			Map<String, Blip> blips = new HashMap<String, Blip>();
			Map<String, String> roles = new HashMap<String, String>();

			newWavelet = new Wavelet(waveId, waveletId, rootBlipId,
					participants, roles, blips, opQueue);

			blips.put(rootBlipId, new Blip(rootBlipId, "", null, newWavelet));
		}
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
	 * @param rpcServerUrl
	 *            the active gateway that is used to fetch the wavelet.
	 * 
	 * @throws IOException
	 *             if there is a problem fetching the wavelet.
	 */
	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId,
			String rpcServerUrl) throws IOException {
		return fetchWavelet(waveId, waveletId, null, rpcServerUrl);
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
	 * @param rpcServerUrl
	 *            the active gateway that is used to fetch the wavelet.
	 * 
	 * @throws IOException
	 *             if there is a problem fetching the wavelet.
	 */
	public Wavelet fetchWavelet(WaveId waveId, WaveletId waveletId,
			String proxyForId, String rpcServerUrl) throws IOException {
		Util.checkIsValidProxyForId(proxyForId);
		OperationQueue opQueue = new OperationQueue(proxyForId);
		opQueue.fetchWavelet(waveId, waveletId);

		// Get the response for the robot.fetchWavelet() operation, which is the
		// second operation, since makeRpc prepends the robot.notify()
		// operation.
		JsonRpcResponse response = makeRpc(opQueue, rpcServerUrl).get(0);
		if (response.isError()) {
			throw new IOException(response.getErrorMessage());
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
	 * @return a custom profile based on "name" query parameter, or {@code null}
	 *         if this robot doesn't support custom profile.
	 */
	protected ParticipantProfile getCustomProfile(String name) {
		return null;
	}

	/**
	 * Sets up the verification token that is used for the owner verification
	 * step during the robot registration.
	 * 
	 * @param verificationToken
	 *            the verification token.
	 * @param securityToken
	 *            the security token that should be matched against when serving
	 *            a verification token request.
	 */
	protected void setupVerificationToken(String verificationToken,
			String securityToken) {
		this.verificationToken = verificationToken;
		this.securityToken = securityToken;
	}

	/**
	 * Sets the OAuth related properties, including the consumer key and secret
	 * that are used to sign the outgoing operations in the active mode. Robot
	 * developer needs to visit
	 * {@link "http://wave.google.com/wave/robot/register"} to register and
	 * obtain the consumer key and secret.
	 * 
	 * Should you need to make Active API calls to both our public and sandbox
	 * servers from the same robot, you can call this method multiple times,
	 * with the same consumer key and secret, but different RPC server URLs.
	 * 
	 * After calling this method, the robot no longer accepts unsigned requests,
	 * but you can override that by calling
	 * {@link #setAllowUnsignedRequests(boolean)}.
	 * 
	 * @param consumerKey
	 *            the consumer key.
	 * @param consumerSecret
	 *            the consumer secret.
	 * @param rpcServerUrl
	 *            the URL of the server that serves the JSON-RPC request.
	 *            <ul>
	 *            <li>https://www-opensocial.googleusercontent.com/api/rpc - for
	 *            wave preview.
	 *            <li>
	 *            <li>
	 *            https://www-opensocial-sandbox.googleusercontent.com/api/rpc -
	 *            for wave sandbox.</li>
	 *            </ul>
	 * 
	 * @throws IllegalArgumentException
	 *             if any of the arguments are {@code null}.
	 */
	protected void setupOAuth(String consumerKey, String consumerSecret,
			String rpcServerUrl) {
		// if (consumerKey == null || consumerSecret == null
		// || rpcServerUrl == null) {
		// throw new IllegalArgumentException(
		// "Consumer Key, Consumer Secret and RPCServerURL "
		// + "has to be non-null");
		// }
		// ConsumerData consumerDataObj = new ConsumerData(consumerKey,
		// consumerSecret, rpcServerUrl);
		// this.consumerData.put(rpcServerUrl, consumerDataObj);
		// setAllowUnsignedRequests(false);
	}

	/**
	 * Sets the OAuth related properties, including the consumer key and secret
	 * that are used to sign the outgoing operations in the active mode. This
	 * method sets the JSON-RPC handler URL to http://gmodules.com/api/rpc, that
	 * is associated with wave preview instance.
	 * 
	 * @param consumerKey
	 *            the consumer key.
	 * @param consumerSecret
	 *            the consumer secret.
	 */
	public void setupOAuth(String consumerKey, String consumerSecret) {
		setupOAuth(consumerKey, consumerSecret,
				"https://www-opensocial.googleusercontent.com/api/rpc");
	}

	/**
	 * Sets whether or not unsigned incoming requests from robot proxy are
	 * allowed.
	 * 
	 * @param allowUnsignedRequests
	 *            whether or not unsigned requests from robot proxy are allowed.
	 */
	protected void setAllowUnsignedRequests(boolean allowUnsignedRequests) {
		if (!allowUnsignedRequests) {
			throw new IllegalArgumentException(
					"Please call AbstractRobot.setupOAuth() first to "
							+ "setup the consumer key and secret to validate the request.");
		}
		this.allowUnsignedRequests = allowUnsignedRequests;
	}

	/**
	 * @return {@code true} if unsigned incoming requests from robot proxy are
	 *         allowed.
	 */
	protected boolean isUnsignedRequestsAllowed() {
		return allowUnsignedRequests;
	}

	/**
	 * Processes the incoming HTTP request to obtain the verification token.
	 * 
	 * @param req
	 *            the HTTP request.
	 * @param resp
	 *            the HTTP response.
	 */
	private void processVerifyToken(HttpRequest req, HttpResponse resp) {
		if (verificationToken == null || (verificationToken.length() != 0)) {
			LOG.info("Please register a verification token by calling "
					+ "AbstractRobot.setVerificationToken().");
			resp.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
			return;
		}

		String incomingSecurityToken = (String) req.getParams().getParameter(
				SECURITY_TOKEN_PARAMETER_KEY);
		if (securityToken != null
				&& !securityToken.equals(incomingSecurityToken)) {
			LOG.info("The incoming security token " + incomingSecurityToken
					+ " does not match the " + "expected security token "
					+ securityToken + ".");
			resp.setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
			return;
		}

		resp.setHeader("Content-Type", TEXT_MIME_TYPE);
		// try {
		// resp.getWriter().write(verificationToken);
		// } catch (IOException e) {
		// resp.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
		// return;
		// }
		resp.setStatusCode(HttpURLConnection.HTTP_OK);
	}

	/**
	 * Processes the incoming HTTP request that contains the event bundle.
	 * 
	 * @param req
	 *            the HTTP request.
	 * @param resp
	 *            the HTTP response.
	 */
	private void processRpc(String req) {
		// Deserialize and process the incoming events.
		EventMessageBundle events = null;

		try {
			events = deserializeEvents(req);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		OperationQueue operationQueue = events.getWavelet().getOperationQueue();

		// Call the robot event handlers.
		processEvents(events);

		// Serialize the operations.
		// serializeOperations(operationQueue.getPendingOperations(), resp);

		operationQueue.clear();
	}

	/**
	 * Processes the incoming event bundle. This method iterates over the event
	 * bundle and dispatch the individual event to its own handler, based on the
	 * event type.
	 * 
	 * @param events
	 *            the incoming event bundle.
	 */
	protected void processEvents(EventMessageBundle events) {
		for (Event event : events.getEvents()) {
			switch (event.getType()) {
			case ANNOTATED_TEXT_CHANGED:
				onAnnotatedTextChanged(AnnotatedTextChangedEvent.as(event));
				break;
			case BLIP_CONTRIBUTORS_CHANGED:
				onBlipContributorsChanged(BlipContributorsChangedEvent
						.as(event));
				break;
			case BLIP_SUBMITTED:
				onBlipSubmitted(BlipSubmittedEvent.as(event));
				break;
			case DOCUMENT_CHANGED:
				onDocumentChanged(DocumentChangedEvent.as(event));
				break;
			case FORM_BUTTON_CLICKED:
				onFormButtonClicked(FormButtonClickedEvent.as(event));
				break;
			case GADGET_STATE_CHANGED:
				onGadgetStateChanged(GadgetStateChangedEvent.as(event));
				break;
			case WAVELET_BLIP_CREATED:
				onWaveletBlipCreated(WaveletBlipCreatedEvent.as(event));
				break;
			case WAVELET_BLIP_REMOVED:
				onWaveletBlipRemoved(WaveletBlipRemovedEvent.as(event));
				break;
			case WAVELET_CREATED:
				onWaveletCreated(WaveletCreatedEvent.as(event));
				break;
			case WAVELET_FETCHED:
				onWaveletFetched(WaveletFetchedEvent.as(event));
				break;
			case WAVELET_PARTICIPANTS_CHANGED:
				onWaveletParticipantsChanged(WaveletParticipantsChangedEvent
						.as(event));
				break;
			case WAVELET_SELF_ADDED:
				onWaveletSelfAdded(WaveletSelfAddedEvent.as(event));
				break;
			case WAVELET_SELF_REMOVED:
				onWaveletSelfRemoved(WaveletSelfRemovedEvent.as(event));
				break;
			case WAVELET_TAGS_CHANGED:
				onWaveletTagsChanged(WaveletTagsChangedEvent.as(event));
				break;
			case WAVELET_TITLE_CHANGED:
				onWaveletTitleChanged(WaveletTitleChangedEvent.as(event));
				break;
			case OPERATION_ERROR:
				onOperationError(OperationErrorEvent.as(event));
				break;
			}
		}
	}

	/**
	 * Deserializes the given HTTP request's JSON body into an event message
	 * bundle.
	 * 
	 * @param req
	 *            the HTTP request to be deserialized.
	 * @return an event message bundle.
	 * 
	 * @throws IOException
	 *             if there is a problem reading the request's body.
	 * @throws IllegalArgumentException
	 *             if the request is not signed properly.
	 */
	private EventMessageBundle deserializeEvents(String json)
			throws IOException {

		LOG.info("Incoming events: " + json);

		EventMessageBundle bundle = SERIALIZER.fromJson(json,
				EventMessageBundle.class);

		/*
		 * if (bundle.getRpcServerUrl() == null) { throw new
		 * IllegalArgumentException(
		 * "RPC server URL is not set in the event bundle."); }
		 * 
		 * // Get the OAuth credentials for the given RPC server URL.
		 * ConsumerData consumerDataObj = consumerData.get(bundle
		 * .getRpcServerUrl()); if (consumerDataObj == null &&
		 * !isUnsignedRequestsAllowed()) { throw new IllegalArgumentException(
		 * "No consumer key is found for the RPC server URL: " +
		 * bundle.getRpcServerUrl()); }
		 * 
		 * // Validates the request. if (consumerDataObj != null) { try { //
		 * 
		 * @SuppressWarnings("unchecked") // Map<String, String[]> parameterMap
		 * = req.ParameterMap(); HttpParams parameters = req.getParams(); //
		 * validateOAuthRequest(req.getRequestURL().toString(), // parameters,
		 * validateOAuthRequest( "http://wave.googleusercontent.com/api/rpc",
		 * parameters, json, consumerDataObj.getConsumerKey(),
		 * consumerDataObj.getConsumerSecret()); } catch
		 * (NoSuchAlgorithmException e) { throw new IllegalArgumentException(
		 * "Error validating OAuth request", e); } catch (URISyntaxException e)
		 * { throw new IllegalArgumentException(
		 * "Error validating OAuth request", e); } catch (OAuthException e) {
		 * throw new IllegalArgumentException( "Error validating OAuth request",
		 * e); } }
		 */
		return bundle;
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
	private List<JsonRpcResponse> makeRpc(OperationQueue opQueue,
			String rpcServerUrl) throws IOException {
		if (rpcServerUrl == null) {
			throw new IllegalStateException("RPC Server URL is not set up.");
		}

//		ConsumerData consumerDataObj = consumerData.get(rpcServerUrl);
//		if (consumerDataObj == null) {
//			throw new IllegalStateException(
//					"Consumer key, consumer secret, and  JSON-RPC server URL "
//							+ "have to be set first, by calling AbstractRobot.setupOAuth(), before invoking "
//							+ "AbstractRobot.submit().");
//		}

		String json = SERIALIZER_FOR_ACTIVE_API.toJson(opQueue
				.getPendingOperations(),
				new TypeToken<List<OperationRequest>>() {
				}.getType());

		LOG.info("JSON request to be sent: " + json);

		String responseString = send(rpcServerUrl, JSON_MIME_TYPE, json);

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

	public void send(Wavelet wavelet, String rpcServerUrl) {
		try {
			makeRpc(wavelet.getOperationQueue(), rpcServerUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Serializes the given outgoing operations into a JSON string, and put it
	 * in the given response object.
	 * 
	 * @param operations
	 *            the operations to be serialized.
	 * @param resp
	 *            the response object to flush the output string into.
	 */
	private static void serializeOperations(List<OperationRequest> operations,
			HttpResponse resp) {
		// try {
		String json = SERIALIZER.toJson(operations,
				new TypeToken<List<OperationRequest>>() {
				}.getType());
		LOG.info("Outgoing operations: " + json);

		resp.setHeader("Content-Type", JSON_MIME_TYPE);
		// ((BasicHttpResponse)resp).getEntity().getEntity().setEntity(new
		// BasicHttpEntity().setContent(json));
		resp.setStatusCode(HttpURLConnection.HTTP_OK);
		// } catch (IOException iox) {
		// resp.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
		// }
	}

	/**
	 * Creates a URL that contains the necessary OAuth query parameters for the
	 * given JSON string.
	 * 
	 * @param jsonBody
	 *            the JSON string to construct the URL from.
	 * @param rpcServerUrl
	 *            the URL of the handler that services the JSON-RPC request.
	 * @param consumerKey
	 *            the OAuth consumerKey.
	 * @param consumerSecret
	 *            the OAuth consumerSecret.
	 * 
	 * @return a URL for the given JSON string, and the required OAuth
	 *         parameters:
	 *         <ul>
	 *         <li>oauth_body_hash</li>
	 *         <li>oauth_consumer_key</li>
	 *         <li>oauth_signature_method</li>
	 *         <li>oauth_timestamp</li>
	 *         <li>oauth_nonce</li>
	 *         <li>oauth_version</li>
	 *         <li>oauth_signature</li>
	 *         </ul>
	 */
	private static String createOAuthUrlString(String jsonBody,
			String rpcServerUrl, String consumerKey, String consumerSecret)
			throws IOException, URISyntaxException, OAuthException {

		OAuthMessage message = new OAuthMessage(POST, rpcServerUrl, Collections
				.<Entry<String, String>> emptyList());

		// Compute the hash of the body.
		byte[] rawBody = jsonBody.getBytes(UTF_8);
		byte[] hash = DigestUtils.sha(rawBody);
		byte[] encodedHash = Base64.encodeBase64(hash);
		message.addParameter(OAUTH_BODY_HASH, new String(encodedHash, UTF_8));

		// Add other parameters.
		OAuthConsumer consumer = new OAuthConsumer(null,
				OAUTH_CONSUMER_KEY_DOMAIN + ":" + consumerKey, consumerSecret,
				null);
		consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.HMAC_SHA1);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		message.addRequiredParameters(accessor);
		LOG.info("Signature base string: "
				+ OAuthSignatureMethod.getBaseString(message));

		// Construct the resulting URL.
		StringBuilder sb = new StringBuilder(rpcServerUrl);
		char connector = '?';
		for (Map.Entry<String, String> p : message.getParameters()) {
			if (!p.getKey().equals(jsonBody)) {
				sb.append(connector);
				sb.append(URLEncoder.encode(p.getKey(), UTF_8));
				sb.append('=');
				sb.append(URLEncoder.encode(p.getValue(), UTF_8));
				connector = '&';
			}
		}
		return sb.toString();
	}

	/**
	 * Validates the incoming HTTP request.
	 * 
	 * @param requestUrl
	 *            the URL of the request.
	 * @param jsonBody
	 *            the request body to be validated.
	 * @param consumerKey
	 *            the consumer key.
	 * @param consumerSecret
	 *            the consumer secret.
	 */
	private static void validateOAuthRequest(String requestUrl,
			HttpParams requestParams, String jsonBody, String consumerKey,
			String consumerSecret) throws NoSuchAlgorithmException,
			IOException, URISyntaxException, OAuthException {
		List<OAuth.Parameter> params = new ArrayList<OAuth.Parameter>();
		// for (Entry<String, String[]> entry : requestParams.entrySet()) {
		// for (String value : entry.getValue()) {
		// params.add(new OAuth.Parameter(entry.getKey(), value));
		// }
		// }
		OAuthMessage message = new OAuthMessage(POST, requestUrl, params);

		// Compute and check the hash of the body.
		MessageDigest md = MessageDigest.getInstance(SHA_1);
		byte[] hash = md.digest(jsonBody.getBytes(UTF_8));

		String encodedHash = new String(Base64.encodeBase64(hash, false), UTF_8);
		if (!encodedHash.equals(message.getParameter(OAUTH_BODY_HASH))) {
			throw new IllegalArgumentException(
					"Body hash does not match. Expected: " + encodedHash
							+ ", provided: "
							+ message.getParameter(OAUTH_BODY_HASH));
		}

		// Construct validator arguments.
		OAuthConsumer consumer = new OAuthConsumer(null, consumerKey,
				consumerSecret, null);
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		LOG.info("Signature base string: "
				+ OAuthSignatureMethod.getBaseString(message));
		message.validateMessage(accessor, new SimpleOAuthValidator());
	}

	public List<Digest> search(String query, String rpcServerUrl)
			throws IOException {

		OperationQueue opQueue = new OperationQueue();
		opQueue.appendOperation(OperationType.ROBOT_SEARCH, Parameter.of(
				ParamsProperty.QUERY, query));

		JsonRpcResponse response = makeRpc(opQueue, rpcServerUrl).get(0);
		if (response.isError()) {
			throw new IOException(response.getErrorMessage());
		}

		opQueue.clear();

		// Deserialize wavelets' digests.
		SearchResult result = (SearchResult) response.getData().get(
				ParamsProperty.SEARCH_RESULTS);
		return result.getDigests();
	}

	public abstract String send(String rpcHandler, String contentType,
			String jsonBody) throws IOException;

	public void onAnnotatedTextChanged(AnnotatedTextChangedEvent event) {
		// No-op.
	}

	public void onBlipContributorsChanged(BlipContributorsChangedEvent event) {
		// No-op.
	}

	public void onBlipSubmitted(BlipSubmittedEvent event) {
		// No-op.
	}

	public void onDocumentChanged(DocumentChangedEvent event) {
		// No-op.
	}

	public void onFormButtonClicked(FormButtonClickedEvent event) {
		// No-op.
	}

	public void onGadgetStateChanged(GadgetStateChangedEvent event) {
		// No-op.
	}

	public void onWaveletBlipCreated(WaveletBlipCreatedEvent event) {
		// No-op.
	}

	public void onWaveletBlipRemoved(WaveletBlipRemovedEvent event) {
		// No-op.
	}

	public void onWaveletCreated(WaveletCreatedEvent event) {
		// No-op.
	}

	public void onWaveletFetched(WaveletFetchedEvent event) {
		// No-op.
	}

	public void onWaveletParticipantsChanged(
			WaveletParticipantsChangedEvent event) {
		// No-op.
	}

	public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
		// No-op.
	}

	public void onWaveletSelfRemoved(WaveletSelfRemovedEvent event) {
		// No-op.
	}

	public void onWaveletTagsChanged(WaveletTagsChangedEvent event) {
		// No-op.
	}

	public void onWaveletTitleChanged(WaveletTitleChangedEvent event) {
		// No-op.
	}

	public void onOperationError(OperationErrorEvent event) {
		// No-op.
	}
}
