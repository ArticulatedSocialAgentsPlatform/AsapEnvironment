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
package hmi.unityembodiments.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;

import hmi.environmentbase.Environment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;
import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.loader.GenericMiddlewareLoader;
import lombok.Getter;

public class SharedMiddlewareLoader implements Environment {

	private String id;
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private HashMap<String, String> attrMap = null;
	private XMLTokenizer tokenizer;
	
	@Getter
    private Middleware middleware;
    
	public void load(String resources, String file) throws IOException {
		tokenizer = XMLTokenizer.forResource(resources, file);
        attrMap = tokenizer.getAttributes();
        for (Entry<String, String> kvp : attrMap.entrySet()) {
        	if (kvp.getKey().equals("id")) {
        		this.id = kvp.getValue();
        	}
        }

        tokenizer.takeSTag("SharedMiddlewareLoader");
        if (!tokenizer.atSTag("MiddlewareOptions")) {
            throw new XMLScanException("SharedMiddlewareLoader requires an inner MiddlewareOptions element");
        }

        attrMap = tokenizer.getAttributes();
        String loaderclass = adapter.getRequiredAttribute("loaderclass", attrMap, tokenizer);
        tokenizer.takeSTag("MiddlewareOptions");

        Properties props = new Properties();
        while (tokenizer.atSTag("MiddlewareProperty")) {
            props.put(adapter.getRequiredAttribute("name",  attrMap, tokenizer),
                      adapter.getRequiredAttribute("value", attrMap, tokenizer));
            tokenizer.takeSTag("MiddlewareProperty");
            tokenizer.takeETag("MiddlewareProperty");
        }

        tokenizer.takeETag("MiddlewareOptions");
        
        tokenizer.takeETag("SharedMiddlewareLoader");
        
        GenericMiddlewareLoader gml = new GenericMiddlewareLoader(loaderclass, props);
        middleware = gml.load();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void requestShutdown() {
		// TODO MA - should we try to close middleware here?
	}

	@Override
	public boolean isShutdown() {
		// TODO MA - ...
		return false;
	}

}
