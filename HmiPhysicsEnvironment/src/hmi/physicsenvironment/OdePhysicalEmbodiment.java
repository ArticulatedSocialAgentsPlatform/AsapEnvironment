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
package hmi.physicsenvironment;

import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.physicsembodiments.PhysicalEmbodiment;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.odejava.JointGroup;


/** Take care of its own loading from XML. */
@Slf4j
public class OdePhysicalEmbodiment implements PhysicalEmbodiment, EmbodimentLoader
{

    // some parameters for caching during XML loading
    private XMLStructureAdapter adapter = new XMLStructureAdapter();
    private boolean glueFeetToFloor = false;
    private MixedSkeletonEmbodiment mse = null;
    private OdePhysicsEnvironment ope = null;

    @Override
    public void unload()
    {
        log.debug("Removing VH from Physics");
        // remove from physics
        for (PhysicalHumanoid nextPH : getPhysicalHumans())
        {
            ope.clearPhysicalHumanoid(nextPH);
        }
        ope.clearFeetGlueJointGroup(feetGlueJointGroup);
    }

    private ArrayList<MixedSystem> mixedSystems = null;

    /** associated with getPhysicalHumans, same order */
    public ArrayList<MixedSystem> getMixedSystems()
    {
        return mixedSystems;
    }

    private ArrayList<PhysicalHumanoid> physicalHumans = null;

    /** associated with getMixedSystems, same order */
    public ArrayList<PhysicalHumanoid> getPhysicalHumans()
    {
        return physicalHumans;
    }

    private JointGroup feetGlueJointGroup = new JointGroup();

    @Getter
    @Setter
    private String id = "";

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments, Loader ... requiredLoaders) 
            throws IOException
    {
        id = loaderId;

        for (Environment e : environments)
        {
            if (e instanceof OdePhysicsEnvironment) ope = (OdePhysicsEnvironment) e;
        }
        for (Loader e : requiredLoaders)
        {
            if (e instanceof EmbodimentLoader && ((EmbodimentLoader) e).getEmbodiment() instanceof MixedSkeletonEmbodiment)
            {
                mse = (MixedSkeletonEmbodiment) ((EmbodimentLoader) e).getEmbodiment();
            }
        }
        if (ope == null)
        {
            throw new RuntimeException("OdePhysicalEmbodiment requires an Environment of type OdePhysicalEnvironment");
        }
        if (mse == null)
        {
            throw new RuntimeException("OdePhysicalEmbodiment requires an EmbodimentLoader containing a MixedSkeletonEmbodiment");
        }

        // add default empty phuman and mixed system
        physicalHumans = new ArrayList<PhysicalHumanoid>();
        mixedSystems = new ArrayList<MixedSystem>();

        OdeHumanoid pHumanEmpty = ope.createPhysicalHumanoid(vhId + "_empty_" + loaderId);
        physicalHumans.add(pHumanEmpty);

        float[] g = new float[] { 0, 0, 0 };
        MixedSystem mSystem = ope.createMixedSystem(g, pHumanEmpty);
        mixedSystems.add(mSystem);

        // read remaining mixed systems from tokenizer
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer, vhId);
        }

    }

    protected void readSection(XMLTokenizer tokenizer, String vhId) throws IOException
    {
        // HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("GlueFeetToFloor"))
        {
            glueFeetToFloor = true;

            tokenizer.takeSTag("GlueFeetToFloor");
            tokenizer.takeETag("GlueFeetToFloor");
        }
        else if (tokenizer.atSTag("MixedSystems"))
        {

            float[] g = new float[] { 0, 0, 0 };
            int i = 0;
            tokenizer.takeSTag("MixedSystems");
            while (tokenizer.atSTag())
            {
                String tag = tokenizer.getTagName();
                if (!tag.equals("MixedSystem")) throw new XMLScanException("Unknown tag in MixedSystemss: " + tag);
                HashMap<String, String> attrMap2 = tokenizer.getAttributes();
                String msFile = adapter.getRequiredAttribute("filename", attrMap2, tokenizer);
                String msName = adapter.getRequiredAttribute("name", attrMap2, tokenizer);
                String msResources = adapter.getOptionalAttribute("resources", attrMap2, "");
                i = physicalHumans.size();
                OdeHumanoid pHuman = ope.createPhysicalHumanoid(vhId + "_" + getId() + "_" + i + "_" + msName);

                g = new float[] { 0, -9.8f, 0 };
                MixedSystem mSystem = null;
                try
                {
                    mSystem = ope.createMixedSystem(g, pHuman, mse.getCurrentVJoint(), msResources, msFile);
                }
                catch (IOException ex)
                {
                    log.error("Cannot load mixed system {} from file \"{}\"; dropping mixed system.", msName, msFile);
                    continue;
                }

                mixedSystems.add(mSystem);
                physicalHumans.add(pHuman);

                // init phuman, set collision etc
                ope.initPhysicalHumanoid(pHuman);

                tokenizer.takeSTag(tag);
                tokenizer.takeETag(tag);
            }
            tokenizer.takeETag("MixedSystems");
        }
        else
        {
            throw tokenizer.getXMLScanException("Unknown tag in Loader content");
        }
    }

    /** called by the animation engine, after the reset pose has been set (!) */
    public void glueFeetToFloor()
    {

        // after everything was loaded, we only need to glue the feet to the floor
        if (glueFeetToFloor)
        {
            for (int i = 1; i < physicalHumans.size(); i++)
            { // not the first one!! because the first one is an empty ph, not
              // having any feet!
                OdeHumanoid pHuman = (OdeHumanoid) physicalHumans.get(i);
                ope.glueFeetToFloor(pHuman, feetGlueJointGroup);
            }
        }
    }

    /** Return this embodiment */
    @Override
    public Embodiment getEmbodiment()
    {
        return this;
    }

}
