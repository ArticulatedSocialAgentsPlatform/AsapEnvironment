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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.testutil.animation.HanimBody;
import hmi.testutil.math.Quat4fTestUtil;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the VJointSwitchEmbodiment
 * @author hvanwelbergen
 * 
 */
public class VJointSwitchEmbodimentTest
{
    private VJoint outputJoint = HanimBody.getLOA1HanimBody();
    private VJointSwitchEmbodiment switchEmb = new VJointSwitchEmbodiment("id1", ImmutableList.of("input1", "input2", "input3"),
            outputJoint);
    private static final float ROTATION_PRECISION = 0.001f;
    
    @Test
    public void testInit()
    {
        assertEquals("input1",switchEmb.getCurrentInput());
        assertThat(switchEmb.getInputs(), IsIterableContainingInAnyOrder.containsInAnyOrder("input1","input2","input3"));
        assertNotNull(switchEmb.getInput("input2").getPart(Hanim.r_shoulder));
    }
    
    @Test
    public void testCopy()
    {
        switchEmb.getInput("input2").getPart(Hanim.r_shoulder).setRotation(Quat4f.getQuat4fFromAxisAngle(1,0,0,0.5f));
        switchEmb.selectInput("input2");
        switchEmb.copy();
        float q[] = Quat4f.getQuat4f();
        outputJoint.getPart(Hanim.r_shoulder).getRotation(q);
        Quat4fTestUtil.assertQuat4fRotationEquivalent(Quat4f.getQuat4fFromAxisAngle(1,0,0,0.5f), q, ROTATION_PRECISION);
    }
}
