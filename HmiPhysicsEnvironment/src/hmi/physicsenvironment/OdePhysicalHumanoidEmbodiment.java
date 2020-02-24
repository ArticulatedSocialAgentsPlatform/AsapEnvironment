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
package hmi.physicsenvironment;

import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.assembler.PhysicalHumanoidAssembler;
import hmi.util.Resources;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** 
 * Embodiment for a single OdePhysicalHumanoid
 * Take care of its own loading from XML. 
 */
@Slf4j
public class OdePhysicalHumanoidEmbodiment implements EmbodimentLoader, Embodiment
{

    // some parameters for caching during XML loading
    private OdePhysicsEnvironment ope = null;
    private PhysicalHumanoid pHuman;
    private MixedSkeletonEmbodiment mse;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    @Override
    public void unload()
    {
        log.debug("Removing VH from Physics");
        // remove from physics
        ope.clearPhysicalHumanoid(pHuman);
    }

    @Getter
    @Setter
    private String id = "";

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException
    {
        id = loaderId;

        for (Environment e : environments)
        {
            if (e instanceof OdePhysicsEnvironment) ope = (OdePhysicsEnvironment) e;
        }
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() 
                    instanceof MixedSkeletonEmbodiment) mse = (MixedSkeletonEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
        }
        if (ope == null)
        {
            throw new RuntimeException("OdePhysicalEmbodiment requires an Environment of type OdePhysicalEnvironment");
        }
        if (mse == null)
        {
            throw new RuntimeException("OdePhysicalEmbodiment requires an EmbodimentLoader containing a MixedSkeletonEmbodiment");
        }

        // read remaining mixed systems from tokenizer
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer, vhId);
        }

    }

    protected void readSection(XMLTokenizer tokenizer, String vhId) throws IOException
    {
        if (tokenizer.atSTag("PhysicalHumanoidDef"))
        {
            HashMap<String, String> attrMap2 = tokenizer.getAttributes();
            String msFile = adapter.getRequiredAttribute("filename", attrMap2, tokenizer);
            String msResources = adapter.getRequiredAttribute("resources", attrMap2, tokenizer);

            pHuman = ope.createPhysicalHumanoid(vhId + "_" + getId());
            PhysicalHumanoidAssembler psa = new PhysicalHumanoidAssembler(mse.getCurrentVJoint(), pHuman);
            psa.readXML(new Resources(msResources).getReader(msFile));
            tokenizer.takeSTag("PhysicalHumanoidDef");
            tokenizer.takeETag("PhysicalHumanoidDef");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

    public PhysicalHumanoid getPhysicalHuman()
    {
        return pHuman;
    }
}
