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

import hmi.math.Vec3f;
import hmi.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;

import com.google.common.base.Splitter;


/**
 * Keeps track of WorldObjects
 * @author Herwin
 * 
 */
public class WorldObjectManager
{
    private HashMap<String, WorldObject> worldObjectMap;

    public WorldObjectManager()
    {
        worldObjectMap = new HashMap<String, WorldObject>();
    }

    public void addWorldObject(String id, WorldObject obj)
    {
        worldObjectMap.put(id, obj);
    }

    /**
     * Get worldobject with id id. If id is of the form x,y,z returns a new 
     * AbsolutePositionWorldObject representing this global position.
     */
    public WorldObject getWorldObject(String id)
    {
        if (worldObjectMap.containsKey(id))
        {
            return worldObjectMap.get(id);
        }

        Iterable<String> pos = Splitter.on(',').trimResults().split(id);
        Iterator<String> iter = pos.iterator();
        float worldPos[] = Vec3f.getVec3f();
        int i = 0;
        while (iter.hasNext())
        {
            String val = iter.next();
            if(StringUtil.isNumeric(val))
            {
                worldPos[i]=Float.parseFloat(val);
            }
            else
            {
                return null;
            }
            i++;
            if(i>3)return null;
        }  
        if(i==3)
        {
            return new AbsolutePositionWorldObject(worldPos);
        }
        return null;
    }
}
