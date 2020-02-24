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
package hmi.jcomponentenvironment;

import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import hmi.environmentbase.Environment;
import lombok.Getter;
import lombok.Setter;

/**
 * Manages and constructs a bunch of JComponentEmbodiments
 * @author hvanwelbergen
 *
 */
public class JComponentEnvironment implements Environment
{
    private ConcurrentHashMap<String, JComponentEmbodiment> componentMap = new ConcurrentHashMap<>();
    private volatile boolean shutdown = false;

    public void registerComponent(String id, JComponent jc)
    {
        JComponentEmbodiment jce = new JComponentEmbodiment();
        jce.setId(id);
        jce.setMasterComponent(jc);
        componentMap.put(id, jce);
    }
    
    public JComponentEmbodiment getJComponentEmbodiment(String id)
    {
        return componentMap.get(id);
    }

    @Getter
    @Setter
    private String id = "";

    @Override
    public void requestShutdown()
    {
        shutdown = true;
    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

}
