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
package hmi.renderenvironment;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.converters.FACSConverter;
import hmi.faceanimation.model.FACS;
import hmi.faceanimation.model.FACSConfiguration;
import hmi.faceanimation.model.MPEG4Configuration;
import hmi.faceembodiments.AUConfig;
import hmi.faceembodiments.EyelidMorpherEmbodiment;
import hmi.faceembodiments.FACSFaceEmbodiment;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.facegraphics.HMIFaceController;
import hmi.math.Quat4f;

import java.util.ArrayList;

/** Loaded through HmiRenderEmbodimentLoader. CLEAN UP USING LOMBOK! */
public class HmiRenderBodyAndFaceEmbodiment extends HmiRenderBodyEmbodiment implements FaceEmbodiment, FACSFaceEmbodiment,
        BodyAndFaceEmbodiment
{
    private FaceController faceController = null;

    @Override
    public synchronized FaceController getFaceController()
    {
        return faceController;
    }

    public synchronized void setFaceController(FaceController fc)
    {
        faceController = fc;

    }

    private FACSConfiguration facsConfig = new FACSConfiguration();

    // TODO: grab this from elsewhere??
    private FACSConverter facsConverter = new FACSConverter();

    private EyelidMorpherEmbodiment eyelidMorpher = new EyelidMorpherEmbodiment("nullmorpher", new ArrayList<String>());

    public synchronized void setEyelidMorpher(EyelidMorpherEmbodiment elm)
    {
        eyelidMorpher = elm; 
    }

    @Override
    public synchronized void copy()
    {
        VJoint vjRightEye = super.getAnimationVJoint().getPartBySid(Hanim.r_eyeball_joint);
        VJoint vjLeftEye = super.getAnimationVJoint().getPartBySid(Hanim.l_eyeball_joint);
        float qRight[] = Quat4f.getQuat4f();
        float qLeft[] = Quat4f.getQuat4f();
        vjRightEye.getRotation(qRight);
        vjLeftEye.getRotation(qLeft);
        eyelidMorpher.setEyeLidMorph(qLeft, qRight, faceController);

        MPEG4Configuration mpeg4Config = new MPEG4Configuration();
        facsConverter.convert(facsConfig, mpeg4Config);
        faceController.addMPEG4Configuration(mpeg4Config);
        super.copy();
        faceController.copy();
        faceController.removeMPEG4Configuration(mpeg4Config);
    }

    @Override
    public synchronized void setAUs(AUConfig... configs)
    {
        for (AUConfig conf : configs)
        {
            if (conf != null)
            {
                switch (conf.getSide())
                {
                case LEFT:
                    facsConfig.setValue(FACS.Side.LEFT, conf.getAu(), conf.getValue());
                    break;
                case RIGHT:
                    facsConfig.setValue(FACS.Side.RIGHT, conf.getAu(), conf.getValue());
                    break;
                default:
                    facsConfig.setValue(FACS.Side.LEFT, conf.getAu(), conf.getValue());
                    facsConfig.setValue(FACS.Side.RIGHT, conf.getAu(), conf.getValue());
                    break;
                }
            }
        }
    }

}
