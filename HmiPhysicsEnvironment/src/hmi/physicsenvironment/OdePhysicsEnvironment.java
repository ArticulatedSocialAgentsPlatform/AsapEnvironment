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
package hmi.physicsenvironment;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.environmentbase.CopyEmbodiment;
import hmi.environmentbase.Environment;
import hmi.physics.JointType;
import hmi.physics.PhysicalHumanoid;
import hmi.physics.PhysicalSegment;
import hmi.physics.assembler.MixedSystemAssembler;
import hmi.physics.mixed.MixedSystem;
import hmi.physics.ode.OdeHumanoid;
import hmi.physics.ode.OdeJoint;
import hmi.physics.ode.OdePhysicalSegment;
import hmi.physics.ode.OdeRigidBody;
import hmi.util.AnimationSync;
import hmi.util.Clock;
import hmi.util.ClockListener;
import hmi.util.PhysicsSync;
import hmi.util.RenderSync;
import hmi.util.Resources;
import hmi.util.SystemClock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;

import org.odejava.GeomPlane;
import org.odejava.HashSpace;
import org.odejava.JointGroup;
import org.odejava.Odejava;
import org.odejava.Space;
import org.odejava.World;
import org.odejava.collision.JavaCollision;
import org.odejava.ode.OdeConstants;


/**
 * 
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public class OdePhysicsEnvironment implements Environment, PhysicsUpdater
{
    private String id = "odephysicsenvironment";

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    
    // ========= PHYSICS SIMULATION SETTINGS ==========

    /** If, at any time, set to false, no physics are calculated */
    public boolean runphysics = true;

    /**
     * physics is calculated on about 2 times render speed: you want every render tick to surely
     * have latest physics results in it, or at most one animatino step (half a render frame time)
     * behind time... Note: this is NOT the resolution with which physics is simulated; that is
     * determined by timeStep or by the AnimationPlayerManager.getH()
     */
    public long physicsLoopFrequency = 100;

    /** Simulate collisions yes or no? */
    public boolean collisionEnabled = false;

    /** Direction and strength of gravity in this environment */
    protected float[] g = new float[] { 0, -9.8f, 0 };

    /**
     * Stepsize for physics simulation in seconds. Note that in the ElckerlycEnvironment
     * this value is ignored.
     */
    protected float timeStep = 0.003f;

    // ========= PHYSICS SIMULATION ==========

    protected Sleeper sleeper = null;

    @GuardedBy("itself")
    protected List<Runnable> physicsRunners = Collections.synchronizedList(new ArrayList<Runnable>());

    /** Set to true when shutdown process has been completed */
    protected volatile boolean isShutdown = false;

    /** The Physical World */
    @GuardedBy("PhysicsSync.getSync()")
    protected World phworld;

    /** Collison space? */
    @GuardedBy("PhysicsSync.getSync()")
    protected HashSpace space;

    /** To calculate collisions and their effecting forces/impulses. */
    @GuardedBy("PhysicsSync.getSync()")
    protected JavaCollision collision;

    /** The clock on which steps in the physical simulation are taken */
    @GuardedBy("PhysicsSync.getSync()")
    protected SystemClock physicsClock;

    /**
     * a class can register itself as a prePhysicsCopyListener
     * -- and then it will get a callback AFTER the physicssimulation has run, but BEFORE the results are copied onto the body
     */
    @GuardedBy("PhysicsSync.getSync()")
    protected List<ClockListener> prePhysicsCopyListeners = new ArrayList<ClockListener>();
    
    /** a class can register itself as a physicsUpdater -- and thereby make itself responsible for the stepping of the physics simulation */
    @GuardedBy("PhysicsSync.getSync()")
    protected PhysicsUpdater physicsUpdater;

    /**
     * last time stamp for which physics was simulated. Note that in ElckerlycEnvironment
     * prevTime is ignored because taking phsyics steps is delegated to the
     * animationplayermanager
     */
    @GuardedBy("PhysicsSync.getSync()")
    protected double prevTime = 0;

    /** By default, the physical world contains this physical representation of the Visual ground */
    @GuardedBy("PhysicsSync.getSync()")
    protected GeomPlane groundGeom;

    /** all embodiments that must be "copied" on the physicsloop */
    @GuardedBy("itself")
    protected List<CopyEmbodiment> copyEmbodiments = new ArrayList<CopyEmbodiment>();

    @GuardedBy("itself")
    protected List<OdeRigidBody> rigidBodies = new ArrayList<OdeRigidBody>();

    /*
     * ===================
     * =================== CONSTRUCTION AND INITIALISATION
     * ===================
     */

    /**
     * Init the world physics before initializing the rest of the system; after initialization, set
     * up the physics of the ground
     */
    public void init()
    {
        synchronized (PhysicsSync.getSync())
        {
            initWorldPhysics();
            initGroundPhysics();
            physicsUpdater = this;
        }
    }

    /**
     * Initialise the world physics, collision space, etc When called from init, this is garanteed
     * to be locked on the physics sync
     */
    protected void initWorldPhysics()
    {
        Odejava.init();

        // Create ODE world
        phworld = new World();
        // Set gravity along Y axis
        phworld.setGravity(g[0], g[1], g[2]);
        phworld.setContactSurfaceThickness(0.001f);
        // phworld.setConstantForceMix(0.0001f);

        space = new HashSpace();
        collision = new JavaCollision(phworld);

        collision.setSurfaceMode(OdeConstants.dContactApprox1);
        collision.setSurfaceMu(0.9f);
        collision.setSurfaceBounce(0.9f);

        physicsClock = new SystemClock(1000 / physicsLoopFrequency, "DefaultPhysicsEnvironmentClock");
        physicsClock.addClockListener(new MyPhysicsClockListener());

    }

    /**
     * Initialise the default physical representation of the ground The default invocation from init
     * is synchronized to the PhysicsSync
     */
    protected void initGroundPhysics()
    {
        groundGeom = new GeomPlane(0f, 1f, 0f, 0f);
        space.add(groundGeom);
    }

    /**  */
    public void startPhysicsClock()
    {
        // hack to enforce high res timing
        sleeper = new Sleeper();
        sleeper.start();
        prevTime = 0;
        physicsClock.start();
    }

    public void toggleCollision()
    {
        setCollisionEnabled(!collisionEnabled);
    }

    public void togglePhysics()
    {
        setPhysicsEnabled(!runphysics);
    }

    /*
     * ===================
     * =================== PHYSICS CLOCK AND PHYSICS RUNNERS
     * ===================
     */

    /**
     * Return a version of the physics clock for which access to getMediaSeconds is guaranteed to be ynchronized by the PhysicsSync
     */
    public Clock getPhysicsClock()
    {
        return new Clock(){
        	
        	@Override
        	public double getMediaSeconds()
        	{
        		synchronized (PhysicsSync.getSync())
        		{
        			return physicsClock.getMediaSeconds();
        		}
        	}

			@Override
			public void addClockListener(ClockListener listener) {
				physicsClock.addClockListener(listener);
			}
        };
    }

    /** Links physics clock to the physicsTime method in which the simulation should be updated */
    class MyPhysicsClockListener implements ClockListener
    {
        public MyPhysicsClockListener()
        {

        }

        @Override
        public void time(double currentTime)
        {
            physicsTime(currentTime);
        }

        @Override
        public void initTime(double initTime)
        {
        }
    }

    /**
     * Add a runner to the physics thread. These runners is executed as a last step in physicsTime.
     * Here the proper synchronisation is in place to do 'stuff' with the physical simulator or
     * physics clock (e.g. reset it). The Runnable itself may not call addPhysicsRunner or in any
     * other way tinker with physicsRunners.
     */
    public void addPhysicsRunner(Runnable r)
    {
        synchronized (physicsRunners)
        {
            if (physicsRunners.isEmpty() || !(physicsRunners.get(physicsRunners.size() - 1) instanceof PoisonPillRunnable))
            {
                physicsRunners.add(r);
            }
        }
    }

    /** Resume simulation if physics clock was paused; otherwise, no effect. Must be called on the physics thread using requestPlay(). */
    protected void play()
    {
        synchronized (PhysicsSync.getSync())
        {
            physicsClock.start();
        }
    }

    /** Request the simulation to resume, if it was paused */
    public void requestPlay()
    {
        addPhysicsRunner(new Runnable()
        {
            public void run()
            {
                play();
            }
        });
    }

    /** Pause physics simulation. Must be called on the physics thread using requestPause(). */
    protected void pause()
    {
        synchronized (PhysicsSync.getSync())
        {
            physicsClock.pause();
        }
    }

    /** Request the simulation to pause */
    public void requestPause()
    {
        addPhysicsRunner(new Runnable()
        {
            public void run()
            {
                pause();
            }
        });
    }

    /** Reset the physics simulation: reset some of the collision info. */
    protected void reset()
    {
        collision.emptyContactGroup();
        physicsClock.setMediaSeconds(0);
    }

    /** Request a reset */
    public void requestReset()
    {
        addPhysicsRunner(new Runnable()
        {
            public void run()
            {
                reset();
            }
        });
    }

    /** Terminate render clock. Must be called on the physics thread using requestShutdown(). After this, the clock can no longer be started. */
    protected void shutdown()
    {
        log.info("Shutdown of OdePhysicsEnvironment starts...");
        physicsClock.terminate();
        cleanUpOdeObjects();
        log.info("Shutdown of OdePhysicsEnvironment finished");
    }

    /** clean up ODE upon termination. Must be called on the physics thread using requestShutdown(). */
    private void cleanUpOdeObjects()
    {
        space.delete();
        collision.delete();
        phworld.delete();
        Odejava.close();
    }

    @Override
    public void requestShutdown()
    {
        addPhysicsRunner(new PoisonPillRunnable()
        {
            public void run()
            {
                shutdown();
                isShutdown = true;
            }
        });

    }

    @Override
    public boolean isShutdown()
    {
        return isShutdown;
    }

    /*
     * ===================
     * =================== THE PHYSICS SIMULATION PIPELINE
     * ===================
     */

    /** these embodiments are copied on the physicsclock loop */
    public void addPhysicsCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (PhysicsSync.getSync())
        {
            copyEmbodiments.add(ce);
        }
    }

    /** these embodiments are copied on the physicsclock loop */
    public void removePhysicsCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (PhysicsSync.getSync())
        {
            copyEmbodiments.remove(ce);
        }
    }

    public OdeJoint createPhysicalJoint(final String name, JointType type, JointGroup group)
    {
        return new OdeJoint(type, name, phworld, group);
    }
    
    public OdePhysicalSegment createPhysicalSegment(final String segmentId, final String segmentSID)
    {
        return new OdePhysicalSegment(segmentId, segmentSID, phworld, space);
    }
    
    public OdeHumanoid createPhysicalHumanoid(final String name)
    {
        OdeHumanoid pHuman = null;
        synchronized (PhysicsSync.getSync())
        {
            pHuman = new OdeHumanoid(name, phworld, space);
            pHuman.setGroundGeom(groundGeom);
            pHuman.setEnabled(false);
        }
        return pHuman;
    }

    public MixedSystem createMixedSystem(float[] g, OdeHumanoid pHuman)
    {
        MixedSystem mSystem = new MixedSystem(g, pHuman);
        synchronized (PhysicsSync.getSync())
        {
            mSystem.setup();
        }
        return mSystem;
    }

    public MixedSystem createMixedSystem(float[] g, OdeHumanoid pHuman, VJoint h, String msResources, String msFile) throws IOException
    {
        MixedSystem mSystem = new MixedSystem(g, pHuman);
        MixedSystemAssembler msa = new MixedSystemAssembler(h, pHuman, mSystem);
        Resources resources = new Resources(msResources);
        log.debug(msFile);
        synchronized (PhysicsSync.getSync())
        {
            msa.readXML(resources.getReader(msFile));
            msa.setup();
        }
        return mSystem;
    }

    /**
     * these bodies are copied on the physicsclock loop, i.e. if you attach them to a
     * vjoint, they will steer the vjoint with their physical location / movement
     */
    public OdeRigidBody createRigidBody(String name)
    {
        OdeRigidBody orb = new OdeRigidBody(name, phworld, space);
        addRigidBody(orb);
        return orb;
    }

    /** protected, because the rigidbody must be created by this class! (because it needs access to the phworld and space attribs) */
    protected void addRigidBody(OdeRigidBody orb)
    {
        synchronized (PhysicsSync.getSync())
        {
            rigidBodies.add(orb);
        }
    }

    public void removeRigidBody(OdeRigidBody orb)
    {
        synchronized (PhysicsSync.getSync())
        {
            rigidBodies.remove(orb);
        }
    }

    /**
     * Called for every tick of the physics simulation, runs collisions and steps the physical
     * world. By default, called by physicsTime, which is called from the physicsClock and calls
     * physicsTick until the simulation is again current with the clock.
     * This method is typically called with a fixed delta, e.g. timeStep (this class) or getH() (mixedanimationplayer/animationplayermanager)
     */
    public void physicsTick(float delta)
    {
        if (!runphysics) return;
        if (collisionEnabled)
        {
            collision.collide(space);
            collision.applyContacts();
        }
        phworld.step(delta);
    }

    /**
     * Implementation of PhysicsUpdater interface: Call "physicsTick" as many times as needed to bring the
     * physics simulation up to currentTime. Note that if you use this
     * environment in combination with mixedanimationenvironment, the mixedanimationenvironment
     * will take the physicsupdate task, including calling physicsTick.
     */
    @Override
    public void physicsUpdate(double currentTime)
    {

        synchronized (PhysicsSync.getSync())
        {
            double time = currentTime - prevTime;
            if (time < 0) return;
            while (time > timeStep)
            {
                physicsTick(timeStep);
                time -= timeStep;
                prevTime += timeStep;
            }
        }
    }

    public void physicsCopy()
    {
        synchronized (PhysicsSync.getSync())
        {
            synchronized (AnimationSync.getSync())
            {
                for (CopyEmbodiment ce : copyEmbodiments)
                {
                    ce.copy();
                }
                for (OdeRigidBody orb : rigidBodies)
                {
                    orb.copy();
                }
            }
        }
    }

    protected void runPhysicsRunners()
    {
        synchronized (PhysicsSync.getSync())
        {
            synchronized (physicsRunners) // needs to be synchronized, see:
                                          // https://java.sun.com/docs/books/tutorial/collections/implementations/wrapper.html
            {
                for (Runnable r : physicsRunners)
                {
                    if (r instanceof PoisonPillRunnable)
                    {
                        // XXX ugly hack to ensure that shutdown is not started during rendering. Still needed now we have split off all
                        // environments
                        synchronized (RenderSync.getSync())
                        {
                            synchronized (PhysicsSync.getSync())
                            {
                                r.run();
                            }
                        }
                    }
                    else
                    {
                        synchronized (PhysicsSync.getSync())
                        {
                            r.run();
                        }
                    }
                }
                physicsRunners.clear();
            }
        }
    }

    /**
     * Callback from the physicsclock thread.
     * 
     * First physicsRun(currentTime) is called, running the actual
     * physical simulation. By default, this calls physicsTick until the physics simulation is
     * current with the clock again;
     * 
     * Then copies the result of the simulation onto the animation VJoint structures, using the
     * physicsCopy() function. Note that this method is done quite differently in the
     * ElcerklycEnvironment where some of this task is delegated to the animationplayermanager.
     * 
     * Both physicsRun and physicsCopy have the appropriate synchronizations set.
     * 
     * Finally all physics runners are executed.
     * 
     * It is strongly encouraged to not override this method but to override physicsRun and
     * physicsCopy instead, since they have the proper synchronization mechanisms in place already.
     */
    protected synchronized void physicsTime(double currentTime)
    {
        physicsUpdater.physicsUpdate(currentTime);
        for (ClockListener ppcl : prePhysicsCopyListeners)
        {
            ppcl.time(currentTime);
        }
        physicsCopy();
        runPhysicsRunners();

    }

    public void setPhysicsUpdater(PhysicsUpdater pu)
    {
        physicsUpdater = pu;
    }

    public void addPrePhysicsCopyListener(ClockListener ppcl)
    {
        synchronized (PhysicsSync.getSync())
        {
            prePhysicsCopyListeners.add(ppcl);
        }
    }

    /*
     * ===================
     * =================== SOME USEFUL HELPER METHODS
     * ===================
     */

    /*
     * ===================
     * =================== MAKING PHYSICAL OBJECTS
     * ===================
     */

    /** Added as a hack to enforce high precision timing */
    static class Sleeper extends Thread
    {
        public Sleeper()
        {
            super("SleeperThread");
            this.setDaemon(true);
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(Integer.MAX_VALUE);
            }
            catch (InterruptedException e)
            {
                /* Allow thread to exit */
            }
        }
    }

    /**
     * Connects the ankle segments to the world with a fixed, unmovable joint, to prevent the feet
     * from slipping
     * 
     * @param pHuman
     *            human to tighten in place like this
     */
    public void glueFeetToFloor(PhysicalHumanoid pHuman, JointGroup feetGlueJointGroup)
    {
        synchronized (PhysicsSync.getSync())
        {
            float pos[] = new float[3];
            PhysicalSegment lAnkle = pHuman.getSegment(Hanim.l_ankle);
            PhysicalSegment rAnkle = pHuman.getSegment(Hanim.r_ankle);

            OdeJoint jl = new OdeJoint(JointType.FIXED, "l_ankleScrew", phworld, feetGlueJointGroup);
            lAnkle.box.getTranslation(pos);
            jl.attach(lAnkle.box, null);
            jl.setAnchor(pos[0], pos[1], pos[2]);

            OdeJoint jr = new OdeJoint(JointType.FIXED, "r_ankleScrew", phworld, feetGlueJointGroup);
            rAnkle.box.getTranslation(pos);
            jr.attach(rAnkle.box, null);
            jr.setAnchor(pos[0], pos[1], pos[2]);
        }
    }

    public void clearFeetGlueJointGroup(JointGroup feetGlueJointGroup)
    {
        feetGlueJointGroup.empty();
        feetGlueJointGroup.delete();
    }

    public void clearPhysicalHumanoid(PhysicalHumanoid ph)
    {
        synchronized (PhysicsSync.getSync())
        {
            ph.clear();
        }
    }

    public void initPhysicalHumanoid(OdeHumanoid pHuman)
    {
        synchronized (PhysicsSync.getSync())
        {
            pHuman.updateCOM(0);
            pHuman.setCollision(collision);
            pHuman.setEnabled(false);
        }
    }

    /**
     * @param ce
     *            the collisionEnabled to set
     */
    public void setCollisionEnabled(boolean ce)
    {
        collisionEnabled = ce;
    }

    /**
     * @param rp
     *            the runPhysics to set
     */
    public void setPhysicsEnabled(boolean rp)
    {
        runphysics = rp;
    }

}
