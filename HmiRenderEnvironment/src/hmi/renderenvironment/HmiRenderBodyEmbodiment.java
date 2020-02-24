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
package hmi.renderenvironment;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.environmentbase.Embodiment;
import hmi.graphics.opengl.scenegraph.GLScene;
import hmi.graphics.opengl.state.GLMaterial;
import hmi.headandgazeembodiments.EulerHeadEmbodiment;

/** Loaded through HmiRenderEmbodimentLoader. CLEAN UP USING LOMBOK! */

public class HmiRenderBodyEmbodiment implements Embodiment, SkeletonEmbodiment, EulerHeadEmbodiment
{

    private VJoint animationRoot = null;
    private float headRoll = 0, headPitch = 0, headYaw = 0;
    private float eyeRoll = 0, eyePitch = 0, eyeYaw = 0;
    private boolean headClaimed = false;
    private boolean renderEyes = false;
	private GLScene glScene = null;
	
    public void setAnimationVJoint(VJoint vj)
    {
        animationRoot = vj;
    }

    private String id = "";

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public void setGLScene(GLScene gls)
    {
        this.glScene = gls;
    }

    public VJoint getAnimationVJoint()
    {
        return animationRoot;
    }
    
    public void copy()
    {
        if(headClaimed) //see EulerHeadEmbodiment
        {
            animationRoot.getPart(Hanim.skullbase).setRollPitchYawDegrees(headRoll, headPitch, headYaw);
        }
        if(renderEyes)
        {
        	animationRoot.getPart(Hanim.l_eyeball_joint).setRollPitchYawDegrees(eyeRoll, eyePitch, eyeYaw);
        	animationRoot.getPart(Hanim.r_eyeball_joint).setRollPitchYawDegrees(eyeRoll, eyePitch, eyeYaw);
    	}
        animationRoot.calculateMatrices();
    }

    @Override
    public void setHeadRollPitchYawDegrees(float roll, float pitch, float yaw)
    {
        headRoll = roll;
        headPitch = pitch;
        headYaw = yaw;
    }

    @Override
    public void claimHeadResource()
    {
        headClaimed = true;
    }

    @Override
    public void releaseHeadResource()
    {
        headClaimed = false;
    }
	
	public void setShaderParameter(String mesh, String material, String parameter, float value)
	{
		GLMaterial glm = glScene.getGLMaterial(mesh, material);
		glm.setPupilSize(value);
	}	
}
