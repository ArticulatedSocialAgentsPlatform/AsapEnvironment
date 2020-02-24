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
package hmi.faceembodiments.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.xml.XMLTokenizer;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

/**
 * Unit tests for the FaceSwitchEmbodimentLoader
 * @author hvanwelbergen
 *
 */
public class FaceSwitchEmbodimentLoaderTest
{
    private EmbodimentLoader mockEmbodimentLoader = mock(EmbodimentLoader.class);
    private FaceEmbodiment mockFaceEmbodiment = mock(FaceEmbodiment.class);
    private FaceController mockFaceController = mock(FaceController.class);
    
    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockFaceEmbodiment);
        when(mockFaceEmbodiment.getFaceController()).thenReturn(mockFaceController);
        
        //@formatter:off
        String str = "<Loader id=\"id1\" loader=\"hmi.animationembodiments.loader.VJointSwitchEmbodimentLoader\">" +
                     "<Inputs ids=\"input1,input2,input3\"/>"+
                     "</Loader>";
        //@formatter:on
        FaceSwitchEmbodimentLoader loader = new FaceSwitchEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "vh1", "vh1", new Environment[0],new Loader[]{mockEmbodimentLoader});
        assertNotNull(loader.getEmbodiment());
        assertThat(loader.getEmbodiment().getInputs(), IsIterableContainingInAnyOrder.containsInAnyOrder("input1","input2","input3"));
        assertEquals(3, loader.getParts().size());
    }
}
