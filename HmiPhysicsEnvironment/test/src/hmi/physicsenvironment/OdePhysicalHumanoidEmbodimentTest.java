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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hmi.animationembodiments.MixedSkeletonEmbodiment;
import hmi.environmentbase.Environment;
import hmi.testutil.animation.HanimBody;
import hmi.xml.XMLTokenizer;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
/**
 * Unit test cases for the OdePhysicalHumanoidEmbodiment
 * @author Herwin
 */
public class OdePhysicalHumanoidEmbodimentTest
{
    MixedSkeletonEmbodiment mockMixedSkeletonEmbodiment = mock(MixedSkeletonEmbodiment.class);
    MixedSkeletonEmbodimentLoader mockmseLoader = mock(MixedSkeletonEmbodimentLoader.class);    
    OdePhysicsEnvironment odePhEnv = new OdePhysicsEnvironment();
    
    @Test
    @Ignore     //ignored for now, introduces unwanted Elckerlyc/AsapRealizer dependency
    public void testReadEmptyXML() throws IOException
    {
        odePhEnv.init();
        OdePhysicalHumanoidEmbodiment emb = new OdePhysicalHumanoidEmbodiment();
        
        Environment[] environments ={odePhEnv};
        //AsapVirtualHuman avh = new AsapVirtualHuman();
        when(mockmseLoader.getEmbodiment()).thenReturn(mockMixedSkeletonEmbodiment);
        when(mockMixedSkeletonEmbodiment.getCurrentVJoint()).thenReturn(HanimBody.getLOA1HanimBody());
        
        String str = "<Loader><PhysicalHumanoidDef filename=\"Humanoids/armandia/physicalmodels/armandia_ph.xml\" resources=\"\"/></Loader>";
        XMLTokenizer tok = new XMLTokenizer(str);
        tok.takeSTag("Loader");
        emb.readXML(tok, "embodiment1","embodiment1","embodiment1", environments, mockmseLoader);
        
        assertNotNull(emb.getPhysicalHuman());
    }
}
