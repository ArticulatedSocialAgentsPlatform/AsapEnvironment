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
import hmi.environmentbase.InputSwitchEmbodiment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * Redirects the rotations/translations from selected input VJoint tree to an output joint. Input joints are constructed on creation. 
 * @author hvanwelbergen
 *
 */
public class VJointSwitchEmbodiment implements SkeletonEmbodiment, InputSwitchEmbodiment
{
    private Map<String, VJoint> inputJoints = new HashMap<String, VJoint>();
    private VObjectTransformCopier copier;
    private VJoint currentJoint;
    private final VJoint outputJoint;
    private String currentJointName;
    
    @Getter
    private final String id;
    
    public VJointSwitchEmbodiment(String id, List<String> inputIds, VJoint outputJoint)
    {
        if(inputIds.isEmpty())
        {
            throw new RuntimeException("Cannot construct VJointSwitchEmbodiment with empty input list");
        }
        for(String input: inputIds)
        {
            VJoint copy = outputJoint.copyTree(input);
            inputJoints.put(input,copy);
        }
        this.outputJoint = outputJoint;
        this.id = id;
        selectInput(inputIds.get(0));        
    }
    
    public void selectInput(String name)
    {
        currentJoint = inputJoints.get(name);
        currentJointName = name;
        copier = VObjectTransformCopier.newInstanceFromVJointTree(currentJoint,outputJoint,"T1R");
    }
    
    public String getCurrentInput()
    {
        return currentJointName;
    }
    
    public VJoint getInput(String name)
    {
        return inputJoints.get(name);
    }
    
    public Set<String>getInputs()
    {
        return inputJoints.keySet();
    }
    
    @Override
    public void copy()
    {
        copier.copyConfig();        
    }    

    @Override
    public VJoint getAnimationVJoint()
    {
        return currentJoint;
    }

}
