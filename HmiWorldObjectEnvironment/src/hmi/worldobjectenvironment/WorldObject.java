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
package hmi.worldobjectenvironment;

import hmi.animation.VJoint;

/**
 * Contains an entity in the world that can be pointed at, talked about etc.
 * @author welberge
 */
public interface WorldObject
{
    /**
     * Get the world position of the object
     * @param tr output: the world position of the object
     */
    void getWorldTranslation(float[] tr);

    /**
     * Get the position of the world object in the coordinate system of vj.
     * This joint and vj are assumed to be in the same joint tree, but can be in different branches of the tree.
     * @param tr output: the position of the world object
     */
    void getTranslation(float tr[], VJoint vj);

    /**
     * Get the position of the world object in the coordinate system of vj, minus the rotation of vj.
     * This joint and vj are assumed to be in the same joint tree, but can be in different branches of the tree.
     * @param tr output: the position of the world object
     */
    void getTranslation2(float tr[], VJoint vj);
    
    /**
     * Set the worldObject to position tr
     */
    void setTranslation(float tr[]);
}
