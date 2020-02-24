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
import hmi.faceanimation.FaceController;
import lombok.Getter;

/**
 * Combination of VJoint and FaceController steering in a single embodiment
 * @author hvanwelbergen
 *
 */
public class SimpleBodyAndFaceEmbodiment implements BodyAndFaceEmbodiment
{
    private final VJoint aniJoint;
    private final FaceController  faceController;
    @Getter
    private final String id;
   
    public SimpleBodyAndFaceEmbodiment(String id, VJoint aniJoint, FaceController fc)
    {
        this.aniJoint = aniJoint;
        this.faceController = fc;
        this.id = id;
    }
    
    @Override
    public FaceController getFaceController()
    {
        return faceController;
    }

    @Override
    public void copy()
    {
        faceController.copy();
        aniJoint.calculateMatrices();        
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return aniJoint;
    }
    
}
