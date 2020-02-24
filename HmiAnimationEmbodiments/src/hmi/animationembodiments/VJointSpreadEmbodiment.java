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

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import lombok.Getter;

/**
 * Spreads the rotation of the input joint over the output joints. The input joint is constructed on creation.
 * @author hvanwelbergen
 * 
 */
public class VJointSpreadEmbodiment implements SkeletonEmbodiment
{
    @Getter
    private final String id;
    private final VJoint inputJoint;
    private final Collection<VJoint> outputJoints;
    private Collection<VObjectTransformCopier> copiers;

    public static VJointSpreadEmbodiment createFromEmbodiments(String id, String input, Collection<? extends SkeletonEmbodiment> outputs)
    {
        return new VJointSpreadEmbodiment(id, input, Collections2.transform(outputs, new Function<SkeletonEmbodiment, VJoint>()
        {
            @Override            
            public VJoint apply(SkeletonEmbodiment se)
            {
                return se.getAnimationVJoint();
            }
        }));
    }

    public VJointSpreadEmbodiment(String id, String input, Collection<VJoint> outputs)
    {
        this.id = id;
        if (outputs.isEmpty())
        {
            throw new RuntimeException("Cannot construct VJointSpreadEmbodiment with empty output list");
        }
        inputJoint = outputs.iterator().next().copyTree(input);
        this.outputJoints = outputs;
        copiers = new ArrayList<VObjectTransformCopier>();
        for (VJoint vj : outputJoints)
        {
            copiers.add(VObjectTransformCopier.newInstanceFromVJointTree(inputJoint, vj, "T1R"));
        }
    }

    @Override
    public void copy()
    {
        for (VObjectTransformCopier votc : copiers)
        {
            votc.copyConfig();
        }
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return inputJoint;
    }

}
