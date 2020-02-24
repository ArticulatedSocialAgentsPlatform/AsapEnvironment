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
package hmi.mixedanimationenvironment;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.doubleThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import hmi.animation.VJoint;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the MixedAnimationPlayerManager
 * @author Herwin
 *
 */
public class MixedAnimationPlayerManagerTest
{
    private PhysicsCallback mockPhysicsCallback = mock(PhysicsCallback.class);
    private MixedAnimationPlayer mockAniPlayer = mock(MixedAnimationPlayer.class);
    private MixedAnimationPlayerManager apm;
    private static final float PRECISION = 0.001f;
    @Before
    public void setup()
    {
        apm = new MixedAnimationPlayerManager(mockPhysicsCallback);
        apm.addAnimationPlayer(mockAniPlayer, new VJoint(), new VJoint());
    }
    
    @Test
    public void testZeroTime()
    {
        apm.time(0);
        verify(mockPhysicsCallback,times(0)).time(anyFloat());
        verify(mockAniPlayer,times(0)).playStep(anyDouble());
    }
    
    @Test
    public void testTwoTimes()
    {
        apm.time(0.007f);
        verify(mockPhysicsCallback,times(2)).time(0.003f);
        verify(mockAniPlayer,times(2)).playStep(anyDouble());
        verify(mockAniPlayer,times(1)).playStep(doubleThat(closeTo(0.003d,PRECISION)));
        verify(mockAniPlayer,times(1)).playStep(doubleThat(closeTo(0.006d,PRECISION)));
    }
    
    @Test
    public void testH()
    {
        apm = new MixedAnimationPlayerManager(mockPhysicsCallback, 0.5f);
        apm.addAnimationPlayer(mockAniPlayer, new VJoint(), new VJoint());
        apm.time(1.1);
        verify(mockPhysicsCallback,times(2)).time(0.5f);
        verify(mockAniPlayer,times(2)).playStep(anyDouble());
        verify(mockAniPlayer,times(1)).playStep(doubleThat(closeTo(0.5d,PRECISION)));
        verify(mockAniPlayer,times(1)).playStep(doubleThat(closeTo(1d,PRECISION)));        
    }
}
