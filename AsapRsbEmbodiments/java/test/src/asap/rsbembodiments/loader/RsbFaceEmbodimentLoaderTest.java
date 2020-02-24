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
package asap.rsbembodiments.loader;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import hmi.environmentbase.ClockDrivenCopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.rsbembodiments.RsbEmbodiment;
import asap.rsbembodiments.RsbFaceController;

import com.google.common.collect.ImmutableList;

/**
 * unit tests for the RsbFaceEmbodimentLoader
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RsbFaceEmbodimentLoader.class)
public class RsbFaceEmbodimentLoaderTest
{
    private RsbEmbodimentLoader mockEmbodimentLoader = mock(RsbEmbodimentLoader.class);
    private RsbEmbodiment mockEmbodiment = mock(RsbEmbodiment.class);
    
    @Test
    public void test() throws Exception
    {
        RsbFaceController mockRsbFc = mock(RsbFaceController.class);
        whenNew(RsbFaceController.class).withArguments(any(String.class),any(RsbEmbodiment.class)).thenReturn(mockRsbFc);
        
        when(mockEmbodimentLoader.getEmbodiment()).thenReturn(mockEmbodiment);
        String str = "<Loader id=\"ipaacafaceembodiment\" loader=\"asap.rsbembodiments.loader.RsbFaceEmbodimentLoader\"/>";
        ClockDrivenCopyEnvironment env = new ClockDrivenCopyEnvironment(20);
        RsbFaceEmbodimentLoader loader = new RsbFaceEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", ImmutableList.of(env).toArray(new Environment[0]), mockEmbodimentLoader);
        assertNotNull(loader.getEmbodiment());
        env.time(0);
        verify(mockRsbFc).copy();
    }
}
