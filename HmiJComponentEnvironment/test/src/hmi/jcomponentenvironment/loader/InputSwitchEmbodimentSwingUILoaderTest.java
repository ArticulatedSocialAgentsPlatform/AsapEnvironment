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
package hmi.jcomponentenvironment.loader;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.InputSwitchEmbodiment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEmbodiment;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import javax.swing.JComponent;

import org.junit.Test;


/**
 * Unit tests for the VJointSwitchEmbodimentSwingUILoader
 * @author hvanwelbergen
 *
 */
public class InputSwitchEmbodimentSwingUILoaderTest
{
    @Test
    public void test() throws IOException
    {
        //@formatter:off
        String str = "<Loader id=\"id1\" loader=\"asap.animationswitchuienvironment.loader.VJointSwitchEmbodimentSwingUILoader\"/>";
        //@formatter:on

        JComponentEmbodiment mockJCEmbodiment = mock(JComponentEmbodiment.class);
        JComponentEmbodimentLoader mockJCEmbodimentLoader = mock(JComponentEmbodimentLoader.class);
        when(mockJCEmbodimentLoader.getEmbodiment()).thenReturn(mockJCEmbodiment);
        
        EmbodimentLoader mockVJSwitchEmbodimentLoader = mock(EmbodimentLoader.class);
        InputSwitchEmbodiment mockvjSwitch = mock(InputSwitchEmbodiment.class);
        when(mockVJSwitchEmbodimentLoader.getEmbodiment()).thenReturn(mockvjSwitch);
        
        InputSwitchEmbodimentSwingUILoader loader = new InputSwitchEmbodimentSwingUILoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "vh1", "vh1", new Environment[0],new Loader[]{mockJCEmbodimentLoader,mockVJSwitchEmbodimentLoader});
        verify(mockJCEmbodiment).addJComponent(any(JComponent.class));
    }
}
