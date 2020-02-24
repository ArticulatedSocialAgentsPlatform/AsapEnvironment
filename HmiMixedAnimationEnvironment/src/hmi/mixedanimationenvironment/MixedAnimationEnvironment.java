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

import hmi.animation.VJoint;
import hmi.environmentbase.Environment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.physicsenvironment.PhysicsUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MixedAnimationEnvironment maintains the AnimationPlayers and AnimationPlayerManager; it maintains the multiple mixed dynamics animation joints of virtual humans;
 * and it maintains a connection to the PhysicsEnvironment to do the physics stepping.
 * 
 * @author Dennis Reidsma
 */
public class MixedAnimationEnvironment implements Environment, PhysicsUpdater
{
    private String id = "mixedanimationenvironment";

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    private Logger logger = LoggerFactory.getLogger(MixedAnimationEnvironment.class.getName());

    protected Object shutdownSync = new Object();

    protected volatile boolean shutdownPrepared = false;

    /** */
    protected MixedAnimationPlayerManager theAnimationPlayerManager = null;

    protected OdePhysicsEnvironment ope;

    public void init(OdePhysicsEnvironment ope, float h)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            theAnimationPlayerManager = new MixedAnimationPlayerManager(new PhysCallback(), h);
            this.ope = ope;
            // steal the responsibility for time-stepping the physics simulation (by default, ope takes care of this itself, 
            //but now we want to do this through the animationplayermanager)
            ope.setPhysicsUpdater(this); 
        }
    }
    
    public void init(OdePhysicsEnvironment ope)
    {
        init(ope, MixedAnimationPlayerManager.DEFAULT_PHYSICS_STEPTIME);        
    }

    /**
     * This environment registers itself as the PhysicsUpdater. Thereeby it "steals" the physicsupdate 
     * function from OPE and delegates it to the animationplayermanager. note
     * that in turn animationplayermanager will call the PhysicsCallback.time, which again gets looped back to OPE.physicsTick
     */
    public void physicsUpdate(double currentTime)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            theAnimationPlayerManager.time(currentTime); // goes in to animationenvironmkent
        }
    }

    // this is the callback to perform one timestep in physical simulation. Loop this back to the OdePhysicsEnvironment
    class PhysCallback implements PhysicsCallback
    {
        @Override
        public void time(float timeDiff)
        {
            synchronized (shutdownSync)
            {
                if (shutdownPrepared) return;
                ope.physicsTick(timeDiff);
            }
        }
    }

    public void addAnimationPlayer(MixedAnimationPlayer ap, VJoint curr, VJoint anim)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            theAnimationPlayerManager.addAnimationPlayer(ap, curr, anim);
        }
    }

    public void removeAnimationPlayer(MixedAnimationPlayer ap, VJoint curr, VJoint anim)
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            theAnimationPlayerManager.removeAnimationPlayer(ap, curr, anim);
        }
    }

    @Override
    public void requestShutdown()
    {
        synchronized (shutdownSync)
        {
            if (shutdownPrepared) return;
            logger.info("Prepare shutdown of MixedAnimationEnvironment...");
            shutdownPrepared = true;
        }
    }

    @Override
    public boolean isShutdown()
    {
        synchronized (shutdownSync)
        {
            return shutdownPrepared;
        }
    }
    
    public float getH()
    {
        return theAnimationPlayerManager.getH();
    }
}
