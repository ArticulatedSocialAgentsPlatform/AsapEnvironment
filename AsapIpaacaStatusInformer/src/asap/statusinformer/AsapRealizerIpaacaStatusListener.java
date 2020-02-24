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
package asap.statusinformer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableSet;

import ipaaca.AbstractIU;
import ipaaca.HandlerFunctor;
import ipaaca.IUEventType;
import ipaaca.InputBuffer;
import ipaaca.util.communication.FutureIUs;

/**
 * Utitility to receive and wait for specific AsapRealizer status messages (e.g. to wait till AsapRealizer is fully started).
 * @author hvanwelbergen
 *
 */
public class AsapRealizerIpaacaStatusListener
{
    private final FutureIUs futures;
    private final InputBuffer inBuffer;
    private AtomicReference<String> status = new AtomicReference<>("initialized");
    
    public AsapRealizerIpaacaStatusListener()
    {
        inBuffer = new InputBuffer("IpaacaStatusListener", ImmutableSet.of(AsapRealizerIpaacaStatus.CATEGORY));
        inBuffer.registerHandler(new HandlerFunctor()
        {
            @Override
            public void handle(AbstractIU iu, IUEventType type, boolean local)
            {
                status.set(iu.getPayload().get(AsapRealizerIpaacaStatus.KEY));                
            }
        
        });
        futures = new FutureIUs(AsapRealizerIpaacaStatus.CATEGORY,AsapRealizerIpaacaStatus.KEY);
    }
    
    public String getStatus()
    {
        return status.get();
    }
    
    public void waitForStatus(String status) throws InterruptedException
    {
        futures.take(status);
    }
    
    public void waitForStatus(String status, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
    {
       if(futures.take(status, timeout, unit)==null)
       {
           throw new TimeoutException();
       }
    }
    
    public void close()
    {
        futures.close();
        inBuffer.close();
    }
}