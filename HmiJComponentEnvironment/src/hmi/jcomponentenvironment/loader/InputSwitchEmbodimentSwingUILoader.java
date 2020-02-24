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

import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.InputSwitchEmbodiment;
import hmi.environmentbase.Loader;
import hmi.jcomponentenvironment.InputSwitchEmbodimentSwingUI;
import hmi.util.ArrayUtils;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import lombok.Getter;

/**
 * Loader for the VJointSwitchEmbodimentSwingUI
 * @author hvanwelbergen
 * 
 */
public class InputSwitchEmbodimentSwingUILoader implements Loader
{
    @Getter
    private String id;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        InputSwitchEmbodiment switchEmbodiment = null;
        for (EmbodimentLoader ebl : ArrayUtils.getClassesOfType(requiredLoaders, EmbodimentLoader.class))
        {
            if (ebl.getEmbodiment() instanceof InputSwitchEmbodiment)
            {
                switchEmbodiment = (InputSwitchEmbodiment) ebl.getEmbodiment();
            }
        }
        if (switchEmbodiment == null)
        {
            throw new RuntimeException("VJointSwitchEmbodimentSwingUILoader requires a VJointSwitchEmbodimentLoader.");
        }

        JComponentEmbodimentLoader jceLoader = ArrayUtils.getFirstClassOfType(requiredLoaders, JComponentEmbodimentLoader.class);
        if (jceLoader == null || jceLoader.getEmbodiment() == null)
        {
            throw new RuntimeException("VJointSwitchEmbodimentSwingUILoader requires a JComponentEmbodimentLoader.");
        }

        InputSwitchEmbodimentSwingUI ui = new InputSwitchEmbodimentSwingUI(id, switchEmbodiment);
        jceLoader.getEmbodiment().addJComponent(ui.getJComponent());
    }

    @Override
    public void unload()
    {

    }
}
