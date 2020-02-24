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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.faceanimation.FaceController;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.testutil.math.Quat4fTestUtil;
import hmi.testutil.math.Vec3fTestUtil;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import rsb.RSBException;

/**
 * Unit tests for RsbBodyAndFaceEmbodiment
 * @author Herwin
 *
 */
public class RsbBodyAndFaceEmbodimentIntegrationTest
{
    private static final float PRECISION = 0.001f;
    private FaceController mockFaceController = mock(FaceController.class);
    
    @Test//(timeout = 6000)
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
        
        ImmutableList<String> expectedMorphs = ImmutableList.of("face1","face2","face3");
        when(mockFaceController.getPossibleFaceMorphTargetNames()).thenReturn(expectedMorphs);
        StubBodyAndFace sb = new StubBodyAndFace(remoteBody,mockFaceController,"billie");
                
        RsbEmbodiment rsbEmbodiment = new RsbEmbodiment();
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("idx", "billie", rsbEmbodiment);
        body.initialize(Lists.newArrayList(Hanim.HumanoidRoot, Hanim.vl5, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));
        RsbFaceEmbodiment rsbFace = new RsbFaceEmbodiment("id1", new RsbFaceController("billie", rsbEmbodiment));
        rsbFace.initialize();
        RsbBodyAndFaceEmbodiment rsbBodyAndFace = new RsbBodyAndFaceEmbodiment("id", "billie", rsbEmbodiment, rsbFace, body);
        
        assertEquals(Hanim.HumanoidRoot, rsbBodyAndFace.getAnimationVJoint().getSid());
        assertNotNull(rsbBodyAndFace.getAnimationVJoint().getPartBySid(Hanim.l_shoulder));
        Thread.sleep(2000);
        assertThat(sb.getJointList(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(Hanim.HumanoidRoot, Hanim.r_shoulder, Hanim.l_shoulder, Hanim.r_elbow));

        float qExpected[] = Quat4f.getQuat4fFromAxisAngle(0f, 1f, 0f, (float) Math.PI*0.3f);
        rsbBodyAndFace.getAnimationVJoint().getPart(Hanim.HumanoidRoot).setRotation(qExpected);
        rsbBodyAndFace.getAnimationVJoint().getPart(Hanim.r_shoulder).setRotation(qExpected);
        rsbBodyAndFace.getAnimationVJoint().getPart(Hanim.HumanoidRoot).setTranslation(1,2,3);
        
        assertThat(rsbBodyAndFace.getFaceController().getPossibleFaceMorphTargetNames(), 
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedMorphs.toArray(new String[3])));
        Thread.sleep(500);
        assertThat(sb.getMorphList(), 
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedMorphs.toArray(new String[3])));
        rsbBodyAndFace.getFaceController().setMorphTargets(expectedMorphs.toArray(new String[3]), new float[]{0.1f,0.2f,0.3f});
        rsbBodyAndFace.copy();
        Thread.sleep(500);
        
        verify(mockFaceController).setMorphTargets(expectedMorphs.toArray(new String[3]), new float[]{0.1f,0.2f,0.3f});
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
