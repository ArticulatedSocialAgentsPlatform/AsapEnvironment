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
package hmi.environment.bodyandfaceembodiments.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.testutil.animation.HanimBody;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;
/**
 * Unit tests for the BodyAndFaceSwitchEmbodimentLoader
 * @author hvanwelbergen
 */
public class BodyAndFaceSwitchEmbodimentLoaderTest
{
    private EmbodimentLoader mockEmbodimentLoader = mock(EmbodimentLoader.class);
    private BodyAndFaceEmbodiment mockBodyAndFaceEmbodiment = mock(BodyAndFaceEmbodiment.class);
    private FaceController mockFaceController = mock(FaceController.class);
    private CopyEnvironment mockCopyEnvironment = mock(CopyEnvironment.class);
        
    @Test
    public void test() throws IOException
    {
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockBodyAndFaceEmbodiment);
        when(mockBodyAndFaceEmbodiment.getFaceController()).thenReturn(mockFaceController);
        when(mockBodyAndFaceEmbodiment.getAnimationVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        
        
        //@formatter:off
        String str = "<Loader id=\"id1\" loader=\"hmi.animationembodiments.loader.BodyAndFaceSwitchEmbodimentLoader\">" +
                     "<Inputs ids=\"input1,input2,input3\"/>"+
                     "</Loader>";
        //@formatter:on
        BodyAndFaceSwitchEmbodimentLoader loader = new BodyAndFaceSwitchEmbodimentLoader();
        
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "vh1", "vh1", new Environment[]{mockCopyEnvironment},new Loader[]{mockEmbodimentLoader});
        
        assertNotNull(loader.getEmbodiment());
        assertThat(loader.getEmbodiment().getInputs(), IsIterableContainingInAnyOrder.containsInAnyOrder("input1","input2","input3"));
        assertEquals(3, loader.getParts().size());
    }
}
