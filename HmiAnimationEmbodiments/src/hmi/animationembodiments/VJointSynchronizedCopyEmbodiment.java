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
package hmi.animationembodiments;

import hmi.animation.VJoint;
import hmi.animation.VObjectTransformCopier;
import hmi.util.AnimationSync;

/**
 * Creates a new VJoint that is a copy of a source joint. The copy action copies source to this joint, 
 * synchronized by AnimationSync.
 * @author hvanwelbergen
 */
public class VJointSynchronizedCopyEmbodiment implements SkeletonEmbodiment
{
    private final VJoint dst;
    private final String id;
    private final VObjectTransformCopier votc;
    
    public VJointSynchronizedCopyEmbodiment(String id, VJoint src)
    {
        this.id = id;
        dst = src.copyTree(id);
        votc = VObjectTransformCopier.newInstanceFromVJointTree(src,dst,"T1R");               
    }
    
    @Override
    public void copy()
    {
        synchronized(AnimationSync.getSync())
        {
            votc.copyConfig();
        }
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return dst;
    }
}
