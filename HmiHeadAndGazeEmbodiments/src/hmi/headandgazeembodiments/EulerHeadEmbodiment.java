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
package hmi.headandgazeembodiments;

import hmi.environmentbase.Embodiment;

/**
 * A simple head that can be steered with roll, pitch and yaw values
 * @author welberge
 *
 */
public interface EulerHeadEmbodiment extends Embodiment
{
    /**
     * Set the head roll, pitch and yaw (in degrees)
     * Implementations may omit setting one or more of these
     */
    void setHeadRollPitchYawDegrees(float roll, float pitch, float yaw);
    
    /**
     * Claim access to the head; other control on this embodiment may not override
     * anything set through setHeadRollPitchYawDegrees
     */
    void claimHeadResource();
    
    /**
     * Release head resouce claim set by claimHeadResource
     */
    void releaseHeadResource();    
}
