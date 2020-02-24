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

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.EyelidMorpherEmbodiment;
import hmi.math.Quat4f;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import asap.rsbembodiments.Rsbembodiments.AnimationData;

/**
 * Steers a body and a face through a rsb renderer
 * @author hvanwelbergen
 * 
 */
public class RsbBodyAndFaceEmbodiment implements BodyAndFaceEmbodiment
{
    @Getter
    private final String id;
    private final String characterId;
    private final RsbFaceEmbodiment faceEmbodiment;
    private final RsbBodyEmbodiment bodyEmbodiment;
    private final RsbEmbodiment rsbEmbodiment;

    @Setter
    private EyelidMorpherEmbodiment eyelidMorpher = new EyelidMorpherEmbodiment("", new ArrayList<String>());

    public RsbBodyAndFaceEmbodiment(String id, String characterId, RsbEmbodiment rsbEmbodiment, RsbFaceEmbodiment faceEmbodiment,
            RsbBodyEmbodiment bodyEmbodiment)
    {
        this.id = id;
        this.characterId = characterId;
        this.rsbEmbodiment = rsbEmbodiment;
        this.faceEmbodiment = faceEmbodiment;
        this.bodyEmbodiment = bodyEmbodiment;
    }

    @Override
    public FaceController getFaceController()
    {
        return faceEmbodiment.getFaceController();
    }

    @Override
    public void copy()
    {
        synchronized (faceEmbodiment.getFaceController())
        {
            VJoint vjRightEye = getAnimationVJoint().getPartBySid(Hanim.r_eyeball_joint);
            VJoint vjLeftEye = getAnimationVJoint().getPartBySid(Hanim.l_eyeball_joint);
            if (vjRightEye != null && vjLeftEye != null)
            {
                float qRight[] = Quat4f.getQuat4f();
                float qLeft[] = Quat4f.getQuat4f();
                vjRightEye.getRotation(qRight);
                vjLeftEye.getRotation(qLeft);
                eyelidMorpher.setEyeLidMorph(qLeft, qRight, faceEmbodiment.getFaceController());
            }
            rsbEmbodiment.sendAnimationData(AnimationData.newBuilder().setCharacterId(characterId)
                    .addAllRootTranslation(bodyEmbodiment.getRootTranslation()).addAllJointQuats(bodyEmbodiment.getJointQuats())
                    .addAllMorphWeights(faceEmbodiment.getMorphValues()).build());
        }
    }

    @Override
    public VJoint getAnimationVJoint()
    {
        return bodyEmbodiment.getAnimationVJoint();
    }
}
