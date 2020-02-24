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

import hmi.environmentbase.StatusInformer;
import ipaaca.LocalMessageIU;
import ipaaca.OutputBuffer;

/**
 * Submits status as a MessageIU on a specified category and under the key specified by statusKey 
 * @author herwinvw
 *
 */
public class IpaacaStatusInformer implements StatusInformer
{
    private final OutputBuffer outBuffer;
    private final String statusKey;
    private final String category;
    
    public IpaacaStatusInformer(String category, String statusKey)
    {
        this(category, statusKey, "default");
    }
    
    public IpaacaStatusInformer(String category, String statusKey, String channel)
    {
        this.category = category;
        this.statusKey = statusKey;
        outBuffer = new OutputBuffer("IpaacaStatusInformer", channel);
    }
    
    @Override
    public void setStatus(String status)
    {
        LocalMessageIU message = new LocalMessageIU(category);
        message.getPayload().put(statusKey, status);
        outBuffer.add(message);
    }
    
    @Override
    public void close()
    {
        outBuffer.close();
    }
}
