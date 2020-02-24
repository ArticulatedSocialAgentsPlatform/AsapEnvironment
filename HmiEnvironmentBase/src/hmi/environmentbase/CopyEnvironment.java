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
package hmi.environmentbase;



/**
Environment that is responsible for calling copy() on a set of {@link CopyEmbodiment 
CopyEmbodiments} at appropriate times.

A Player has a plan containing PlanUnits. These PlanUnits operate on an Embodiment. 
At play(t) time, when all current planunits have finished, the result needs to be 
copied to the embodiment. A CopyEnvironment is an environment responsible for 
making sure this copy() is performed.

ANIMATIONENGINE
MotionUnits operate on a VJoint (part of a SkeletonEmbodiment). If the HmiRenderEnvironment 
is used, the copy() action involves calling calculateMatrices(). If rendering is done in Ogre, 
SUIT, etc, the copy() also involves sending the resulting joint configuration to the render
system.

FACEENGINE
FaceUnits operate on a FaceController (part of a FaceEmbodiment). If the HmiRenderEnvironment 
is used (so, a {@link hmi.renderenvironment.HmiRenderBodyAndFaceEmbodiment}), the copy() action involves taking the  
current Mpeg4config and actually applying it to the OpenGL head, and setting the morph targets 
of the graphical body (see {@link hmi.renderenvironment.HmiRenderBodyAndFaceEmbodiment#copy()}. For other environments it may again
involve some UDP or TCPIP communication

A CopyEnvironment is an environment responsible for making sure this copy() is performed.
In a typical HMI setup, HmiRenderEnvironment does this just prior to actually rendering a frame.
If no HmiRenderEnvironment is present, you may e.g. use a ClockDrivenCopyEnvironment that does 
this on the time() of a SystemClock.

*/ 

public interface CopyEnvironment extends Environment
{
//TODO:  IT SEEMS THAT THE COPY ACTION SHOULD ALWAYS HAPPEN SYNCED TO ANIMSYNC, AND IN THE FUTURE ALSO PHYSICSSYNC ?! Uitzoeken en documenteren
  void addCopyEmbodiment(CopyEmbodiment ce);
  
  void removeCopyEmbodiment(CopyEmbodiment ce);

}