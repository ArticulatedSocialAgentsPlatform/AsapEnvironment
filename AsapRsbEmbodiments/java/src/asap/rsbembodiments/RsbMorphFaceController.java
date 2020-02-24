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

import hmi.faceanimation.MorphFaceController;
import hmi.faceanimation.MorphTargetHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import asap.rsbembodiments.Rsbembodiments.AnimationData;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Face controller that handles face morphing through rsb
 * @author hvanwelbergen
 * 
 */
public class RsbMorphFaceController implements MorphFaceController
{
    private final RsbEmbodiment rsbEmbodiment;
    private final BiMap<String, String> renamingMap;
    private final String characterId;

    public RsbMorphFaceController(String characterId, RsbEmbodiment embodiment, BiMap<String, String> renamingMap)
    {
        this.characterId = characterId;
        rsbEmbodiment = embodiment;
        this.renamingMap = renamingMap;
    }

    public RsbMorphFaceController(String characterId, RsbEmbodiment embodiment)
    {
        this(characterId, embodiment, HashBiMap.<String, String> create());
    }

    public void initialize()
    {
        rsbEmbodiment.initialize(characterId);
        rsbEmbodiment.selectMorphs(rsbEmbodiment.getAvailableMorphs());
    }
    
    private MorphTargetHandler morphTargetHandler = new MorphTargetHandler();

    @Override
    public List<String> getPossibleFaceMorphTargetNames()
    {
        List<String> targets = new ArrayList<>();
        for (String str : rsbEmbodiment.getAvailableMorphs())
        {
            if (renamingMap.get(str) != null)
            {
                targets.add(renamingMap.get(str));
            }
            else
            {
                targets.add(str);
            }
        }
        return ImmutableList.copyOf(targets);
    }

    public ImmutableMap<String, Float> getDesiredMorphTargets()
    {
        Map<String, Float> desired = new HashMap<String, Float>();
        {
            for (Entry<String, Float> entry : morphTargetHandler.getDesiredMorphTargets().entrySet())
            {
                String id = entry.getKey();
                if (renamingMap.inverse().get(id) != null)
                {
                    id = renamingMap.inverse().get(id);
                }
                desired.put(id, entry.getValue());
            }
        }
        return ImmutableMap.copyOf(desired);
    }

    public List<Float> getMorphValues()
    {
        List<Float> morphList = new ArrayList<Float>();
        for (String morph : getPossibleFaceMorphTargetNames())
        {
            morphList.add(getCurrentWeight(morph));
        }
        return morphList;
    }

    @Override
    public void copy()
    {
        rsbEmbodiment.sendAnimationData(AnimationData.newBuilder().addAllMorphWeights(getMorphValues()).build());
    }

    @Override
    public void addMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.addMorphTargets(arg0, arg1);
    }

    @Override
    public void removeMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.removeMorphTargets(arg0, arg1);
    }

    @Override
    public void setMorphTargets(String[] arg0, float[] arg1)
    {
        morphTargetHandler.setMorphTargets(arg0, arg1);
    }

    public float getCurrentWeight(String morph)
    {
        return morphTargetHandler.getCurrentWeight(morph);
    }
}
