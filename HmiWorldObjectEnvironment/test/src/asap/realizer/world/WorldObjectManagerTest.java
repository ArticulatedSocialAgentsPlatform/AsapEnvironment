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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import org.junit.Test;

/**
 * Unit tests for the WorldObjectManager
 * @author hvanwelbergen
 * 
 */
public class WorldObjectManagerTest
{
    private WorldObjectManager woManager = new WorldObjectManager();
    private static final float POSITION_PRECISION = 0.0001f;

    @Test
    public void testGetWorldObjectAbsolute()
    {
        WorldObject wo = woManager.getWorldObject("1,2, 3");
        assertNotNull(wo);
        float tr[]=Vec3f.getVec3f();
        wo.getTranslation(tr,null);
        assertVec3fEquals(1,2,3,tr,POSITION_PRECISION);
    }
    
    @Test
    public void testGetWorldInvalid()
    {
        WorldObject wo = woManager.getWorldObject("1,2, 3b");
        assertNull(wo);
    }
    
    @Test
    public void testGetVJointWorldObject()
    {
        woManager.addWorldObject("testObject", new VJointWorldObject(new VJoint("vj")));
        assertNotNull(woManager.getWorldObject("testObject"));
    }
}
