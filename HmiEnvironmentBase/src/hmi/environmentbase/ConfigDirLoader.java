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

import java.util.HashMap;

import lombok.Getter;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Provides a way to specify a directory for e.g. a Loader. 
 * The directory is specified either by localdir (relative to shared.project.root), by dir (absolute path), 
 * or not at all (in which case its defaults to /lib/&lt;CONFIGNAME&gt; ).
 * @author hvanwelbergen
 */
public class ConfigDirLoader extends XMLStructureAdapter
{
    private final String CONFIGNAME;
    private final String XMLTAG;
    
    @Getter
    private String configDir;
    
    public ConfigDirLoader(String configName, String configTag)
    {
        CONFIGNAME = configName;
        XMLTAG = configTag;
        configDir = System.getProperty("user.dir") + "/lib/"+CONFIGNAME;
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        String localDir = getOptionalAttribute("localdir", attrMap);
        String dir = getOptionalAttribute("dir", attrMap);
        if (dir == null)
        {
            if (localDir != null)
            {
                String spr = System.getProperty("shared.project.root");
                if (spr == null)
                {
                    throw tokenizer.getXMLScanException("the use of the localdir setting "
                            + "requires a shared.project.root system variable (often: -Dshared.project.root=\"../..\" "
                            + "but this may depend on your system setup).");
                }
                configDir = System.getProperty("shared.project.root") + "/" + localDir;
            }
        }
        else
        {
            configDir = dir;
        }
    }
    
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
