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

import org.junit.Test;

/**
 * Integration tests for the AsapRealizerIpaacaStatusListener
 * @author herwinvw
 *
 */
public class AsapRealizerIpaacaStatusListenerIntegrationTest
{
    AsapRealizerIpaacaStatusListener listener = new AsapRealizerIpaacaStatusListener();
    
    @Test(timeout=1000)
    public void testWaitForStatus() throws InterruptedException
    {
        AsapRealizerIpaacaStatusInformer informer = new AsapRealizerIpaacaStatusInformer();
        informer.setStatus("initialized");
        listener.waitForStatus("initialized");
        informer.close();
    }
    
    @Test(expected=TimeoutException.class)
    public void testWaitForStatusTimeout() throws InterruptedException, TimeoutException
    {
        AsapRealizerIpaacaStatusInformer informer = new AsapRealizerIpaacaStatusInformer();
        informer.setStatus("initialized");
        listener.waitForStatus("started",500, TimeUnit.MILLISECONDS);
        informer.close();        
    }
}
