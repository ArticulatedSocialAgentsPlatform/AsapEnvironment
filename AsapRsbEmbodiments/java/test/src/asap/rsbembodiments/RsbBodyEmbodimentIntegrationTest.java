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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.math.Quat4fTestUtil;
import hmi.testutil.math.Vec3fTestUtil;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Ignore;
import org.junit.Test;

import rsb.RSBException;

import com.google.common.collect.Lists;

/**
 * Unit test for the RsbBodyEmbodiment
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodimentIntegrationTest
{
    private static final float PRECISION = 0.001f;
    
    @Test//(timeout = 6000)
    @Ignore //FIXME: broken for unknown reasons in junitAll
    public void test() throws RSBException, InterruptedException
    {
        VJoint remoteBody = new VJoint(Hanim.HumanoidRoot, Hanim.HumanoidRoot);
        VJoint vRShoulder = new VJoint(Hanim.r_shoulder, Hanim.r_shoulder);
        vRShoulder.setTranslation(Vec3f.getVec3f(-1,1,0));
        remoteBody.addChild(vRShoulder);
        VJoint vLShoulder = new VJoint(Hanim.l_shoulder, Hanim.l_shoulder);
        vLShoulder.setTranslation(Vec3f.getVec3f(1,1,0));
        remoteBody.addChild(vLShoulder);
        VJoint vRElbow = new VJoint(Hanim.r_elbow, Hanim.r_elbow);
        vRElbow.setTranslation(Vec3f.getVec3f(0,-1,0));
        vRShoulder.addChild(vRElbow);
        remoteBody.setTranslation(Vec3f.getVec3f(1,2,3));
        
        StubBody sb = new StubBody(remoteBody,"billie");        
        RsbEmbodiment rsbEmbodiment = new RsbEmbodiment();
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("idx", "billie", rsbEmbodiment);
        body.initialize(Lists.newArrayList(Hanim.HumanoidRoot, Hanim.vl5, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));
        assertEquals(Hanim.HumanoidRoot, body.getAnimationVJoint().getSid());
        assertNotNull(body.getAnimationVJoint().getPartBySid(Hanim.l_shoulder));
        Thread.sleep(1000);
        assertThat(sb.getJointList(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.HumanoidRoot, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));

        float qExpected[] = Quat4f.getQuat4fFromAxisAngle(0f, 1f, 0f, (float) Math.PI*0.3f);
        body.getAnimationVJoint().getPart(Hanim.HumanoidRoot).setTranslation(1,2,3);
        body.getAnimationVJoint().getPart(Hanim.HumanoidRoot).setRotation(qExpected);
        body.getAnimationVJoint().getPart(Hanim.r_shoulder).setRotation(qExpected);
        body.copy();
        Thread.sleep(500);
        float q[] = Quat4f.getQuat4f();
        remoteBody.getPart(Hanim.r_shoulder).getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(qExpected, q, PRECISION);
        float tr[] = Vec3f.getVec3f();
        remoteBody.getPart(Hanim.HumanoidRoot).getTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(Vec3f.getVec3f(1,2,3), tr, PRECISION);
        sb.deactivate();        
        body.shutdown();
    }
}
