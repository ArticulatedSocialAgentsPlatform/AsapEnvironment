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
package asap.rsbembodiments.util;

import static org.junit.Assert.assertEquals;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Mat4f;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import org.junit.Test;

import asap.rsbembodiments.Rsbembodiments.Skeleton;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;

/**
 * Unit tests for VJointRsbUtils
 * @author hvanwelbergen
 * 
 */
public class VJointRsbUtilsTest
{
    private static final float PRECISION = 0.0001f;

    private Skeleton createOneJointSkeleton(String id, String parent, float m[])
    {
        return Skeleton.newBuilder().addAllJoints(ImmutableList.of("HumanoidRoot")).addAllParents(ImmutableList.of("root"))
                .addAllLocalTransformation(Floats.asList(m)).build();
    }

    @Test
    public void testToVJointRootOnly()
    {
        float qExpected[] = Quat4f.getQuat4fFromAxisAngleDegrees(1, 1, 1, 50);
        float mExpected[] = Mat4f.getMat4f(); 
        Mat4f.setFromTR(mExpected, Vec3f.getVec3f(1, 2, 3), qExpected);
        Skeleton s = createOneJointSkeleton("HumanoidRoot", "-", mExpected);
        
        
        VJoint vj = VJointRsbUtils.toVJoint(s);
        assertEquals("HumanoidRoot", vj.getSid());
        assertEquals(null, vj.getParent());
        float q[] = Quat4f.getQuat4f();
        vj.getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, PRECISION);
    }

    @Test
    public void testToVJoint()
    {
        Skeleton.Builder sBuilder = Skeleton.newBuilder();
        sBuilder.addAllJoints(ImmutableList.of(Hanim.HumanoidRoot, Hanim.r_shoulder  , Hanim.r_elbow   , Hanim.l_shoulder,  Hanim.l_elbow));
        sBuilder.addAllParents(ImmutableList.of("root"           , Hanim.HumanoidRoot, Hanim.r_shoulder, Hanim.HumanoidRoot,Hanim.l_shoulder));
        sBuilder.addAllLocalTransformation(Floats.asList(new float[16*5]));
        VJoint vj = VJointRsbUtils.toVJoint(sBuilder.build());
        assertEquals(5, vj.getParts().size());
    }

    @Test
    public void testConvertBackAndForth()
    {
        VJoint vj = VJointRsbUtils.toVJoint(VJointRsbUtils.toRsbSkeleton(HanimBody.getLOA1HanimBody()));
        assertEquals(Hanim.HumanoidRoot, vj.getSid());
    }
}
