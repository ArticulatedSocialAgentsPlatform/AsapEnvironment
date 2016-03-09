/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package hmi.physicsembodiments;

import hmi.environmentbase.Embodiment;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.mixed.MixedSystem;

import java.util.ArrayList;


/**
Provides access to the physical humanoid and mixed systems of a virtual human.
*/
public interface PhysicalEmbodiment extends Embodiment
{
    /** associated with getPhysicalHumans, same order */
    ArrayList<MixedSystem> getMixedSystems();

    /** associated with getMixedSystems, same order */
    ArrayList<PhysicalHumanoid> getPhysicalHumans();

    /** called by the animatino engine, after the reset pose has been set (!) */
    void glueFeetToFloor();
  
}