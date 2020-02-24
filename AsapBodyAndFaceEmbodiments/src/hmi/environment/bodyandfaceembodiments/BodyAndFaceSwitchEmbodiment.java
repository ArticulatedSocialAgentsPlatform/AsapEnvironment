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
import hmi.animationembodiments.VJointSwitchEmbodiment;
import hmi.environmentbase.InputSwitchEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.faceembodiments.FaceSwitchEmbodiment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * Redirects joint rotations to a BodyEmbodiment and Face control to the face embodiment. Input bodies and face are constructed on creation.
 * @author hvanwelbergen
 *
 */
public class BodyAndFaceSwitchEmbodiment implements FaceEmbodiment, SkeletonEmbodiment, InputSwitchEmbodiment
{
    private BodyAndFaceEmbodiment currentEmbodiment;
    private FaceSwitchEmbodiment fse;
    private VJointSwitchEmbodiment ase;
    private Map<String, SimpleBodyAndFaceEmbodiment> inputEmbodiments = new HashMap<String, SimpleBodyAndFaceEmbodiment>();
    
    @Getter
    private final String id;
    
    public BodyAndFaceSwitchEmbodiment(String id, List<String> inputIds, BodyAndFaceEmbodiment outputEmbodiment)
    {
        if (inputIds.isEmpty())
        {
            throw new RuntimeException("Cannot construct BodyAndFaceSwitchEmbodiment with empty input list");
        }
        this.id = id;
        fse = new FaceSwitchEmbodiment(id, inputIds, outputEmbodiment.getFaceController());
        ase = new VJointSwitchEmbodiment(id, inputIds, outputEmbodiment.getAnimationVJoint());
        for(String input:inputIds)
        {
            inputEmbodiments.put(input,new SimpleBodyAndFaceEmbodiment(input, ase.getInput(input),fse.getInput(input)));
        }
        currentEmbodiment = inputEmbodiments.get(inputIds.get(0));
    }
    
    public SimpleBodyAndFaceEmbodiment getInput(String input)
    {
        return inputEmbodiments.get(input);
    }
    
    @Override
    public void copy()
    {
        fse.copy();
        ase.copy();
    }

    @Override
    public Set<String> getInputs()
    {
        return fse.getInputs();
    }

    @Override
    public void selectInput(String name)
    {
        fse.selectInput(name);
        ase.selectInput(name);
        currentEmbodiment = inputEmbodiments.get(name);
    }

    @Override
    public FaceController getFaceController()
    {
        return currentEmbodiment.getFaceController();
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return currentEmbodiment.getAnimationVJoint();
    }
    
    public String getCurrentInput()
    {
        return fse.getCurrentInput();
    }
}
