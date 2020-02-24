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
package asap.rsbembodiments;

import hmi.faceanimation.FaceController;
import hmi.faceanimation.NullMPEG4FaceController;

import java.util.Collection;
import java.util.List;

import lombok.Delegate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;

/**
 * Implements morph based face animation through rsb (MPEG4 animation is ignored)
 * @author hvanwelbergen
 * 
 */
public class RsbFaceController implements FaceController
{
    public RsbFaceController(String characterId, RsbEmbodiment env)
    {
        this(characterId, env, HashBiMap.<String,String>create());
    }
    
    public RsbFaceController(String characterId, RsbEmbodiment env, BiMap<String, String> renamingMap)
    {
        mfc = new RsbMorphFaceController(characterId, env, renamingMap);
    }
    
    public void initialize()
    {
        mfc.initialize();
    }
    
    private interface Excludes
    {
        void copy();
    }
    @Delegate(excludes = Excludes.class)
    private NullMPEG4FaceController mpegfc = new NullMPEG4FaceController();
    
    
    private RsbMorphFaceController mfc;
    
    @Override
    public void setMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.setMorphTargets(targetNames, weights);        
    }

    @Override
    public float getCurrentWeight(String targetName)
    {
        return mfc.getCurrentWeight(targetName);
    }

    @Override
    public void addMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.addMorphTargets(targetNames, weights);        
    }

    @Override
    public void removeMorphTargets(String[] targetNames, float[] weights)
    {
        mfc.removeMorphTargets(targetNames, weights);        
    }

    @Override
    public Collection<String> getPossibleFaceMorphTargetNames()
    {
        return mfc.getPossibleFaceMorphTargetNames();
    }

    public ImmutableMap<String, Float> getDesiredMorphTargets()
    {
        return mfc.getDesiredMorphTargets();
    }    
    
    @Override
    public void copy()
    {
        mfc.copy();        
    }    
    
    public List<Float> getMorphValues()
    {
        return mfc.getMorphValues();
    }
}
