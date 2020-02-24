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
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.xml.XMLTokenizer;

import java.io.IOException;


/** Take care of its own loading from XML. Actually, nothing in XML -- it just needs the SkeletonEmbodiment as input */
public class MixedSkeletonEmbodimentLoader implements EmbodimentLoader
{

    private MixedSkeletonEmbodiment mse = null;

    @Override
    public void unload()
    {
    }

    private String id = "";

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
    	throws IOException
    {
        id = loaderId;

        SkeletonEmbodiment se = null;

        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof SkeletonEmbodiment)
            {
                se = (SkeletonEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
        }
        if (se == null)
        {
            throw new RuntimeException(
                    "MixedSkeletonEmbodimentLoader requires an EmbodimentLoader containing a SkeletonEmbodiment (e.g., HmiRenderBodyEmbodiment)");
        }

        mse = new MixedSkeletonEmbodiment();

        mse.setAnimationVJoint(se.getAnimationVJoint());
        mse.setNextVJoint(se.getAnimationVJoint().copyTree("next-"));
        mse.setCurrentVJoint(se.getAnimationVJoint().copyTree("curr-"));
        mse.setPreviousVJoint(se.getAnimationVJoint().copyTree("prev-"));
    }

    @Override
    public Embodiment getEmbodiment()
    {
        return mse;
    }

}
