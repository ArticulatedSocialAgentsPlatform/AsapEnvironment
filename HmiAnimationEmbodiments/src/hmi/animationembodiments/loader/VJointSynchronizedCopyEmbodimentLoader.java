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
package hmi.animationembodiments.loader;

import java.io.IOException;

import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.animationembodiments.VJointSynchronizedCopyEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

/**
 * Loads a VJointSynchronizedCopyEmbodiment from XML
 * @author hvanwelbergen
 * 
 */
public class VJointSynchronizedCopyEmbodimentLoader implements EmbodimentLoader
{
    private String id;
    private VJointSynchronizedCopyEmbodiment embodiment;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        SkeletonEmbodiment src = null;

        for (EmbodimentLoader el : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (el.getEmbodiment() instanceof SkeletonEmbodiment)
            {
                src = (SkeletonEmbodiment) el.getEmbodiment();
            }
        }
        if (src == null)
        {
            throw new RuntimeException(
                    "VJointSynchronizedCopyEmbodimentLoader requires an EmbodimentLoader containing a SkeletonEmbodiment");
        }
        embodiment = new VJointSynchronizedCopyEmbodiment(id, src.getAnimationVJoint());
    }

    @Override
    public void unload()
    {

    }

    @Override
    public Embodiment getEmbodiment()
    {
        return embodiment;
    }

}
