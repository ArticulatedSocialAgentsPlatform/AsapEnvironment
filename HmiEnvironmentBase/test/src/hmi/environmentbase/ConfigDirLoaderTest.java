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
package hmi.environmentbase;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * unit tests for the configdirloader
 * @author hvanwelbergen
 *
 */
public class ConfigDirLoaderTest
{
    private ConfigDirLoader loader;
    
    @Before
    public void setup()
    {
        System.setProperty("user.dir","/test");
        System.setProperty("shared.project.root","/testroot");        
        loader = new ConfigDirLoader("CONF","CONFTAG");        
    }
    
    @Test
    public void testDefault()
    {
        assertEquals("/test/lib/CONF",loader.getConfigDir());
    }
    
    @Test
    public void testDefaultWithParse()
    {
        String config = "<CONFTAG/>";
        loader.readXML(config);
        assertEquals("/test/lib/CONF",loader.getConfigDir());
    }
    
    @Test
    public void testGlobal()
    {
        String config = "<CONFTAG dir=\"globaldir\"/>";
        loader.readXML(config);
        assertEquals("globaldir",loader.getConfigDir());
    }
    
    @Test
    public void testLocal()
    {
        String config = "<CONFTAG localdir=\"localdir\"/>";
        loader.readXML(config);
        assertEquals("/testroot/localdir",loader.getConfigDir());
    }
}
