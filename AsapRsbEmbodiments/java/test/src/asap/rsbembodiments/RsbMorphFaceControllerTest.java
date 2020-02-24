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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;

/**
 * Unit tests for the RsbMorphFaceController
 * @author hvanwelbergen
 *
 */
public class RsbMorphFaceControllerTest
{
    private RsbEmbodiment mockRsbEmbodiment = mock(RsbEmbodiment.class);
    private BiMap<String,String> morphMap = HashBiMap.create();
    
    @Before
    public void setup()
    {
        when(mockRsbEmbodiment.getAvailableMorphs()).thenReturn(ImmutableList.of("face1","face2","face3"));
        morphMap.put("face1","face1map");
        morphMap.put("face2","face2map");
        morphMap.put("face3","face3map");
    }
    
    @Test
    public void testPossibleMorphs()
    {
        RsbMorphFaceController mfc = new RsbMorphFaceController("billie", mockRsbEmbodiment);
        assertThat(mfc.getPossibleFaceMorphTargetNames(),IsIterableContainingInAnyOrder.containsInAnyOrder("face1","face2","face3")); 
    }
    
    @Test
    public void testPossibleMorphsWithMap()
    {
        RsbMorphFaceController mfc = new RsbMorphFaceController("billie", mockRsbEmbodiment,morphMap);
        assertThat(mfc.getPossibleFaceMorphTargetNames(),IsIterableContainingInAnyOrder.containsInAnyOrder("face1map","face2map","face3map")); 
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSelection()
    {
        RsbMorphFaceController mfc = new RsbMorphFaceController("billie", mockRsbEmbodiment);
        mfc.initialize();   
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> args = ArgumentCaptor.forClass(List.class);
        verify(mockRsbEmbodiment).selectMorphs(args.capture());
        assertThat((List<String>)args.getValue(), IsIterableContainingInOrder.contains("face1","face2","face3"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSelectionWithMapping()
    {
        RsbMorphFaceController mfc = new RsbMorphFaceController("billie", mockRsbEmbodiment, morphMap);
        mfc.initialize();   
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> args = ArgumentCaptor.forClass(List.class);
        verify(mockRsbEmbodiment).selectMorphs(args.capture());
        assertThat((List<String>)args.getValue(), IsIterableContainingInOrder.contains("face1","face2","face3"));
    }
    @Test
    public void testGetMorphValues()
    {
        RsbMorphFaceController mfc = new RsbMorphFaceController("billie", mockRsbEmbodiment);
        mfc.initialize();
        mfc.addMorphTargets(new String[]{"face1","face2","face3"}, new float[]{0.1f,0.2f,0.3f});
        assertThat(mfc.getMorphValues(), IsIterableContainingInOrder.contains(0.1f,0.2f,0.3f));
    }    
}
