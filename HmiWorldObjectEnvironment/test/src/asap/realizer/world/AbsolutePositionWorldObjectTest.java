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
package asap.realizer.world;

import static hmi.testutil.math.Vec3fTestUtil.assertVec3fEquals;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.math.Vec3f;
import hmi.worldobjectenvironment.AbsolutePositionWorldObject;
import hmi.worldobjectenvironment.WorldObject;

import org.junit.Test;

/**
 * Unit tests for the AbsolutePositionWorldObject
 * @author hvanwelbergen
 *
 */
public class AbsolutePositionWorldObjectTest
{
    private static final float POSITION_PRECISION = 0.0001f;
    
    @Test
    public void testGetTranslation()
    {
        VJoint vj2 = new VJoint();
        VJoint vjWorld = new VJoint();
        vj2.setTranslation(-10, 0, 0);
        float q[] = new float[4];
        Quat4f.setFromAxisAngle4f(q, 0, 0, 1, (float) Math.PI * 0.5f);
        vj2.setRotation(q);
        vjWorld.addChild(vj2);
        WorldObject wj = new AbsolutePositionWorldObject(Vec3f.getVec3f(10,0,0));
        float[] trRef = { 0, -20, 0 };
        float[] tr = new float[3];
        wj.getTranslation(tr, vj2);
        assertVec3fEquals(tr, trRef, POSITION_PRECISION);
    }
}
