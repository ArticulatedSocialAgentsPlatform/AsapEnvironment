package hmi.audioenvironment.middleware;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hmi.audioenvironment.Wav;
import hmi.audioenvironment.WavCreationException;
import nl.utwente.hmi.middleware.Middleware;

public class StreamingClip implements Wav {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MiddlewareStreamingSoundManager.class.getName());
    
	private ObjectMapper om;
	private Middleware middleware;
	private String source;
	private String clipId;

    private static final int AUDIO_STREAM_BUFFER_SIZE = 32000;
    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private long audioFrameLength;
    private long audioLength;
    
    public volatile boolean playing = false;
    public volatile boolean stop = false;
    private float desiredVolume = 50;

    public String getClipId() {
    	return clipId;
    }
    
    private JsonNode[] partsForRetransmission;
    
    public StreamingClip(InputStream inputStream, Middleware mw,  String source) throws WavCreationException {
    	logger.debug("Creating AudioClip for streaming over middleware, src: {}", source);
		om = new ObjectMapper();
		middleware = mw;
		
		this.source = source;
		this.clipId = "A"+RandomStringUtils.randomAlphanumeric(11);
    	StreamingClipDataJSON dataObj = new StreamingClipDataJSON();

        try {
            audioStream = AudioSystem.getAudioInputStream(inputStream);
        } catch (UnsupportedAudioFileException e1) {
            throw new WavCreationException(e1.getLocalizedMessage(), this, e1);
        } catch (IOException e1) {
            throw new WavCreationException(e1.getLocalizedMessage(), this, e1);
        }

        audioFormat = audioStream.getFormat();
        audioFrameLength = audioStream.getFrameLength();
        audioLength = audioFrameLength * audioFormat.getFrameSize();

        dataObj.source = this.source;
        dataObj.frameLength = audioStream.getFrameLength();
        dataObj.audioFormat.encoding = audioFormat.getEncoding().toString();
        dataObj.audioFormat.channels = audioFormat.getChannels();
        dataObj.audioFormat.frameSize = audioFormat.getFrameSize();
        dataObj.audioFormat.sampleRate = audioFormat.getSampleRate();
        dataObj.totalSize = audioLength;
        dataObj.thisPartIdx = 0;
        int nParts = (int) Math.ceil((double) audioLength / (double) AUDIO_STREAM_BUFFER_SIZE);
        partsForRetransmission = new JsonNode[nParts];
        dataObj.partOffsets = new int[nParts];
        dataObj.clipId = clipId;
        
        for (int i = 0; i < nParts; i++) {
        	dataObj.partOffsets[i] = i*AUDIO_STREAM_BUFFER_SIZE;
        }

        int audioReadPos = 0;
        int partCounter = 0;

        // Store clip data
        while (audioReadPos < audioLength) {
            long size = AUDIO_STREAM_BUFFER_SIZE;
            if (size > audioLength - audioReadPos)
                size = audioLength - audioReadPos;
            try {
                dataObj.data = new byte[(int) size];
                audioReadPos += audioStream.read(dataObj.data, 0, (int) size);
                dataObj.thisPartIdx = partCounter;
                JsonNode partMsg = om.convertValue(dataObj, JsonNode.class);
                partsForRetransmission[partCounter] = partMsg;
                partCounter++;
            } catch (IOException e) {
                throw new WavCreationException(e.getLocalizedMessage(), this, e);
            }
        }
        
        try {
            audioStream.close();
        } catch (IOException e) {
            throw new WavCreationException(e.getLocalizedMessage(), this, e);
        }
    }
    
    public void sendWholeClip() {
    	for (JsonNode partMsg : partsForRetransmission) {
            middleware.sendData(partMsg);
    	}
    }
    
    public JsonNode getClipPartForRetransmission(int part) {
	    if (part >= 0 && part < partsForRetransmission.length)
	    	return partsForRetransmission[part];
    	return null;
    }

    @Override
    public double getDuration() {
        return (double) audioFrameLength / (double) audioFormat.getSampleRate();
    }

    @Override
    public void setVolume(float value) {
    	// TODO: middleware set volume...
    	this.desiredVolume = value;
    }

    @Override
    public float getVolume() {
    	return this.desiredVolume;
    }

    @Override
    public void stop() {
        stop = true;
        playing = false;
    	StreamingClipCtrlJSON ctrlMsg = new StreamingClipCtrlJSON("stop", clipId, source);
    	middleware.sendData(om.convertValue(ctrlMsg, JsonNode.class));
    }

    @Override
    public void start(double relTime) {
        play(relTime);
        playing = true;
    }
    
    @Override
    public void play(double relTime) {
        if (stop)
            return;
    	StreamingClipCtrlJSON ctrlMsg = new StreamingClipCtrlJSON("play", clipId, source, relTime);
    	middleware.sendData(om.convertValue(ctrlMsg, JsonNode.class));
    }
}

// Protocol classes:

class StreamingClipJSON {
	public String msgType;
	public String source;
	public String clipId;
	
	public StreamingClipJSON() {}
	public StreamingClipJSON(String msgType) {
		this.msgType = msgType;
	}
}

class StreamingClipDataJSON extends StreamingClipJSON {
	public long frameLength;
	public StreamingClipDataFormatJSON audioFormat;
	public long totalSize;
	public int thisPartIdx;
	public int[] partOffsets;
	public byte[] data;
	
	public StreamingClipDataJSON() {
		super("DATA");
		this.audioFormat = new StreamingClipDataFormatJSON();
	}
}

class StreamingClipDataFormatJSON {
	public String encoding;
	public int channels;
	public int frameSize;
	public float sampleRate;
}

class StreamingClipCtrlJSON extends StreamingClipJSON {
	public String cmd;
	public double floatParam;

	public StreamingClipCtrlJSON(String cmd, String clipId, String source) {
		this(cmd, clipId, source, 0);
	}
	
	public StreamingClipCtrlJSON(String cmd, String clipId, String source, double relTime) {
		super("CTRL");
		this.cmd = cmd;
		this.floatParam = relTime;
		this.clipId = clipId;
		this.source = source;
	}
}

