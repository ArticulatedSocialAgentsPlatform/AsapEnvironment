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
package asap.rsbembodiments.loader;

import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;
import asap.rsbembodiments.RsbFaceController;
import asap.rsbembodiments.RsbFaceEmbodiment;

/**
 * Loads an RsbFaceEmbodiment, requires an RsbEmbodiment
 * @author Herwin
 *
 */
public class RsbFaceEmbodimentLoader implements EmbodimentLoader
{
    private RsbFaceEmbodiment embodiment;

    @Getter
    private String id;
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        this.id = loaderId;
        
        ClockDrivenCopyEnvironment copyEnv = ArrayUtils.getFirstClassOfType(environments, ClockDrivenCopyEnvironment.class);
        RsbEmbodimentLoader ldr = ArrayUtils.getFirstClassOfType(requiredLoaders, RsbEmbodimentLoader.class); 
        
        if(copyEnv == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }
        
        if(ldr == null)
        {
            throw new XMLScanException("IpaacaFaceEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        RsbFaceController fc = new RsbFaceController(vhId, ldr.getEmbodiment());
        embodiment = new RsbFaceEmbodiment(vhId, fc);
        embodiment.initialize();
        copyEnv.addCopyEmbodiment(embodiment);
    }

    @Override
    public void unload()
    {
                
    }

    @Override
    public RsbFaceEmbodiment getEmbodiment()
    {
        return embodiment;
    }
}
