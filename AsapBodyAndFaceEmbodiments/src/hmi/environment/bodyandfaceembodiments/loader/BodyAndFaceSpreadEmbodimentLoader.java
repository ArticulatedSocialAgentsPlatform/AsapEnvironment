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
package hmi.environment.bodyandfaceembodiments.loader;

import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceSpreadEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class BodyAndFaceSpreadEmbodimentLoader implements EmbodimentLoader
{
    @Getter
    private String id;
    private BodyAndFaceSpreadEmbodiment embodiment;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
        List<BodyAndFaceEmbodiment> outputs = new ArrayList<BodyAndFaceEmbodiment>();
        for (EmbodimentLoader el : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (el.getEmbodiment() instanceof BodyAndFaceEmbodiment)
            {
                outputs.add((BodyAndFaceEmbodiment)el.getEmbodiment());
            }
        }
        
        if(outputs.isEmpty())
        {
            throw new RuntimeException("BodyAndFaceSwitchEmbodimentLoader requires output BodyAndFaceEmbodiments");
        }
        
        CopyEnvironment env = ArrayUtils.getFirstClassOfType(environments, CopyEnvironment.class);
        if (env == null)
        {
            throw new RuntimeException("BodyAndFaceSwitchEmbodimentLoader requires a CopyEnvironment");
        }        
        embodiment = new BodyAndFaceSpreadEmbodiment(id, id, outputs);
        env.addCopyEmbodiment(embodiment);
    }

    @Override
    public void unload()
    {
                
    }    

    @Override
    public BodyAndFaceSpreadEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
