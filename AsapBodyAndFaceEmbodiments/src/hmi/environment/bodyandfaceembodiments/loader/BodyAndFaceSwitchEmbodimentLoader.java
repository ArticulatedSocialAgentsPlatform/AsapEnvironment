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
import hmi.environment.bodyandfaceembodiments.BodyAndFaceSwitchEmbodiment;
import hmi.environmentbase.CompoundLoader;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.InputsLoader;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;

/**
 * Loader for the BodyAndFaceSwitchEmbodiment
 * @author hvanwelbergen
 *
 */
public class BodyAndFaceSwitchEmbodimentLoader implements EmbodimentLoader, CompoundLoader
{
    @Getter
    private String id;
    private BodyAndFaceSwitchEmbodiment embodiment;
    private List<SimpleBodyAndFaceEmbodimentLoader> parts;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        BodyAndFaceEmbodiment output = null;
        for (EmbodimentLoader el : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (el.getEmbodiment() instanceof BodyAndFaceEmbodiment)
            {
                output = (BodyAndFaceEmbodiment) el.getEmbodiment();
            }            
        }
        CopyEnvironment env = ArrayUtils.getFirstClassOfType(environments, CopyEnvironment.class);
        if (env == null)
        {
            throw new RuntimeException("BodyAndFaceSwitchEmbodimentLoader requires a CopyEnvironment");
        }
        
        if (output == null)
        {
            throw new RuntimeException("BodyAndFaceSwitchEmbodimentLoader requires an EmbodimentLoader containing a BodyAndFaceEmbodiment for its output");
        }
        
        
        InputsLoader inputs = new InputsLoader();        
        while (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            switch (tag)
            {
            case InputsLoader.XMLTAG:
                inputs.readXML(tokenizer);                
                break;
            default:
                throw new XMLScanException("Invalid tag " + tag);
            }
        }
       
        embodiment = new BodyAndFaceSwitchEmbodiment(loaderId, inputs.getIds(), output);
        
        parts = new ArrayList<SimpleBodyAndFaceEmbodimentLoader>();
        for(String partId:inputs.getIds())
        {
            parts.add(new SimpleBodyAndFaceEmbodimentLoader(partId,embodiment.getInput(partId)));
        }        
        env.addCopyEmbodiment(embodiment);
    }

    @Override
    public void unload()
    {
                
    }

    @Override
    public Collection<? extends Loader> getParts()
    {
        return parts;
    }

    @Override
    public BodyAndFaceSwitchEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
