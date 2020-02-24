/*******************************************************************************
 * Copyright (C) 2009-2020 Human Media Interaction, University of Twente, the Netherlands
 *
 * This file is part of the Articulated Social Agents Platform BML realizer (ASAPRealizer).
 *
 * ASAPRealizer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ASAPRealizer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ASAPRealizer.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package hmi.audioenvironment;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plays a wav file
 * 
 * @author welberge
 */
public class ClipWav implements Wav
{
    private final AudioInputStream audioStream;
    private final AudioFormat audioFormat;
    private final long audioLength;
    private final long audioFrameLength;
    private int audioReadPos = 0;
    private byte[] audioData = null;
    private static final int AUDIO_READ_BUFFER_SIZE = 65535;
    //private static final double AUDIOPOSITION_UPDATE_DELTA = 0.1d;// allow a
    // 3 frame
    // delay
    // with
    // graphics
    // running
    // at 30fps
    private static final double AUDIOPOSITION_UPDATE_DELTA = 0.066d; 
    private Clip outputLine;

    private final Object clipLock = new Object();
    public volatile boolean playing = false;
    public volatile boolean stop = false;
    private boolean firstPlay = false;

    private float desiredVolume = 50;
    private final boolean syncAudio = false;
    //private final boolean syncAudio = true;

    private static Logger logger = LoggerFactory.getLogger(ClipWav.class.getName());

    public ClipWav(InputStream inputStream) throws WavCreationException
    {
        try
        {
            audioStream = AudioSystem.getAudioInputStream(inputStream);
        }
        catch (UnsupportedAudioFileException e1)
        {
            throw new WavCreationException(e1.getLocalizedMessage(), this, e1);
        }
        catch (IOException e1)
        {
            throw new WavCreationException(e1.getLocalizedMessage(), this, e1);
        }
        audioFrameLength = audioStream.getFrameLength();
        audioLength = audioStream.getFrameLength() * audioStream.getFormat().getFrameSize();
        audioData = new byte[(int) audioLength];
        audioFormat = audioStream.getFormat();
        audioReadPos = 0;

        while (audioReadPos < audioLength)
        {
            long size = AUDIO_READ_BUFFER_SIZE;
            if (size > audioLength - audioReadPos)
                size = audioLength - audioReadPos;
            try
            {
                audioReadPos += audioStream.read(audioData, audioReadPos, (int) size);
            }
            catch (IOException e)
            {
                throw new WavCreationException(e.getLocalizedMessage(), this, e);
            }
        }
        try
        {
            audioStream.close();
        }
        catch (IOException e)
        {
            throw new WavCreationException(e.getLocalizedMessage(), this, e);
        }
    }

    @Override
    public double getDuration()
    {
        logger.debug("WAV AUDIO FRAME LENGTH: " + audioFrameLength);
        logger.debug("WAV SAMPLERATE: " + audioFormat.getSampleRate());
        return (double) audioFrameLength / (double) audioFormat.getSampleRate();
    }

    public void setVolume(float value)
    {
        synchronized (clipLock)
        {
            desiredVolume = value;
            if (outputLine == null)
            {
                return;
            }
            // from documentation:
            // linearScalar = pow(10.0, gainDB/20.0)
            logger.debug("Setting wav volume to {}", value);
            FloatControl volume = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
            float minA = (float) Math.pow(10, volume.getMinimum() / 20);
            float maxA = (float) Math.pow(10, volume.getMaximum() / 20);
            float currentA = minA + (maxA - minA) * (value / 100f);
            float gainDb = (float) (20 * Math.log(currentA) / Math.log(10));
            volume.setValue(gainDb);            
        }
    }


    @Override
    public float getVolume() 
    {
        synchronized (clipLock)
        {
            if (outputLine != null)
            {
                FloatControl volume = (FloatControl) outputLine.getControl(FloatControl.Type.MASTER_GAIN);
                return volume.getValue();
            }
            else
            {
                logger.warn("Attempting to get parameter: 'volume' on null output line!");
                return desiredVolume;
            }
        }
    }

    private void setPlaying(boolean play)
    {
        playing = play;
    }

    private static class OutputLineCloseThread extends Thread
    {
        private Clip outputLine;
        public OutputLineCloseThread(Clip outputLine)
        {
            this.outputLine = outputLine;
        }
        
        @Override
        public void run()
        {
            outputLine.close();
        }        
    }
    
    @Override
    public void stop()
    {
        stop = true;
        playing = false;
        logger.debug("Wav stop");
        synchronized (clipLock)
        {
            if (outputLine != null)
            {
                logger.debug("Stop output line");
                outputLine.stop();                
                //XXX: hack to prevent huge delays in output line closing
                new OutputLineCloseThread(outputLine).start();
                outputLine = null;
            }
        }
    }

    @Override
    public void start(double relTime)
    {
        setPlaying(true);
    }
    
    /**
     * Play
     * 
     * @param relTime relative to start of Wav
     * @throws WavPlayException
     */
    @Override
    public void play(double relTime) throws WavPlayException
    {
        if (stop)
            return;
        synchronized (clipLock)
        {
            /*
            if (outputLine != null)
            {
                System.out.println("outputLine time: "+outputLine.getMicrosecondPosition() * 1E-6+", relTime:"+relTime); 
            }
            */
            if (outputLine == null)
            {
                // create new line
                Clip.Info info = new Clip.Info(Clip.class, audioFormat);
                if (!AudioSystem.isLineSupported(info))
                {
                    throw new WavPlayException("AudioSystem line unsupported for " + info.toString(), this);
                }
                try
                {
                    outputLine = (Clip) AudioSystem.getLine(info);
                    outputLine.open(audioFormat, audioData, 0, audioData.length);
                    //System.out.println("Outputline type: "+outputLine.getClass());
                    setVolume(desiredVolume);
                }
                catch (LineUnavailableException ex)
                {
                    throw new WavPlayException(ex.getLocalizedMessage(), this, ex);
                }
                // if(syncAudio)
                {
                    outputLine.setMicrosecondPosition((long) (relTime * 1E6));
                    //System.out.println("setting pos: "+relTime+"s, "+(long)(relTime * 1E6));
                }
                outputLine.start();
                firstPlay = true;
            }            
            else if (Math.abs(outputLine.getMicrosecondPosition() * 1E-6 - relTime) > AUDIOPOSITION_UPDATE_DELTA || firstPlay)
            {
                
                if (syncAudio)
                {
                    if (!firstPlay)
                    {
                        System.err.println("Wav asynchrony:" + " relTime=" + relTime + ", pos=" + outputLine.getMicrosecondPosition() * 1E-6+ "s, "
                                + (outputLine.getMicrosecondPosition() * 1E-6 - relTime) + "s, "
                                + (int) ((outputLine.getMicrosecondPosition() * 1E-6 - relTime) * audioFormat.getSampleRate() + 0.5)
                                + "frames, skipping to correct play position");
                    }
                    outputLine.setMicrosecondPosition((long) (relTime * 1E6));
                }
                firstPlay = false;
            }
        }
    }
}
