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
package asap.rsbworldenvironment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hmi.animation.VJoint;
import hmi.math.Vec3f;
import hmi.testutil.math.Vec3fTestUtil;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObject;
import asap.rsbworldenvironment.Rsbworldenvironment.RSBWorldObjects;

import com.google.common.primitives.Floats;

/**
 * Unit tests for the RsbWorldEnvironment
 * @author hvanwelbergen
 *
 */
public class RsbWorldEnvironmentTest
{
    private WorldObjectManager mockWoManager = mock(WorldObjectManager.class);
    private WorldObjectManager woManager = new WorldObjectManager();
    private Factory factory = Factory.getInstance();
    private Informer<RSBWorldObjects> informer;
    private static final float PRECISION = 0.0001f;
    
    @Before
    public void before() throws InitializeException
    {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBWorldObjects.getDefaultInstance()));
        informer = factory.createInformer(RsbWorldEnvironmentC.SCENEINFO_SCOPE);
        try {
            informer.activate();
        } catch (RSBException e) {
            throw new RuntimeException(e);
        }
    }
    
    @After
    public void after() throws RSBException, InterruptedException
    {
        informer.deactivate();
    }
    
    @Test
    public void test() throws InterruptedException, RSBException
    {
        new RsbWorldEnvironmentC(mockWoManager);
        informer.publish(RSBWorldObjects.newBuilder()
                     .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("camera").addAllPosition(Floats.asList(1,1,1)).build())
                     .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(1,1,1)).build())
                     .build());
        Thread.sleep(500);
        verify(mockWoManager).addWorldObject(eq("camera"), any(WorldObject.class));
        verify(mockWoManager).addWorldObject(eq("ent 2"), any(WorldObject.class));
    }
    
    @Test
    public void testPos()throws InterruptedException, RSBException
    {
        new RsbWorldEnvironmentC(woManager);
        informer.publish(RSBWorldObjects.newBuilder()
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("camera").addAllPosition(Floats.asList(0.12f, 0.12f, 0f)).build())
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(0,0,2)).build())
                .build());
        Thread.sleep(500);
        float tr[] = Vec3f.getVec3f();
        woManager.getWorldObject("ent 2").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0f,0f,2f},tr, PRECISION);
        
        woManager.getWorldObject("camera").getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.12f, 0.12f, 0f},tr, PRECISION);
    }
    
    @Test
    public void testPosExisting() throws InterruptedException, RSBException
    {
        WorldObject ent1 = new VJointWorldObject(new VJoint("ent1"));
        woManager.addWorldObject("ent 1", ent1);
        new RsbWorldEnvironmentC(woManager);
        
        informer.publish(RSBWorldObjects.newBuilder()
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 1").addAllPosition(Floats.asList(0.1f,0.2f,2f)).build())
                .addWorldObjects(RSBWorldObject.newBuilder().setObjectId("ent 2").addAllPosition(Floats.asList(0,0,2)).build())
                .build());
        
        Thread.sleep(500);        
        float tr[] = Vec3f.getVec3f();
        ent1.getWorldTranslation(tr);
        Vec3fTestUtil.assertVec3fEquals(new float[]{0.1f,0.2f,2f},tr, PRECISION);
    }
}
