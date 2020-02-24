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

import hmi.environmentbase.Environment;
import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Dennis Reidsma
 */
public class AudioEnvironment implements Environment
{
    @Getter
    @Setter
    private String id = "audioenvironment";

    private Logger logger = LoggerFactory.getLogger(AudioEnvironment.class.getName());

    private SoundManager soundManager = null;
    private String soundmanagerType = "LJWGL_JOAL";

    protected volatile boolean shutdownPrepared = false;

    public AudioEnvironment()
    {
    }

    public AudioEnvironment(String type)
    {
        soundmanagerType = type;
    }

    public void init(SoundManager sm)
    {
        soundManager = sm;
        sm.init();
        soundmanagerType = sm.getClass().toString();
    }
    
    public void init()
    {
        if (soundmanagerType.equals("LJWGL_JOAL"))
        {
            soundManager = new LWJGLJoalSoundManager();
            soundManager.init();
        }
        else if (soundmanagerType.equals("WAV_CLIP"))
        {
            soundManager = new ClipSoundManager();
            soundManager.init();
        }
    }

    public void shutdown()
    {
        logger.info("Shutdown of Audioenvironment...");
        if (soundManager != null) soundManager.shutdown();
        logger.info("Shutdown of AudioEnvironment finished");
    }

    public SoundManager getSoundManager()
    {
        return soundManager;
    }

    @Override
    public void requestShutdown()
    {
        shutdown();
        shutdownPrepared = true;
    }

    @Override
    public boolean isShutdown()
    {
        return shutdownPrepared;
    }
}
