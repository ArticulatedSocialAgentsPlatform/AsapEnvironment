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
package hmi.faceembodiments;

import hmi.environmentbase.Embodiment;
import hmi.faceanimation.EyeLidMorpher;
import hmi.faceanimation.FaceController;

import java.util.List;

import lombok.Getter;

/**
 * Embodiment wrapper of an EyeLidMorpher
 * @author hvanwelbergen
 *
 */
public class EyelidMorpherEmbodiment implements Embodiment
{
    @Getter
    private String id;
    
    private final EyeLidMorpher morpher;
    
    public EyelidMorpherEmbodiment(String id, List<String>morphs)
    {
        this.id = id;
        morpher = new EyeLidMorpher(morphs.toArray(new String[morphs.size()]));
    }
    public EyelidMorpherEmbodiment(String id, List<String>morphs, float morpherWeight)
    {
        this.id = id;
        morpher = new EyeLidMorpher(morphs.toArray(new String[morphs.size()]), morpherWeight);
    }
    
    public void setEyeLidMorph(float []qLeftEye, float[]qRightEye, FaceController fc)
    {
        morpher.setEyeLidMorph(qLeftEye, qRightEye, fc);
    }
}
