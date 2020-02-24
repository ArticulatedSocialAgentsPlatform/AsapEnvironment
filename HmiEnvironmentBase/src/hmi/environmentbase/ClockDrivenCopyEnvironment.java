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

import hmi.util.AnimationSync;
import hmi.util.ClockListener;
import hmi.util.SystemClock;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;

/** For example, when no HmiRender env, and we need to copy vjoint info to relion.... */
@Slf4j
public class ClockDrivenCopyEnvironment implements CopyEnvironment, ClockListener
{
    @Getter
    @Setter
    private String id = null;

    private SystemClock theClock = null;

    private boolean isShutdown = false;

    @GuardedBy("itself")
    private Set<CopyEmbodiment> copyEmbodiments = new HashSet<CopyEmbodiment>();
    private long tickSize;

    public ClockDrivenCopyEnvironment(long tickSize)
    {
        this.tickSize = tickSize;
    }

    public void initTime(double initTime)
    {
        copy();
    }

    public void time(double currentTime)
    {
        copy();
    }

    @Override
    public void requestShutdown()
    {
    	log.debug("Shutdown initiated");
        theClock.terminate();
        isShutdown = true;
    	log.debug("Shutdown finished");
    }

    @Override
    public boolean isShutdown()
    {
        return isShutdown;
    }

    protected void copy()
    {
        synchronized (AnimationSync.getSync())
        {
            synchronized (copyEmbodiments)
            {
                for (CopyEmbodiment ce : copyEmbodiments)
                {
                    ce.copy();
                }
            }
        }
    }

    public void addCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.add(ce);
        }
    }

    public void removeCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.remove(ce);
        }
    }

    /**
     * Creates a SystemClock with tickSize and hooks up this ClockDrivenCopyEnvironment to it (that is, the ClockDrivenCopyEnvironment will now
     * call copy at all its CopyEmbodiments at rate tickSize).
     */
    public void init()
    {
        theClock = new SystemClock(tickSize);
        theClock.addClockListener(this);
        theClock.start();

    }
}
