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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.animation.Hanim;

import java.util.List;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import asap.rsbembodiments.Rsbembodiments.Skeleton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;

/**
 * Unit tests for RsbBodyEmbodiment
 * @author hvanwelbergen
 * 
 */
public class RsbBodyEmbodimentTest
{
    private RsbEmbodiment mockRsbEmbodiment = mock(RsbEmbodiment.class);
    private Skeleton skeleton;
    private BiMap<String,String> morphMap = HashBiMap.create();
    
    @Before
    public void setup()
    {
        skeleton = Skeleton.newBuilder().addAllParents(ImmutableList.of("-", Hanim.HumanoidRoot, "joint2"))
                .addAllJoints(ImmutableList.of(Hanim.HumanoidRoot, "joint2", "joint3")).addAllLocalTransformation(Floats.asList(new float[16 * 3]))
                .build();
        when(mockRsbEmbodiment.getSkeleton()).thenReturn(skeleton);
        morphMap.put("joint2","joint2map");
        morphMap.put("joint3","joint3map");        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJointSelection()
    {
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("id", "billie", mockRsbEmbodiment);
        body.initialize(ImmutableList.of(Hanim.HumanoidRoot, "joint2", "joint4"));
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> args = ArgumentCaptor.forClass(List.class);
        verify(mockRsbEmbodiment, times(1)).selectJoints(args.capture());
        assertThat((List<String>) args.getValue(), IsIterableContainingInOrder.contains(Hanim.HumanoidRoot, "joint2"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJointSelectionWithMap()
    {
        RsbBodyEmbodiment body = new RsbBodyEmbodiment("id", "billie", mockRsbEmbodiment);
        body.initialize(morphMap, ImmutableList.of(Hanim.HumanoidRoot, "joint2map", "joint3map"));
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> args = ArgumentCaptor.forClass(List.class);
        verify(mockRsbEmbodiment, times(1)).selectJoints(args.capture());
        assertThat((List<String>) args.getValue(), IsIterableContainingInOrder.contains(Hanim.HumanoidRoot, "joint2", "joint3"));
    }
}
