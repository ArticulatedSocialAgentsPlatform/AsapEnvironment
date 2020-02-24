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
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

/**
 * SoundManager that creates LWJGLJoalWav wavs
 * @author hvanwelbergen
 *
 */
public class LWJGLJoalSoundManager implements SoundManager
{
    private Map<String, IntBuffer> sourceMap = new HashMap<String, IntBuffer>();

    @Override
    public void init()
    {
        try
        {
            AL.create(null, 15, 22050, true);            
        }
        catch (LWJGLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Wav createWav(InputStream inputStream, String sourceId) throws WavCreationException
    {
        IntBuffer source = sourceMap.get(sourceId);
        if (source == null)
        {
            source = BufferUtils.createIntBuffer(1);
            AL10.alGenSources(source);
            
            /*
            int error = AL10.alGetError();
            if (error != AL10.AL_NO_ERROR)
            {
                throw new WavCreationException("AL10 error " + error, null);
            }
            */
            sourceMap.put(sourceId, source);
        }
        return new LWJGLJoalWav(inputStream, source.get(0));
    }

    @Override
    public Wav createWav(InputStream inputStream) throws WavCreationException
    {
        return new LWJGLJoalWav(inputStream);
    }

    @Override
    public void shutdown()
    {
        for (IntBuffer source : sourceMap.values())
        {
            AL10.alDeleteSources(source);
        }
        AL.destroy();
    }

}
