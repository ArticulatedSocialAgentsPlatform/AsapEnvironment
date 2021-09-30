package hmi.audioenvironment.middleware;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hmi.audioenvironment.SoundManager;
import hmi.audioenvironment.Wav;
import hmi.audioenvironment.WavCreationException;
import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;

/**
 * Sound manager that streams clips over middleware.
 * @author jankolkmeier
 * 
 * For now, the main client to use this SoundManager with is the AudioStreamingReceiver unity client:
 * 	https://github.com/jankolkmeier/ASAPToolkitUnity/blob/master/Scripts/Environment/AudioStreamingReceiver.cs
 * 
 * Retransmission of (partial) data has been implemented in the application layer, allowing hot-plugging
 * clients that may have missed a previously cached clip, even during an ongoing utterance.
 */
public class MiddlewareStreamingSoundManager implements SoundManager, MiddlewareListener {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MiddlewareStreamingSoundManager.class.getName());

	private Middleware middleware;
	private ObjectMapper om;
	
	Map<String, StreamingClip> clipLut = new HashMap<String, StreamingClip>();
	
	public MiddlewareStreamingSoundManager(Middleware mw) {
		this.om = new ObjectMapper();
		this.middleware = mw;
	}
	
    @Override
    public void init() {
        logger.debug("init");
    	this.middleware.addListener(this);
    }
    
    @Override
    public Wav createWav(InputStream inputStream, String source) throws WavCreationException {
        logger.debug("create WAV for streaming. Source: {}", source);
    	StreamingClip res = new StreamingClip(inputStream, middleware, source);
        clipLut.put(res.getClipId(), res);
        /* All(?) our middleware implementations use a send queue, 
         * so the send call shouldn't block the entire thread for I/O.
         * Otherwise we might want to consider running this in a separate thread:
         */
        res.sendWholeClip();
        return res;
    }

    @Override
    public Wav createWav(InputStream inputStream) throws WavCreationException {
        return createWav(inputStream, "");
    }

    @Override
    public void shutdown() {
                
    }

	@Override
	public void receiveData(JsonNode jn) {
        logger.debug("recv data, msgType: {}", jn.get("msgType").asText());
		if (jn.has("msgType") && jn.get("msgType").asText().equals("retransmit")) {
			try {
				StreamingClipRetransmitRequestJSON req = om.treeToValue(jn, StreamingClipRetransmitRequestJSON.class);
				handleRetransmission(req);
			} catch (JsonProcessingException e) {
				logger.warn("failed to parse retransmit request: ", e);
			}
		} else {
			logger.warn("Can't handle message: {}", jn.asText());
		}
	}
	
    private void handleRetransmission(StreamingClipRetransmitRequestJSON req) {
        logger.debug("handle retransmit, clipId: {}", req.clipId);
		if (!clipLut.containsKey(req.clipId)) {
			logger.warn("Clip does not exist for retransmission: {}", req.clipId);
			return;
		}
		
		JsonNode partData = clipLut.get(req.clipId).getClipPartForRetransmission(req.partIdx);
		if (partData != null) {
			middleware.sendData(partData);
			logger.debug("Clip (part) retransmitted: {}:{}", req.clipId, req.partIdx);
		}
		else {
			logger.warn("Clip {} exists for retransmission, but part out of range: {}", req.clipId, req.partIdx);
		}
    }

}

class StreamingClipRequestJSON {
	public String msgType;
	public String source;
	public String clipId;
	
	public StreamingClipRequestJSON() {}
	public StreamingClipRequestJSON(String msgTye, String clipId, String source) {
		
	}
}

class StreamingClipRetransmitRequestJSON extends StreamingClipRequestJSON {
	public int partIdx;
	
	public StreamingClipRetransmitRequestJSON() {}
	public StreamingClipRetransmitRequestJSON(String clipId, int partIdx) {
		super("retransmit", clipId, "");
		this.partIdx = partIdx;
	}
}
