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

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author hvanwelbergen
 * 
 */
public class VJointSpreadEmbodimentTest
{
    private static final float ROTATION_PRECISION = 0.001f;
    
    @Test
    public void test()
    {
        VJoint output1 = HanimBody.getLOA1HanimBody();
        VJoint output2 = HanimBody.getLOA1HanimBody();
        VJointSpreadEmbodiment emb = new VJointSpreadEmbodiment("spreademb","inputj",ImmutableList.of(output1,output2));
        float qsrc[]=Quat4f.getQuat4fFromAxisAngle(1,0,0,2);
        emb.getAnimationVJoint().getPart(Hanim.r_shoulder).setRotation(qsrc);
        emb.copy();
        float qtarget[]=Quat4f.getQuat4f();
        output1.getPart(Hanim.r_shoulder).getRotation(qtarget);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qtarget,qsrc,ROTATION_PRECISION);
        output2.getPart(Hanim.r_shoulder).getRotation(qtarget);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qtarget,qsrc,ROTATION_PRECISION);
    }
}
