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

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import lombok.extern.slf4j.Slf4j;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

/**
 * Plays back wav files using LWJGL's implementation of openal.
 */
@Slf4j
public class LWJGLJoalWav implements Wav
{
    private IntBuffer buffer = BufferUtils.createIntBuffer(1);
    private final int source;
    private FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
    private FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
    private FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
    private FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
    private FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f });
    private float volume = 50f;
    private double duration = 0;
    private boolean removeSourceOnStop = false;
    private String syncObject;

    /*
     * private void checkForOpenALErrorInPlanning() throws WavCreationException
     * {
     * int error = AL10.alGetError();
     * if (error != AL10.AL_NO_ERROR)
     * {
     * throw new WavCreationException("AL10 error " + error, this);
     * }
     * }
     */

    private void setup(InputStream inputStream) throws WavCreationException
    {
        // any buffer that has data added, must be flipped to establish its position and limits
        sourcePos.flip();
        sourceVel.flip();
        listenerPos.flip();
        listenerVel.flip();
        listenerOri.flip();

        AL10.alGenBuffers(buffer);
        // checkForOpenALErrorInPlanning();
        WaveData waveFile = WaveData.create(inputStream);
        if (waveFile == null)
        {
            throw new WavCreationException("Cannot create WaveDate for inputStream " + inputStream, this);
        }
        AL10.alBufferData(buffer.get(0), waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        int sizeInBytes = AL10.alGetBufferi(buffer.get(0), AL10.AL_SIZE);
        int channels = AL10.alGetBufferi(buffer.get(0), AL10.AL_CHANNELS);
        int bits = AL10.alGetBufferi(buffer.get(0), AL10.AL_BITS);
        long lengthInSamples = sizeInBytes * 8 / (channels * bits);
        int frequency = AL10.alGetBufferi(buffer.get(0), AL10.AL_FREQUENCY);

        duration = (double) lengthInSamples / (double) frequency;

        syncObject = ("" + source).intern();
        synchronized (syncObject)
        {
            AL10.alSourcef(source, AL10.AL_PITCH, 1.0f);
            AL10.alSourcef(source, AL10.AL_GAIN, volume/100f);
            AL10.alSource(source, AL10.AL_POSITION, sourcePos);
            AL10.alSource(source, AL10.AL_VELOCITY, sourceVel);
            // checkForOpenALErrorInPlanning();

            AL10.alListener(AL10.AL_POSITION, listenerPos);
            AL10.alListener(AL10.AL_VELOCITY, listenerVel);
            AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
        }
    }

    public LWJGLJoalWav(InputStream inputStream) throws WavCreationException
    {
        IntBuffer src = BufferUtils.createIntBuffer(1);
        AL10.alGenSources(src);
        source = src.get(0);
        removeSourceOnStop = true;
        setup(inputStream);
    }

    public LWJGLJoalWav(InputStream inputStream, int source) throws WavCreationException
    {
        this.source = source;
        setup(inputStream);
    }

    @Override
    public void setVolume(float vol)
    {
        synchronized (syncObject)
        {
            volume = vol;
            AL10.alSourcef(source, AL10.AL_GAIN, volume / 100f);
        }
    }

    @Override
    public float getVolume()
    {
        return volume;
    }

    @Override
    public void stop()
    {
        synchronized (syncObject)
        {
            if (buffer.get() == AL10.alGetSourcei(source, AL10.AL_BUFFER))
            {
                AL10.alSourceStop(source); // only stop if source isn't already playing something else
            }

            if (removeSourceOnStop)
            {
                AL10.alDeleteSources(source);
            }
            AL10.alDeleteBuffers(buffer);
        }
    }

    public void start(double relTime)
    {
        synchronized (syncObject)
        {
            log.debug("relative startTime: " + relTime);
            AL10.alSourceStop(source);
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer.get(0));
            AL10.alSourcef(source, AL10.AL_GAIN, volume / 100f);
            // AL10.alSourcef(source, AL11.AL_SEC_OFFSET, (float)relTime);
            AL10.alSourcePlay(source);
        }
    }

    @Override
    public void play(double relTime) throws WavPlayException
    {

    }

    @Override
    public double getDuration()
    {
        return duration;
    }
}
