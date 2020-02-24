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

import hmi.animation.Hanim;
import hmi.animation.RenamingXMLMap;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceembodiments.loader.EyelidMorpherLoader;
import hmi.util.ArrayUtils;
import hmi.util.Resources;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import asap.rsbembodiments.RsbBodyAndFaceEmbodiment;
import asap.rsbembodiments.RsbBodyEmbodiment;
import asap.rsbembodiments.RsbEmbodiment;
import asap.rsbembodiments.RsbFaceController;
import asap.rsbembodiments.RsbFaceEmbodiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

/**
 * Loads an RsbBodyAndFaceEmbodiment
 * @author herwinvw
 * 
 */
public class RsbBodyAndFaceEmbodimentLoader implements EmbodimentLoader
{
    @Getter
    private String id;
    private RsbBodyAndFaceEmbodiment embodiment;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private BiMap<String, String> skeletonRenaming = HashBiMap.create();
    private BiMap<String, String> morphRenaming = HashBiMap.create();

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;

        ClockDrivenCopyEnvironment copyEnv = ArrayUtils.getFirstClassOfType(environments, ClockDrivenCopyEnvironment.class);
        RsbEmbodimentLoader ldr = ArrayUtils.getFirstClassOfType(requiredLoaders, RsbEmbodimentLoader.class);

        if (copyEnv == null)
        {
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an ClockDrivenCopyEnvironment");
        }

        if (ldr == null)
        {
            throw new XMLScanException("IpaacaFaceAndBodyEmbodimentLoader requires an IpaacaEmbodimentLoader");
        }
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        
        
        RsbEmbodiment rsbEmb = ldr.getEmbodiment();

        RsbFaceController fc = new RsbFaceController(vhId, rsbEmb, morphRenaming);
        RsbFaceEmbodiment faceEmbodiment = new RsbFaceEmbodiment(id+"-face",  fc);
        RsbBodyEmbodiment bodyEmbodiment = new RsbBodyEmbodiment(id+"-body", vhId, rsbEmb);

        List<String> jointFilter;
        if (!skeletonRenaming.isEmpty())
        {
            jointFilter = ImmutableList.copyOf(skeletonRenaming.values());
        }
        else
        {
            jointFilter = new ImmutableList.Builder<String>().add(Hanim.all_body_joints).add(Hanim.temporomandibular).add(Hanim.r_eyeball_joint)
                    .add(Hanim.l_eyeball_joint).add(Hanim.ROLL_JOINTS).build();
        }
        bodyEmbodiment.initialize(skeletonRenaming, jointFilter);
        faceEmbodiment.initialize();
        
        embodiment = new RsbBodyAndFaceEmbodiment(id, vhId, rsbEmb, faceEmbodiment, bodyEmbodiment);        
        EyelidMorpherLoader eml = ArrayUtils.getFirstClassOfType(requiredLoaders, EyelidMorpherLoader.class);
        if(eml!=null)
        {
            embodiment.setEyelidMorpher(eml.getEmbodiment());
        }
        copyEnv.addCopyEmbodiment(embodiment);

    }

    private BiMap<String, String> getRenamingMap(String mappingFile) throws IOException
    {
        RenamingXMLMap map = new RenamingXMLMap();
        BufferedInputStream s = new Resources("").getInputStream(mappingFile);
        if(s==null)
        {
            throw new XMLScanException("Cannot find renaming file in IpaacaFaceAndBodyEmbodiment "+mappingFile);
        }
        map.readXML(new XMLTokenizer(s));
        return map.getRenamingMap();
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("renaming"))
        {
            attrMap = tokenizer.getAttributes();

            attrMap = tokenizer.getAttributes();
            String skelRenamingFile = adapter.getOptionalAttribute("skeletonRenamingFile", attrMap);
            if (skelRenamingFile != null)
            {
                skeletonRenaming = getRenamingMap(skelRenamingFile);
            }

            String morphsRenamingFile = adapter.getOptionalAttribute("morphRenamingFile", attrMap);
            if (morphsRenamingFile != null)
            {
                morphRenaming = getRenamingMap(morphsRenamingFile);
            }
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
        tokenizer.takeEmptyElement("renaming");
    }

    @Override
    public void unload()
    {

    }

    @Override
    public RsbBodyAndFaceEmbodiment getEmbodiment()
    {
        return embodiment;
    }

}
