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

import hmi.faceembodiments.FaceEmbodiment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.ImmutableMap;

/**
 * FaceEmbodiment that makes use of an RsbFaceController
 * @author hvanwelbergen
 *
 */
public class RsbFaceEmbodiment implements FaceEmbodiment
{
    private final RsbFaceController fc;
    @Getter @Setter
    private String id;
    
    public RsbFaceEmbodiment(String id, RsbFaceController fc)
    {
        this.id = id;
        this.fc = fc;         
    }
    
    public void initialize()
    {
        fc.initialize();
    }
    
    @Override
    public void copy()
    {
        fc.copy();        
    }
    
    public ImmutableMap<String, Float> getDesiredMorphTargets()
    {
        return fc.getDesiredMorphTargets();
    }
    
    @Override
    public RsbFaceController getFaceController()
    {
        return fc;
    }
    
    public List<Float> getMorphValues()
    {
        return fc.getMorphValues();
    }
}
