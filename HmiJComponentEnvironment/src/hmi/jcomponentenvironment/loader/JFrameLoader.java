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

import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.util.ArrayUtils;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;

/**
 * Loads a JFrameEmbodiment and registers it to the JComponentEnvironment
 * @author hvanwelbergen
 * 
 */
public class JFrameLoader implements Loader
{
    @Getter
    @Setter
    private String id = "";

    private class JFrameInfo extends XMLStructureAdapter
    {
        @Getter
        private int width = 640;
        @Getter
        private int height = 480;
        @Getter
        private String title ="";
        
        public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
        {
            width = this.getOptionalIntAttribute("width", attrMap, width);
            height = this.getOptionalIntAttribute("height", attrMap, height);
            title = this.getOptionalAttribute("title", attrMap, title);
        }

        public String getXMLTag()
        {
            return XMLTAG;
        }

        public static final String XMLTAG = "JFrame";
    }
    
    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        final JComponentEnvironment jce = ArrayUtils.getFirstClassOfType(environments, JComponentEnvironment.class);
        if (jce == null)
        {
            throw tokenizer.getXMLScanException("JFrameEmbodimentLoader requires an JComponentEnvironment");
        }

        JFrameInfo jfi = new JFrameInfo();
        if (tokenizer.atSTag())
        {
            String tag = tokenizer.getTagName();
            if (tag.equals(JFrameInfo.XMLTAG))
            {
                jfi.readXML(tokenizer);
            }
        }
        
        final String title = jfi.getTitle();
        final int width = jfi.getWidth();
        final int height = jfi.getHeight();
        final String frameid = id;
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    JFrame frame = new JFrame(title);
                    frame.setSize(width, height);
                    frame.setVisible(true);
                    JPanel p = new JPanel();
                    frame.add(p);
                    jce.registerComponent(frameid, p);
                }
            });
        }
        catch (InvocationTargetException | InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unload()
    {

    }
}
