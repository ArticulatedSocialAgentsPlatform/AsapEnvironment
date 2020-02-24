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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import asap.rsbembodiments.RsbEmbodiment;

/**
 * unit tests for the RsbEmbodimentLoader
 * @author Herwin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RsbEmbodimentLoader.class)
public class RsbEmbodimentLoaderTest
{
    @Test
    public void test() throws Exception
    {
        RsbEmbodiment mockRsbEmbodiment = mock(RsbEmbodiment.class);
        whenNew(RsbEmbodiment.class).withNoArguments().thenReturn(mockRsbEmbodiment);
        
        String str = "<Loader id=\"rsbembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaEmbodimentLoader\"/>";                
        RsbEmbodimentLoader loader = new RsbEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertEquals(mockRsbEmbodiment, loader.getEmbodiment());
    }
    
    @Test
    public void testNoCharacterScope() throws Exception
    {
        RsbEmbodiment mockRsbEmbodiment = mock(RsbEmbodiment.class);
        whenNew(RsbEmbodiment.class).withNoArguments().thenReturn(mockRsbEmbodiment);
        
        String str = "<Loader id=\"rsbembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaEmbodimentLoader\">"
        +"<characterScope characterScope=\"\"/>"
        +"</Loader>";                
        RsbEmbodimentLoader loader = new RsbEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertEquals(mockRsbEmbodiment, loader.getEmbodiment());
        verify(mockRsbEmbodiment, times(1)).initialize("id1","");
    }
    
    @Test
    public void testInvalidXML() throws Exception
    {
        RsbEmbodiment mockRsbEmbodiment = mock(RsbEmbodiment.class);
        whenNew(RsbEmbodiment.class).withNoArguments().thenReturn(mockRsbEmbodiment);
        
        String str = "<Loader id=\"rsbembodiment\" loader=\"asap.ipaacaembodiments.loader.IpaacaEmbodimentLoader\"/>"
                +"<characterScope characterScope=\"characterx\"/>"
                + "</Loader>";                
        RsbEmbodimentLoader loader = new RsbEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "id1", "id1", new Environment[0]);
        assertEquals(mockRsbEmbodiment, loader.getEmbodiment());
        
    }
}
