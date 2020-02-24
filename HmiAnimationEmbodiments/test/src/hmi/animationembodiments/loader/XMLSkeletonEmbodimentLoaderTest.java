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
package hmi.animationembodiments.loader;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import hmi.environmentbase.Environment;
import hmi.xml.XMLTokenizer;

import org.junit.Test;

/**
 * Unit tests for the XMLSkeletonEmbodimentLoader
 * @author Herwin
 *
 */
public class XMLSkeletonEmbodimentLoaderTest
{
    @Test
    public void test() throws IOException
    {
        //@formatter:off
        String str = "<Loader id=\"id1\" loader=\"hmi.animationembodiments.loader.XMLSkeletonEmbodimentLoader\">" +
        		        "<XMLSkeletonSection resources=\"\" filename=\"Humanoids/armandia/skeleton/armandia_skel.xml\"/>" +
        		     "</Loader>";
        //@formatter:on
        XMLSkeletonEmbodimentLoader loader = new XMLSkeletonEmbodimentLoader();
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag();
        loader.readXML(tok, "id1", "vh1", "vh1", new Environment[0]);
        assertNotNull(loader.getEmbodiment());
    }
}
