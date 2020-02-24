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
package asap.rsbworldenvironment;

import hmi.animation.VJoint;
import hmi.environmentbase.Environment;
import hmi.util.ClockListener;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rsb.AbstractDataHandler;
import rsb.Factory;
import rsb.InitializeException;
import rsb.Listener;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObject;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObjects;

import com.google.common.primitives.Floats;
/**
 * Manages world objects that are sent from an external rendering environment through rsb.
 * (note that this class has been suffixed with an arbitrary letter 'C' to differentiate it from the auto-generated protobuf generatedsrc/Rsbworldenvironment.java file)
 * 
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class RsbWorldEnvironmentC implements ClockListener, Environment
{
    private final WorldObjectManager woManager;

    public static final String SCENEINFO_SCOPE = "/asap/sceneinfo";
    private volatile boolean shutdown = false;
    private final Listener listener;

    @Getter
    @Setter
    private String id = "";

    public RsbWorldEnvironmentC(WorldObjectManager wm)
    {
        woManager = wm;
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBWorldObjects.getDefaultInstance()));
        
        Factory factory = Factory.getInstance();
        try
        {
            listener = factory.createListener(SCENEINFO_SCOPE);
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
        try
        {

            listener.addHandler(new AbstractDataHandler<RSBWorldObjects>()
            {
                @Override
                public void handleEvent(RSBWorldObjects wos)
                {
                    for(RSBWorldObject wo: wos.getWorldObjectsList())
                    {
                        setWorldObject(wo.getObjectId(), Floats.toArray(wo.getPositionList()));
                    }                    
                }
            }, true);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        try
        {
            listener.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }        
    }

    private void setWorldObject(String name, float[] pos)
    {
        WorldObject wo = woManager.getWorldObject(name);
        VJoint vj = null;
        if (wo == null)
        {
            vj = new VJoint(name, name);
            wo = new VJointWorldObject(vj);
            woManager.addWorldObject(name, wo);
        }
        wo.setTranslation(pos);
    }

    @Override
    public void initTime(double initTime)
    {

    }

    @Override
    public void time(double currentTime)
    {

    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

    @Override
    public void requestShutdown()
    {
        try
        {
            listener.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("",e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
        }
        shutdown = true;
    }

}
