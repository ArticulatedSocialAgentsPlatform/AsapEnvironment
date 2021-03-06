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
package hmi.faceembodiments.loader;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceembodiments.SimpleFaceEmbodiment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;

public class SimpleFaceEmbodimentLoader implements EmbodimentLoader
{
    @Getter
    private String id;
    
    private SimpleFaceEmbodiment embodiment;
    
    public SimpleFaceEmbodimentLoader(String id, SimpleFaceEmbodiment embodiment)
    {
        this.id = id;
        this.embodiment = embodiment;
    }
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;        
    }

    @Override
    public void unload()
    {
                
    }
    
    public SimpleFaceEmbodiment getEmbodiment()
    {
        return embodiment;
    }
}
