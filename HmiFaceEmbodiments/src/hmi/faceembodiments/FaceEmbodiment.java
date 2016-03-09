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
package hmi.faceembodiments;

import hmi.environmentbase.CopyEmbodiment;
import hmi.faceanimation.FaceController;

/**
Provides access to an animatable face.

NOTE the FaceController needs to be split in two.... one for the MPEG4, one for the morph targets... as these are independent controls...

*/
public interface FaceEmbodiment extends CopyEmbodiment
{
  /** Return the FaceController through which the MPEG4 will be controlled */
  FaceController getFaceController();
}