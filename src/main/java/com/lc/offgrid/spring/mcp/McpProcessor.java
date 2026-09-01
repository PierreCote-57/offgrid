package com.lc.offgrid.spring.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.lc.basics.tools.logging.BasicLogger;
import com.lc.offgrid.pojo.mcp.server.McpErrorCode;
import com.lc.offgrid.pojo.mcp.tool.McpCustomer;
import com.lc.offgrid.pojo.mcp.wire.McpAnswer;
import com.lc.offgrid.pojo.mcp.wire.McpContent;
import com.lc.offgrid.pojo.mcp.wire.McpInitializeResult;
import com.lc.offgrid.pojo.mcp.wire.McpMessage;
import com.lc.offgrid.pojo.mcp.wire.McpParams;
import com.lc.offgrid.pojo.mcp.wire.McpTool;
import com.lc.offgrid.pojo.mcp.wire.McpToolListResult;
import com.lc.offgrid.pojo.mcp.wire.McpToolResult;
import com.lc.offgrid.spring.mcp.tool.AbstractMcpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The MCP protocol: it reads the JSON-RPC message, keeps the sessions, and answers in the
 * envelopes the protocol asks for. What the site actually knows is the tools' own, one class
 * each under AbstractMcpTool, and what the server itself is comes from McpOffgrid.
 */
@Component
public class McpProcessor
{
	private static final BasicLogger	LOGGER			= BasicLogger.getLogger(McpProcessor.class);

	/**
	 * MCP's own Gson. An answer has to carry the id of the request identically, and the shared
	 * file Gson reads every number as a Double, so a client's 7 would be answered as 7.0 and it
	 * would never match the call it made. Pretty printing is off: nothing reads this wire.
	 */
	private static final Gson			GSON			= new GsonBuilder()
			.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
			.disableHtmlEscaping()
			.create();

	public static final String SESSION_HEADER = "Mcp-Session-Id";
	public static final String VERSION_HEADER = "MCP-Protocol-Version";

	/** The revision of the MCP specification this server answers with. */
	public static final String PROTOCOL_VERSION = "2025-06-18";

	public static final String METHOD_INITIALIZE = "initialize";
	public static final String METHOD_NOTIF_INIT = "notifications/initialized";
	public static final String METHOD_PING = "ping";
	public static final String METHOD_TOOL_LIST = "tools/list";
	public static final String METHOD_TOOL_CALL = "tools/call";

	/** How long a server stream is held open with nothing on it. */
	private static final long STREAM_TIMEOUT_MS = 30 * 60 * 1000L;

	@Autowired
	private McpOffgrid offgrid;

	private final Map<String, McpSession>			m_sessionMap	= new ConcurrentHashMap<>();
	private final Map<String, AbstractMcpTool<?>>	m_toolMap		= new LinkedHashMap<>();

	/**
	 * Spring hands over every bean extending AbstractMcpTool, and they are keyed here by the name
	 * they publish: a new tool is a new class, and nothing in this file changes.
	 */
	public McpProcessor(List<AbstractMcpTool<?>> toolList)
	{
		for (AbstractMcpTool<?> tool : toolList)
		{
			m_toolMap.put(tool.getName(), tool);
		}
		getLogger().info("MCP offers %d tools: %s", m_toolMap.size(), m_toolMap.keySet());
	}

	public static BasicLogger getLogger()
	{
		return LOGGER;
	}
	public static Gson getGson()
	{
		return GSON;
	}
	public McpOffgrid getOffgrid()
	{
		return offgrid;
	}
	public Map<String, AbstractMcpTool<?>> getToolMap()
	{
		return m_toolMap;
	}
	public Map<String, McpSession> getSessionMap()
	{
		return m_sessionMap;
	}

	/**
	 * The stream the server owns, for a session that already exists. A session already streaming
	 * answers 409: the protocol allows only one.
	 */
	public ResponseEntity<SseEmitter> processStream(String sessionId, String lastEventId)
	{
		McpSession session = getSessionMap().get(sessionId);
		if (null == session)
		{
			return McpErrorCode.SESSION_NOT_FOUND_STREAM.makeResponse(null, "Unknown MCP session %s, on stream", sessionId);
		}

		SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
		emitter.onCompletion(() -> session.setEmitter(null));
		emitter.onTimeout(() -> session.setEmitter(null));

		boolean claimed = session.claimStream(emitter);
		if (!claimed)
		{
			return McpErrorCode.STREAM_TAKEN.makeResponse(null, "MCP session %s is already streaming", sessionId);
		}

		getLogger().info("MCP session %s opened its stream, from event %s", sessionId, lastEventId);

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder = builder.contentType(MediaType.TEXT_EVENT_STREAM);
		ResponseEntity<SseEmitter> answer = builder.body(emitter);
		return answer;
	}

	/**
	 * The client is done. The session is dropped and its stream closed, so a message arriving on
	 * that id afterwards is told to initialize again.
	 */
	public ResponseEntity<Void> processEndSession(String sessionId)
	{
		McpSession session = getSessionMap().remove(sessionId);
		if (null == session)
		{
			return McpErrorCode.SESSION_NOT_FOUND_END.makeResponse(null, "Unknown MCP session %s, on delete", sessionId);
		}
		session.closeStream();

		getLogger().info("MCP session %s closed", sessionId);

		return ResponseEntity.noContent().build();
	}

	/**
	 * One message from the client, once everything ahead of its method has been answered: one arm
	 * of the switch below per method the server offers.
	 */
	public ResponseEntity<String> processMessage(String sessionId, String protocolVersion, String jsonRpc)
	{
		McpMessage message = readMessage(jsonRpc);
		ResponseEntity<String> response = preProcessMessage(sessionId, protocolVersion, message);
		if (null != response)
		{
			return response;
		}

		switch (message.getMethod())
		{
			case METHOD_NOTIF_INIT ->
			{
				getLogger().info("MCP client is ready");
				response = makeResponseAccepted();
			}
			case METHOD_PING ->
			{
				getLogger().debug("MCP ping");
				Map<String, Object> result = Collections.emptyMap();
				McpAnswer answer = makeResult(message.getId(), result);
				response = makeResponseJson(answer);
			}
			case METHOD_TOOL_LIST ->
			{
				List<McpTool> publishedList = makePublishedToolList();
				McpToolListResult result = new McpToolListResult(publishedList);
				McpAnswer answer = makeResult(message.getId(), result);
				response = makeResponseJson(answer);
			}
			case METHOD_TOOL_CALL ->
			{
				response = processToolCall(sessionId, message);
			}
			default ->
			{
				if (null == message.getId())
				{
					getLogger().debug("Ignored MCP notification %s", message.getMethod());
					response = makeResponseAccepted();
				}
				else
				{
					response = McpErrorCode.METHOD_NOT_FOUND.makeResponse(message.getId(),
						"Unknown MCP method: %s", message.getMethod());
				}
			}
		}

		return response;
	}

	/**
	 * Everything a message meets before its method is run: it has to be JSON, it has to be
	 * JSON-RPC 2.0, it has to name a method, and every method but initialize has to arrive under a
	 * session this server holds. An answer here is the whole answer; a null says the message may
	 * go on to its method.
	 *
	 * Input the server cannot accept at all answers 400, carrying the JSON-RPC error in the body.
	 * A session this server does not know answers 404, which is how the client learns to
	 * initialize again.
	 */
	private ResponseEntity<String> preProcessMessage(String sessionId, String protocolVersion, McpMessage message)
	{
		ResponseEntity<String> answer = null;

		if (null == message)
		{
			answer = McpErrorCode.PARSE_ERROR.makeResponse(null, "MCP message is not JSON");
		}
		else if (!McpAnswer.JSON_RPC_VERSION.equals(message.getJsonRpc()))
		{
			answer = McpErrorCode.INVALID_REQUEST.makeResponse(message.getId(),
				"MCP message names JSON-RPC %s, not %s", message.getJsonRpc(), McpAnswer.JSON_RPC_VERSION);
		}
		else if (null == message.getMethod())
		{
			answer = McpErrorCode.INVALID_REQUEST.makeResponse(message.getId(), "MCP message carries no method");
		}
		else if (METHOD_INITIALIZE.equals(message.getMethod()))
		{
			answer = processInitialize(message);
		}
		else if (null == sessionId)
		{
			answer = McpErrorCode.INVALID_REQUEST.makeResponse(message.getId(),
				"MCP message %s carries no session", message.getMethod());
		}
		else
		{
			McpSession session = getSessionMap().get(sessionId);
			if (null == session)
			{
				answer = McpErrorCode.SESSION_NOT_FOUND.makeResponse(message.getId(),
					"Unknown MCP session %s, on %s", sessionId, message.getMethod());
			}
			else
			{
				session.markUsed(protocolVersion);
			}
		}

		return answer;
	}

	/** The message as the request it describes, or null when it is not JSON at all. */
	private McpMessage readMessage(String jsonRpc)
	{
		try
		{
			McpMessage message = getGson().fromJson(jsonRpc, McpMessage.class);
			return message;
		}
		catch (Exception exception)
		{
			getLogger().info("Unreadable MCP message: %s", jsonRpc);
			return null;
		}
	}

	/**
	 * The client's first message. It opens the session, and the id it is answered with is the one
	 * it puts on every message after this.
	 */
	private ResponseEntity<String> processInitialize(McpMessage message)
	{
		McpParams params = message.getParams();
		String askedVersion = null == params ? null : params.getProtocolVersion();

		McpSession session = new McpSession();
		getSessionMap().put(session.getId(), session);

		McpInitializeResult result = getOffgrid().initialize(PROTOCOL_VERSION);
		McpAnswer answer = makeResult(message.getId(), result);

		getLogger().info("MCP session %s opened, client asked for %s, answered with %s",
			session.getId(), askedVersion, PROTOCOL_VERSION);

		return makeResponseNewSession(answer, session.getId());
	}

	/**
	 * Every tool this server offers, as tools/list publishes them: one entry per bean, each built
	 * from what its own constructor declared.
	 */
	private List<McpTool> makePublishedToolList()
	{
		List<McpTool> answer = new LinkedList<>();
		for (AbstractMcpTool<?> tool : getToolMap().values())
		{
			McpTool publishedTool = tool.makeTool();
			answer.add(publishedTool);
		}
		return answer;
	}

	/**
	 * One tool, run by name. The name is what tools/list published; a name nobody published is a
	 * bad parameter rather than an unknown method. What the tool is handed is its own arguments
	 * and its caller's customer, and what it answers is the text the model reads.
	 */
	private ResponseEntity<String> processToolCall(String sessionId, McpMessage message)
	{
		McpParams params = message.getParams();
		String toolName = null == params ? null : params.getName();
		AbstractMcpTool<?> tool = getToolMap().get(toolName);

		ResponseEntity<String> response = null;
		if (null == tool)
		{
			response = McpErrorCode.INVALID_PARAMS.makeResponse(message.getId(),
				"Unknown MCP tool: %s", toolName);
		}
		else
		{
			McpSession session = getSessionMap().get(sessionId);
			McpCustomer customer = session.getCustomer();
			Map<String, Object> argumentMap = params.getArguments();

			String text = tool.processArgumentMap(argumentMap, customer);

			McpToolResult result = makeToolText(text);
			McpAnswer answer = makeResult(message.getId(), result);
			response = makeResponseJson(answer);
		}

		return response;
	}

	/** A successful answer, carrying the id of the request it answers. */
	private McpAnswer makeResult(Object id, Object result)
	{
		McpAnswer answer = new McpAnswer(id);
		answer.setResult(result);
		return answer;
	}

	/** What a tool answers with: its result as one piece of text. */
	private McpToolResult makeToolText(String text)
	{
		McpContent content = new McpContent(McpContent.TYPE_TEXT, text);

		List<McpContent> contentList = new LinkedList<>();
		contentList.add(content);

		McpToolResult answer = new McpToolResult(contentList);
		answer.setIsError(false);
		return answer;
	}

	/** One envelope, on its way out. */
	private ResponseEntity<String> makeResponseJson(McpAnswer envelope)
	{
		String body = getGson().toJson(envelope);

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder = builder.contentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> answer = builder.body(body);
		return answer;
	}

	/**
	 * The answer that opens a session. It is the one answer carrying the session header, and the
	 * id it stamps is what the client puts on every message it sends afterwards.
	 */
	private ResponseEntity<String> makeResponseNewSession(McpAnswer envelope, String sessionId)
	{
		String body = getGson().toJson(envelope);

		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		builder = builder.contentType(MediaType.APPLICATION_JSON);
		builder = builder.header(SESSION_HEADER, sessionId);
		ResponseEntity<String> answer = builder.body(body);
		return answer;
	}

	/** A notification is never answered, so it is acknowledged with no body at all. */
	private ResponseEntity<String> makeResponseAccepted()
	{
		ResponseEntity<String> answer = ResponseEntity.accepted().build();
		return answer;
	}

	/**
	 * One client's session: the id it was given, when it was last heard from, and the stream it
	 * holds open, if it opened one.
	 */
	public static class McpSession
	{
		private final String		m_id;
		private final McpCustomer	m_customer	= new McpCustomer();
		private final long			m_timeOpenedMS;
		private long				m_timeUsedMS;
		private String				m_protocolVersion;
		private SseEmitter			m_emitter;

		public McpSession()
		{
			UUID uuid = UUID.randomUUID();
			m_id = uuid.toString();
			m_timeOpenedMS = System.currentTimeMillis();
			m_timeUsedMS = m_timeOpenedMS;
		}

		public void markUsed(String protocolVersion)
		{
			m_timeUsedMS = System.currentTimeMillis();
			m_protocolVersion = protocolVersion;
		}

		/**
		 * The stream is taken by the first caller to ask for it, and refused to every other, so
		 * that two GETs arriving together cannot both believe they own it.
		 */
		public synchronized boolean claimStream(SseEmitter emitter)
		{
			if (null != m_emitter)
			{
				return false;
			}
			m_emitter = emitter;
			return true;
		}

		public void closeStream()
		{
			SseEmitter emitter = getEmitter();
			if (null != emitter)
			{
				emitter.complete();
				setEmitter(null);
			}
		}

		public McpCustomer getCustomer()
		{
			return m_customer;
		}
		public String getId()
		{
			return m_id;
		}
		public long getTimeOpenedMS()
		{
			return m_timeOpenedMS;
		}
		public long getTimeUsedMS()
		{
			return m_timeUsedMS;
		}
		public String getProtocolVersion()
		{
			return m_protocolVersion;
		}
		public SseEmitter getEmitter()
		{
			return m_emitter;
		}
		public void setEmitter(SseEmitter emitter)
		{
			m_emitter = emitter;
		}
	}
}
