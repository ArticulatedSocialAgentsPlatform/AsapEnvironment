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
package hmi.faceembodiments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hmi.faceanimation.FaceController;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the FaceSwitchEmbodiment
 * @author hvanwelbergen
 *
 */
public class FaceSwitchEmbodimentTest
{
    private FaceController mockOutputController = mock(FaceController.class);
    private FaceSwitchEmbodiment switchEmb = new FaceSwitchEmbodiment("id1", ImmutableList.of("input1", "input2", "input3"), mockOutputController);
    @Test
    public void testInit()
    {
        assertEquals("input1",switchEmb.getCurrentInput());
        assertThat(switchEmb.getInputs(), IsIterableContainingInAnyOrder.containsInAnyOrder("input1","input2","input3"));        
    }
    
    @Test
    public void testCopy()
    {
        switchEmb.getInput("input2").setMorphTargets(new String[]{"x"},new float[]{1.0f});
        switchEmb.selectInput("input2");
        switchEmb.copy();
        verify(mockOutputController).setMorphTargets(eq(new String[]{"x"}), eq(new float[]{1.0f}));
    }
}
