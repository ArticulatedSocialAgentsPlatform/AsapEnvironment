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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import hmi.faceanimation.FaceController;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import rsb.RSBException;

/**
 * Unit tests for RsbFaceEmbodiment
 * @author Herwin
 *
 */
public class RsbFaceEmbodimentIntegrationTest
{
    private FaceController mockFaceController = mock(FaceController.class);
    
    @Test(timeout = 6000)
    @Ignore //FIXME: broken for unknown reasons in junitAll
    public void test() throws RSBException, InterruptedException
    {
        StubFace sf = new StubFace(mockFaceController,"billie");
        ImmutableList<String> expectedMorphs = ImmutableList.of("face1","face2","face3");        
        when(mockFaceController.getPossibleFaceMorphTargetNames()).thenReturn(expectedMorphs);
        
        RsbEmbodiment rsbEmbodiment = new RsbEmbodiment();
        RsbFaceEmbodiment rsbFace = new RsbFaceEmbodiment("id1", new RsbFaceController("billie", rsbEmbodiment));
        rsbFace.initialize();
        assertThat(rsbFace.getFaceController().getPossibleFaceMorphTargetNames(), 
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedMorphs.toArray(new String[3])));
        Thread.sleep(500);
        assertThat(sf.getMorphList(), 
                IsIterableContainingInAnyOrder.containsInAnyOrder(expectedMorphs.toArray(new String[3])));
        rsbFace.getFaceController().setMorphTargets(expectedMorphs.toArray(new String[3]), new float[]{0.1f,0.2f,0.3f});
        rsbFace.copy();
        Thread.sleep(500);
        
        verify(mockFaceController).setMorphTargets(expectedMorphs.toArray(new String[3]), new float[]{0.1f,0.2f,0.3f});
        sf.deactivate();
        rsbEmbodiment.shutdown();
    }
}
