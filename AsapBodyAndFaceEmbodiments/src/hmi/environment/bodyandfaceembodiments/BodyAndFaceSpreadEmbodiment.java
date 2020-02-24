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
package hmi.environment.bodyandfaceembodiments;

import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.animationembodiments.VJointSpreadEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.faceembodiments.FaceSpreadEmbodiment;

import java.util.Collection;

import lombok.Getter;

/**
 * Spreads the rotation from one input over multiple outputs. The input is constructed upon creation.
 * @author hvanwelbergen
 *
 */
public class BodyAndFaceSpreadEmbodiment implements FaceEmbodiment, SkeletonEmbodiment
{
    @Getter
    private final String id;
    private VJointSpreadEmbodiment vse;
    private FaceSpreadEmbodiment fse;
    
    public BodyAndFaceSpreadEmbodiment(String id, String input, Collection<BodyAndFaceEmbodiment> outputs)
    {
        this.id = id;
        if(outputs.isEmpty())
        {
            throw new RuntimeException("Cannot construct BodyAndFaceSpreadEmbodiment with empty output list");
        }
        vse = VJointSpreadEmbodiment.createFromEmbodiments(id,input, outputs);
        fse = FaceSpreadEmbodiment.createFromEmbodiments(id,input, outputs);
    }

    @Override
    public void copy()
    {
        vse.copy();
        fse.copy();        
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return vse.getAnimationVJoint();
    }

    @Override
    public FaceController getFaceController()
    {
        return fse.getFaceController();
    }
}
