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
import java.util.List;

import lombok.Getter;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

/**
 * Loader for &lt;Inputs ids="input1,input2,input3"/&gt;
 * @author hvanwelbergen
 */
public class InputsLoader extends XMLStructureAdapter
{
    @Getter
    private List<String> ids;

    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        ids = decodeStringList(getRequiredAttribute("ids", attrMap, tokenizer));
    }

    public String getXMLTag()
    {
        return XMLTAG;
    }

    public static final String XMLTAG = "Inputs";
}
