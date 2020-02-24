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
import hmi.math.Mat4f;
import hmi.math.Vec3f;
import hmi.util.AnimationSync;

/**
 * A world object coupled to a VJoint
 * @author welberge
 */
public class VJointWorldObject implements WorldObject
{
    private VJoint joint;
        
    public VJointWorldObject(VJoint vj)
    {
        joint = vj;
    }
    
   
    public void getWorldTranslation(float[]tr)
    {
        synchronized(AnimationSync.getSync())
        {
            joint.calculateMatrices();
            joint.getPathTranslation(null, tr);
        }
    }    
    
    
    public void getTranslation(float tr[], VJoint vj)
    {
        float[] mTemp1 = Mat4f.getIdentity();
        float trTemp[] = new float[3];
        
        
        synchronized(AnimationSync.getSync())
        {
            joint.getPathTranslation(null, trTemp);     //world pos of the joint
            if(vj!=null)
            {
                vj.getPathTransformMatrix(null, mTemp1);
            }            
        }
        Mat4f.invertRigid(mTemp1);
        Mat4f.transformPoint(mTemp1, tr, trTemp);        
    }
   
    public void getTranslation2(float tr[], VJoint vj)
    {
        float trTempToParent[] = Vec3f.getVec3f(0,0,0);
        float trTemp[] = new float[3];
        float[] mTemp1 = Mat4f.getIdentity();
        
        synchronized(AnimationSync.getSync())
        {
            joint.getPathTranslation(null, trTemp);     //world pos of the joint
            
            if(vj!=null)
            {
                vj.getParent().getPathTransformMatrix(null, mTemp1);            
                vj.getTranslation(trTempToParent);
            }           
        }
        Mat4f.invertRigid(mTemp1);
        Mat4f.transformPoint(mTemp1, tr, trTemp);
        Vec3f.sub(tr, trTempToParent);
    }


    @Override
    public void setTranslation(float[] tr)
    {
        joint.setTranslation(tr);        
    }
}
