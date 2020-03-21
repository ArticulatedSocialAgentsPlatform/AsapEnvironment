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
import static org.junit.Assert.*;

import java.nio.IntBuffer;

import hmi.util.Resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

/**
 * Integration tests (with actual wav files) for the LWGLJoalWav
 * @author Herwin
 *
 */
@Ignore("The jenkins buildserver doesn't have a sound card/drivers installed so these tests throw an exception: org.lwjgl.LWJGLException: Could not locate OpenAL library. Probably could add some conditional assumeTrue check to these testcases to disable on server")
public class LWGLJoalWavIntegrationTest
{
    @Before
    public void setup() throws LWJGLException
    {
        AL.create(null, 15, 22050, true);
    }
    
    @After
    public void tearDown()
    {
        AL.destroy();
    }
    private Resources res = new Resources("");
    
    private int getSourceState(int source)
    {
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
    }
    
    @Test
    public void testAnonymousSource() throws WavCreationException, InterruptedException
    {
        //doesn't really test anything, but you should hear "Well hello mister fanycpants" when running this        
        LWJGLJoalWav wav = new LWJGLJoalWav(res.getInputStream("FancyPants.wav"));
        wav.start(0);
        Thread.sleep(5000);
        wav.stop();        
    }
        
    @Test 
    public void testWithSource() throws InterruptedException, WavCreationException
    {
        IntBuffer source = BufferUtils.createIntBuffer(1);
        AL10.alGenSources(source);
        LWJGLJoalWav wav = new LWJGLJoalWav(res.getInputStream("FancyPants.wav"),source.get(0));
        wav.start(0);
        assertEquals(AL10.AL_PLAYING,getSourceState(source.get(0)));
        Thread.sleep(500);
        wav.stop();
        assertEquals(AL10.AL_STOPPED,getSourceState(source.get(0)));
    }
    
    @Test (expected=WavCreationException.class)
    public void testInvalidSource() throws WavCreationException
    {
        IntBuffer source = BufferUtils.createIntBuffer(1);
        AL10.alGenSources(source);
        new LWJGLJoalWav(res.getInputStream("invalid.wav"),source.get(0));
    }
}
