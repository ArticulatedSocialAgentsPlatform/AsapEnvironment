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

import hmi.animation.VJoint;
import hmi.environmentbase.CopyEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.environmentbase.Environment;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.model.HeadManager;
import hmi.faceanimation.model.LowerJaw;
import hmi.facegraphics.GLHead;
import hmi.facegraphics.HMIFaceController;
import hmi.graphics.opengl.GLRenderContext;
import hmi.graphics.opengl.GLRenderList;
import hmi.graphics.opengl.GLRenderObject;
import hmi.graphics.opengl.GLRendererV0;
import hmi.graphics.opengl.GLShader;
import hmi.graphics.opengl.GLShaderProgramLoader;
import hmi.graphics.opengl.GLShape;
import hmi.graphics.opengl.GLSkinnedMesh;
import hmi.graphics.opengl.GLTexture;
import hmi.graphics.opengl.GLTextureLoader;
import hmi.graphics.opengl.GLTextures;
import hmi.graphics.opengl.geometry.BoxGeometry;
import hmi.graphics.opengl.geometry.CapsuleGeometry;
import hmi.graphics.opengl.geometry.DiscGeometry;
import hmi.graphics.opengl.geometry.LineGeometry;
import hmi.graphics.opengl.geometry.SphereGeometry;
import hmi.graphics.opengl.geometry.TexturedRectangleGeometry;
import hmi.graphics.opengl.renderobjects.GLCheckerBoardGround;
import hmi.graphics.opengl.renderobjects.GLNavigation2;
import hmi.graphics.opengl.renderobjects.LightBox;
import hmi.graphics.opengl.scenegraph.GLScene;
import hmi.graphics.opengl.scenegraph.VGLNode;
import hmi.graphics.opengl.state.GLFill;
import hmi.graphics.opengl.state.GLLine;
import hmi.graphics.opengl.state.GLMaterial;
import hmi.graphics.opengl.state.GLPoint;
import hmi.graphics.util.BufferUtil;
import hmi.graphics.util.HumanoidLoader;
import hmi.graphics.util.SceneIO;
import hmi.math.Mat4f;
import hmi.util.AnimationSync;
import hmi.util.ClockListener;
import hmi.util.RenderSync;
import hmi.util.Resources;
import hmi.util.SystemClock;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.opengl.GL2;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.GuardedBy;

/**
 * 
 * @author Dennis Reidsma
 */
@Slf4j
public class HmiRenderEnvironment implements GLRenderObject, Environment, CopyEnvironment
{
    @Getter
    @Setter
    private String id = "hmirenderenvironment";

    // ========= RENDERING SETTINGS ==========
    // ========= RENDERING SETTINGS ==========
    /** if false, do not actually render */
    public volatile boolean render = true;
    /** See main constructor */
    public boolean useVsync = true;
    /** See main constructor */
    public int numStencilBits = 0;
    /** See main constructor */
    public boolean enableFSAA = true;
    /** See main constructor */

    public int FSAA_samples = 4;
    /** See main constructor */
    public boolean weakGraphicsCard = false;

    /** See main constructor */
    public double fovy = 40.0;
    /** See main constructor */
    public double fovyNear = 0.1;
    /** See main constructor */
    public double fovyFar = 100;

    /** frequency of the default render clock made upon initialisation of this object */
    public long defaultClockFrequency = 50;

    // ========= RENDERING ==========

    /** The 3D enabled Canvas */
    protected GLCanvas glCanvas;

    private float bgRed = 0.0f; // The Background color components for OpenGL.
    private float bgGreen = 0.0f;
    private float bgBlue = 0.0f;
    private float bgAlpha = 1.0f; // The Background color is opaque.

    /** the navigation control */
    protected GLNavigation2 glNavControl;
    /** The position of the camera, as determined by the glNavControl. */
    protected VJoint vjCamera;

    private static final int NUM_LIGHTS = 3;
    private VJoint vjLight[] = new VJoint[NUM_LIGHTS];

    /** Object that facilitates easy control of basic OpenGL lights */
    protected LightBox lights;

    /** The module that renders the 3D scene. Here, we use JOGL. In the future this may become configurable. */
    protected GLRendererV0 renderer;

    /** The Clock that triggers rendering. */
    public SystemClock renderClock;

    /** List of Runners that should be executed on the render thread. */
    protected List<Runnable> renderRunners = Collections.synchronizedList(new ArrayList<Runnable>());

    /** Set to true when shutdown process has been requested */
    protected volatile boolean shutdownPrepared = false;
    protected volatile boolean shutdownComplete = false;

    /**
     * all embodiments that must be "copied" on the renderloop, at the time when also morph,
     * deform and calculate matrices are done. Various loaders are responsible for
     * registering the copyembodiments as such...
     */
    @GuardedBy("itself")
    protected Set<CopyEmbodiment> copyEmbodiments = new HashSet<CopyEmbodiment>();

    private boolean allowNavigation = true;

    /*
     * ===================
     * =================== CONSTRUCTION AND INITIALISATION
     * ===================
     */

    /**
     * @param useVsync
     *            Whether or not to synchronize the render framerate to the refresh rate of the display.
     *            May save CPU when display refresh rate is lower than the maximum
     *            achievable rendering frame rate. May drop many frames if the rendering frame rate
     *            drops slightly below the display refresh rate. See <a
     *            href="http://en.wikipedia.org/wiki/Screen_tearing" target=_blank>link</a>
     *            and <a href="http://hardforum.com/showthread.php?t=928593" target=_blank>link</a>
     *            for more information.
     * @param numStencilBits
     * @param enableFSAA
     * @param FSAA_samples
     * @param fovy
     *            Field of view in y-direction (degrees)
     * @param fovyNear
     *            (Positive) distance to the near clip-off plane in meters
     * @param fovyFar
     *            (Positive) distance to the far clip-off plane in meters
     * @param weakGraphicsCard
     *            If true, system is to assume that graphics card is lacking some functions --
     *            and it will try a few hacks to make the system more robust. Especially useful on
     *            the on-board Intel 975 and similar cards
     * 
     */
    public HmiRenderEnvironment(boolean useVsync, int numStencilBits, boolean enableFSAA, int FSAA_samples, double fovy, double fovyNear,
            double fovyFar, boolean weakGraphicsCard)
    {
        this.useVsync = useVsync;
        this.numStencilBits = numStencilBits;
        this.enableFSAA = enableFSAA;
        this.FSAA_samples = FSAA_samples;
        this.fovy = fovy;
        this.fovyNear = fovyNear;
        this.fovyFar = fovyFar;
        this.weakGraphicsCard = weakGraphicsCard;
    }

    public HmiRenderEnvironment()
    {
        this(false, 1, true, 4, 40.0, 0.1, 100, false);
    }

    /**
     * The init method has been moved out of the constructor, because we sometimes want to override
     * it with additional initialisation BEFORE calling the superclass init() method.
     * The init() method is called by the object that constructs this HmiRenderEnvironment.
     */
    public void init()
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    initCanvas();
                }
            });
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }

        initRender();
        initNavigation();
        initLights();
    }

    /** Called by init() to initialize the GLCanvas with the correct capabilities */
    protected void initCanvas()
    {

        GLProfile glp = GLProfile.getDefault();

        GLCapabilities caps = new GLCapabilities(glp);
        if (FSAA_samples < 2)
        {
            caps.setSampleBuffers(false); // disable full screen antialiasing
        }
        else
        {
            caps.setSampleBuffers(true); // enable full screen antialiasing (FSAA)
            caps.setNumSamples(FSAA_samples); // number of samples for FSAA
            caps.setSampleBuffers(true);
        }

        if (numStencilBits > 0) caps.setStencilBits(numStencilBits);
        glCanvas = new GLCanvas(caps);

    }

    /** Initialize the renderer and the renderClock. The renderClock may be replaced later. */
    protected void initRender()
    {
        renderer = new GLRendererV0(glCanvas);
        renderer.setFOVY(fovy); // field of view in y-direction (degrees)
        renderer.setNear(fovyNear);
        renderer.setFar(fovyFar);

        renderClock = new SystemClock(1000 / defaultClockFrequency, "DefaultRenderEnvironmentClock");
        renderClock.addClockListener(new MyRenderClockCallback()); // this allows us to easily do additional things on the renderclock
        renderClock.addClockListener(renderer); // every renderclocktick, render the scene
        renderer.setScene(this); // this means that the renderer will delegate the glInit and glRender calls to this HmiRenderEnvironment object

        // enable or disable VSync (also depends on driver settings, so might not have any effect)
        // VSync disabled means lower quality for scrolling, and is only useful for framerate
        // measurements.
        renderer.setVsync(useVsync);
    }

    protected void initNavigation()
    {

        glNavControl = new GLNavigation2(glCanvas);
        glNavControl.setPosition(0.0f, 1.2f, 4.0f); // The (initial) position of the camera.
        glNavControl.time(0);

        vjCamera = new VJoint("Camera");
        vjCamera.setTranslation(0.0f, 1.2f, 4.0f);
    }

    /** Initialize the GL lights */
    protected void initLights()
    {
        lights = new LightBox(NUM_LIGHTS);
        lights.get(0).setDiffuseColor(0.8f, 0.8f, 0.8f);
        lights.get(1).setDiffuseColor(0.6f, 0.6f, 0.6f);
        lights.get(2).setDiffuseColor(0.6f, 0.6f, 0.6f);

        lights.get(0).setPosition(2.6777347f, 2.5676637f, 1.46893778f); // The (initial) position of the light
        lights.get(1).setPosition(-0.040819082f, 2.066795f, 2.1057882f); // The (initial) position of the light
        lights.get(2).setPosition(-1.7811359f, 2.6335182f, 1.9070864f); // The (initial) position of the light

        // lights.get(0).setLinearAttenuation(1.0f);
        for (int i = 0; i < NUM_LIGHTS; i++)
        {
            lights.get(i).setVisible(true);
            vjLight[i] = new VJoint("Light" + i);
            vjLight[i].setTranslation(lights.get(i).getPosition());
        }
        lights.setControl(glNavControl);
    }

    /** Do not call before the JOGL has been initialised, i.e. the Window in which the canvas is displayed has been made visible. */
    public void startRenderClock()
    {
        glCanvas.requestFocusInWindow(); // requests the focus for keyboard input
        renderClock.start();
    }

    /**
     * Set the position of the (keyboard) navigator (e.g. the camera position).
     */
    public void setViewPoint(final float[] fs)
    {
        if (fs.length != 3)
        {
            log.error("Viewpoint coordinates must be a float array of length 3");
        }
        else
        {
            if (allowNavigation)
            {
                addRenderRunner(new Runnable()
                {
                    public void run()
                    {
                        glNavControl.setPosition(fs[0], fs[1], fs[2]);
                    }
                });
            }
            else
            {
                vjCamera.setTranslation(fs[0], fs[1], fs[2]);
            }
        }
    }

    /**
     * Set the rotation of the (keyboard) navigator (e.g. the camera rotation). Roll pitch and yaw are specified in degrees.
     */
    public void setViewRollPitchYawDegrees(final float roll, final float pitch, final float yaw)
    {
        if (allowNavigation)
        {
            addRenderRunner(new Runnable()
            {
                public void run()
                {
                    glNavControl.setRollPitchYaw(roll, pitch, yaw);
                }
            });
        }
        else
        {
            vjCamera.setRollPitchYawDegrees(roll, pitch, yaw);
        }
    }

    public void setLightPosition(int light, float x, float y, float z)
    {
        glNavControl.setLightPosition(light, x, y, z);
    }

    public void setNear(double near)
    {
        renderer.setNear(near);
    }

    public void setFar(double far)
    {
        renderer.setFar(far);
    }

    public void setFOVY(double fovy)
    {
        renderer.setFOVY(fovy);
    }

    public void setLinearVelocity(float f)
    {
        glNavControl.setLinearVelocity(f);
    }

    public void setVerticalVelocity(float f)
    {
        glNavControl.setVerticalVelocity(f);
    }

    public void setAngularVelocityY(float f)
    {
        glNavControl.setAngularVelocityY(f);
    }

    public void setStrafeVelocity(float f)
    {
        glNavControl.setStrafeVelocity(f);
    }

    /*
     * ===================
     * =================== GET THE AWT COMPONENT
     * ===================
     */

    /**
     * Get the heavyweight AWT Component on which the rendering happens.
     * This component is then typically put in a Window as part of a larger UI. canvas does not exist until
     * init was called
     */
    public java.awt.Component getAWTComponent()
    {
        if (glCanvas == null) log.error("Trying to access glCanvas before HmiRenderEnvironment was properly initialised");
        return glCanvas;
    }

    /*
     * ===================
     * =================== CHANGE RENDER SETTINGS
     * ===================
     */

    // nothing here yet

    /*
     * ===================
     * =================== RENDER CLOCK AND RENDER RUNNERS
     * ===================
     */
    public SystemClock getRenderClock()
    {
        return renderClock;
    }

    /** This object is responsible for doing extra things on the render tick, such as navigation and executing RenderRunners. */
    class MyRenderClockCallback implements ClockListener
    {
        @Override
        public void initTime(double time)
        {
        }

        @Override
        public void time(double currentTime)
        {
            renderTime(currentTime);
        }
    }

    /** Should be called in the renderClock callback. Runs any RenderRunners, and makes the navigation. */
    protected void renderTime(double currentTime)
    {
        navigate(currentTime);
        synchronized (renderRunners)// needs to be synchronized, see:
                                    // https://java.sun.com/docs/books/tutorial/collections/implementations/wrapper.html
        {
            for (Runnable r : renderRunners)
            {
                r.run();
            }

            // in the sync block, makes sure we don't add before clearing this list
            renderRunners.clear();
        }
    }

    /**
     * Add a runner to the render thread. The runners is executed as a last step in renderTime.
     * Here the proper synchronization is in place to do 'stuff' with render joints or the render
     * clock (e.g. reset it). The Runnable itself may not call addRenderRunner or in any other way
     * tinker with renderRunners or physicsRunners.
     */
    public void addRenderRunner(Runnable r)
    {
        renderRunners.add(r);
    }

    /** Resume play if render clock was paused; otherwise, no effect. Must be called on the render thread using requestPlay(). */
    protected void play()
    {
        renderClock.start();
    }

    /** Request the render clock to resume play, if it was paused */
    public void requestPlay()
    {
        addRenderRunner(new Runnable()
        {
            public void run()
            {
                play();
            }
        });
    }

    /** Pause render clock. Must be called on the render thread using requestPause(). */
    protected void pause()
    {
        renderClock.pause();
    }

    /** Request the render clock to pause */
    public void requestPause()
    {
        addRenderRunner(new Runnable()
        {
            public void run()
            {
                pause();
            }
        });
    }

    /** Reset has no effect for the render environment. */
    protected void reset()
    {
        renderClock.setMediaSeconds(0);// Herwin: probably not needed, but doesn't hurt
    }

    /** Request a reset (no effect!) */
    public void requestReset()
    {
        addRenderRunner(new Runnable()
        {
            public void run()
            {
                reset();
            }
        });
    }

    /** Terminate render clock. Must be called on the render thread using requestShutdown(). After this, the clock can no longer be started. */
    protected void shutdown()
    {
        renderClock.terminate();
    }

    /** Request the render clock to terminate. After this, the clock can no longer be started. */
    @Override
    public void requestShutdown()
    {
        addRenderRunner(new Runnable()
        {
            public void run()
            {
                shutdown();
                shutdownComplete = true;
            }
        });
        shutdownPrepared = true;
    }

    @Override
    public boolean isShutdown()
    {
        return shutdownComplete;
    }

    /*
     * ===================
     * =================== THE SCENE
     * ===================
     */

    /**
     * The shapes, meshes, materials, etc that are to be rendered are contained in this list of
     * VGLNodes. They will be rendered in the order in which they are stored in this list. Actually,
     * one might create *one* VGLNode (renderScene...) and add the others as children to it.
     * However, we do it like this because on some machines, we need to restore all rendering
     * properties between rendering two different objects, to avoid java opengl crashes... New
     * visualisation objects are added using the method addVisualisation()
     * Note: access to this variable must be synchronized on itself.
     */
    @GuardedBy("visualisations")
    protected HashSet<VGLNode> visualisations = new HashSet<VGLNode>();

    /**
     * Visualisations that have been (GL)initialized already, but are currently hidden (so moved
     * from visualisations to this list)
     **/
    protected HashSet<VGLNode> hiddenVisualisations = new HashSet<VGLNode>();

    /**
     * When a new visulaisation object is added through addVisualisation, we need to be sure that
     * glInit is called on it before calling glRender on it. To this end, they are stored first in
     * this array of uninitialized vglNodes.
     * Note: access to this variable must be synchronized on itself.
     */
    @GuardedBy("visualisations")
    protected HashSet<VGLNode> visualisationsUninitialized = new HashSet<VGLNode>();

    /**
     * GLScenes are (currently) the access point for doing the morph() and deform() operations.
     * Whenever a VGLNode is loaded that requires these operations, the corresponding GLScene
     * must also be added.
     * Note: access to this variable must be synchronized on itself.
     */
    @GuardedBy("glScenes")
    protected Map<String, GLScene> glScenes = new HashMap<String, GLScene>();

    /**
     * Add a VGLNode to the list of visualisations. This method will add the VGLNode first to a list
     * of nodes waiting to be initialized.
     */
    protected void addVisualisation(VGLNode viz)
    {
        synchronized (visualisations)
        {
            if (hasVisualisation(viz))
            {
                log.warn("Trying to add visualsation twice");
                return;
            }
            visualisationsUninitialized.add(viz);
        }
    }

    /**
     * Add a GLScene to the list of glscenes on which deform and morph will be called.
     */
    protected void addHumanoidGLScene(String id, GLScene gls)
    {
        synchronized (glScenes)
        {
            glScenes.put(id, gls);
        }
    }

    protected void removeHumanoidGLScene(String id)
    {
        synchronized (glScenes)
        {
            glScenes.remove(id);
        }
    }

    /**
     * Checks if a visualization is already in the scene (returns true on hidden VGLNodes too)
     */
    protected boolean hasVisualisation(VGLNode viz)
    {
        synchronized (visualisations)
        {
            if (visualisationsUninitialized.contains(viz)) return true;
            if (visualisations.contains(viz)) return true;
            if (this.hiddenVisualisations.contains(viz)) return true;
        }
        return false;
    }

    protected boolean isVisualisationVisible(VGLNode viz)
    {
        synchronized (visualisations)
        {
            return visualisations.contains(viz);
        }
    }

    /**
     * Show/hide a VGLNode
     */
    protected void setVisualisationVisible(VGLNode viz, boolean enabled)
    {
        synchronized (visualisations)
        {
            if (!enabled)
            {
                if (visualisations.contains(viz))
                {
                    hiddenVisualisations.add(viz);
                    visualisations.remove(viz);
                }
                else if (visualisationsUninitialized.contains(viz))
                {
                    hiddenVisualisations.add(viz);
                }
                else if (!hiddenVisualisations.contains(viz))
                {
                    log.error("Attempting to hide non-existing VGLNode " + viz.getRoot().getId());
                }
            }
            else
            {
                if (hiddenVisualisations.contains(viz))
                {
                    hiddenVisualisations.remove(viz);
                    visualisations.add(viz);
                }
                else if (!visualisationsUninitialized.contains(viz) && !visualisations.contains(viz))
                {
                    log.error("Attempting to show non-existing VGLNode " + viz.getRoot().getId());
                }
            }
        }
    }

    protected void removeVisualisation(VGLNode viz)
    {
        synchronized (visualisations)
        {
            visualisationsUninitialized.remove(viz);
            visualisations.remove(viz);
            hiddenVisualisations.remove(viz);
        }
    }

    /** these embodiments are copied on the renderclock loop */
    public void addCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.add(ce);
        }
    }

    /** these embodiments are copied on the renderclock loop */
    public void addCopyEmbodiments(List<CopyEmbodiment> ces)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.addAll(ces);
        }
    }

    /** these embodiments are copied on the renderclock loop */
    public void removeCopyEmbodiments(List<CopyEmbodiment> ces)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.removeAll(ces);
        }
    }

    /** these embodiments are copied on the renderclock loop */
    public void removeCopyEmbodiment(CopyEmbodiment ce)
    {
        synchronized (copyEmbodiments)
        {
            copyEmbodiments.remove(ce);
        }
    }

    /*
     * ===================
     * =================== THE RENDERING PIPELINE
     * ===================
     */

    /** Do some basic initialization prior to initializing the scene: smoothing, cull_face, etc. Called from glInit(). */
    protected void glPreInit(GLRenderContext glc)
    {
        //
        // gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        // gl.glDisable(GL2.GL_MULTISAMPLE);

        GL2 gl = glc.gl2;
        gl.glEnable(GL2.GL_NORMALIZE);

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glCullFace(GL2.GL_BACK);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glShadeModel(GL2.GL_SMOOTH);
        // gl.glEnable(GL.GL_CULL_FACE);
        // gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHTING);
        /*
         * gl.glPolygonMode( GL.GL_FRONT, GL.GL_LINE ); gl.glPolygonMode( GL.GL_BACK, GL.GL_LINE
         * ); gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_LINE );
         */
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
        // gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        float[] global_amb = new float[] { 0.0f, 0.0f, 0.0f, 1.0f }; // default is non-zero!!!

        FloatBuffer global_ambient = BufferUtil.directFloatBuffer(4);

        global_ambient.put(global_amb);
        global_ambient.rewind();
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, global_ambient);
        //
        // gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        // gl.glDisable(GL2.GL_MULTISAMPLE);
        // gl.gl.glDisable(GL.GL_BLEND);
        // gl.gl.glDisable(GL.GL_STENCIL_TEST);

        gl.glEnable(GL2.GL_NORMALIZE);
    }

    /**
     * Initialise the scene for the first time. Partially delegated to glPreInit.
     */
    @Override
    public void glInit(GLRenderContext glc)
    {
        glPreInit(glc);
        lights.glInit(glc);
        synchronized (visualisations)
        {
            for (VGLNode nextVgl : visualisationsUninitialized)
            {
                nextVgl.glInit(glc);
                synchronized (visualisations)
                {
                    if (!hiddenVisualisations.contains(nextVgl))
                    {
                        visualisations.add(nextVgl);
                    }
                }
            }
            visualisationsUninitialized.clear();
        }

        glNavControl.glInit(glc);
        hmi.graphics.opengl.GLUtil.reportGLErrors(glc);
    }

    /**
     * Do some basic rendering calls prior to rendering the scene: It clears the necessary OpenGL
     * buffers (color, depth, ..) and calls glInit on any uninitialized visualisations
     */
    protected void glPreRender(GLRenderContext glc)
    {
        GL2 gl = glc.gl2;
        gl.glDisable(GL2.GL_BLEND);
        gl.glDepthMask(true);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT); // clear the (OpenGL) Background
        gl.glClearColor(bgRed, bgGreen, bgBlue, bgAlpha);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        synchronized (visualisations)
        {
            for (VGLNode nextVgl : visualisationsUninitialized)
            {
                nextVgl.glInit(glc);
                if (!hiddenVisualisations.contains(nextVgl))
                {
                    visualisations.add(nextVgl);
                }
            }
            visualisationsUninitialized.clear();
        }
    }

    protected void glPostRender(GLRenderContext glc)
    {
        // nothing to do here... but subclasses might find this useful
    }

    /**
     * This method takes care of processing all animation results for rendering
     * -- i.e. calculate the joint matrices, prepare the morph targets and the skin deformations.
     * Uses the AnimationSync to make sure this step is not interfered with by animation processes.
     */
    protected void processAnimationResultForRender(GLRenderContext glc)
    {
        synchronized (AnimationSync.getSync())
        {
            for (VJoint vj : objectAnimationJoints.values())
            {
                vj.calculateMatrices(); // for humanoids, this is done through copyembodiments
            }
            /* copyEmbodiments require some form of copying of data upon render, such as collating MPEG4 data to send it to the face animation */
            for (CopyEmbodiment ce : copyEmbodiments)
            {
                ce.copy();
            }
            synchronized (glScenes)
            {
                for (GLScene gls : glScenes.values())
                {
                    gls.doMorph();
                    gls.deform();
                }
            }
        }
    }

    /**
     * Disables or enables the keyboard navigation (default is enabled).
     */
    public void setNavigationEnabled(boolean b)
    {
        allowNavigation = b;
        glNavControl.setEnabled(b);
    }

    /**
     * do the main rendering step. Consists of calling prerender, then
     * processAnimationResultForRender, then rendering all visualisations
     */
    public void glRender(GLRenderContext glc)
    {
        if (!render) return;
        synchronized (RenderSync.getSync()) // this sync is here because we do not want to dispose the frame
                                            // in the middle of an openGL render action
        {
            if (shutdownPrepared) return; // OK, apparently the system has already shut down earlier.
                                          // Don't try to render anymore -- there is no canvas left :)

            GL2 gl = glc.gl2;
            glPreRender(glc);

            if (allowNavigation)
            {
                glNavControl.glRender(glc);
            }
            else
            {
                vjCamera.calculateMatrices();
                float m[] = Mat4f.getMat4f();
                Mat4f.set(m, vjCamera.getGlobalMatrix());
                Mat4f.invertRigid(m);
                glc.gl2.glMultTransposeMatrixf(m, 0);
            }

            lights.glRender(glc);

            // make sure that the effects of all animation are properly performed on the meshes and
            // stuff
            processAnimationResultForRender(glc);

            // do the actual rendering for all visaulisations
            synchronized (visualisations)
            {
                for (VGLNode nextVgl : visualisations)
                {
                    if (weakGraphicsCard)
                    { // HACK to avoid crashes on laptop with weak graphics card
                        gl.glPushAttrib(GL2.GL_ALL_ATTRIB_BITS);
                        gl.glPushClientAttrib((int) GL2.GL_ALL_CLIENT_ATTRIB_BITS);
                    }
                    if (hiddenVisualisations.contains(nextVgl))
                    {
                        log.error("HELP!");// TODO: this is an assertionerror
                        // System.out.println(nextVgl);
                        // System.exit(0);
                    }
                    nextVgl.glRender(glc);

                    // TODO:
                    // logger.warn("DISABLE BLEND, ENABLE DEPTH MASK: SHOULD NOT BE DONE HERE:(");
                    gl.glDisable(GL2.GL_BLEND); // this seems to be here because some part of
                                                // Armandia requires these settings, but some other parts turn it off?
                    gl.glDepthMask(true);

                    if (weakGraphicsCard)
                    { // HACK to avoid crashes on laptop with weak graphics card
                        gl.glPopClientAttrib();
                        gl.glPopAttrib();
                    }
                }
            }
            glPostRender(glc);

            hmi.graphics.opengl.GLUtil.reportGLErrors(glc);
        }
    }

    /*
     * ===================
     * =================== CONTROLLING VIEWPOINTS AND NAVIGATION
     * ===================
     */

    /** Do the navigation for this time step */
    protected void navigate(double currentTime)
    {
        /* every render clock tick, do the navigation actions */
        /**/
        synchronized (AnimationSync.getSync()) // the render must be synchronized with any changes
                                               // in the animation -- to make sure that the renderer
                                               // always has a single consistent animation scene to
                                               // determine matrices
        {
            // do the vjoint control thingies: navigation in the world. This can be done on the
            // renderclock, which is slower than the mixedanimationplayerclock

            if (allowNavigation)
            {
                glNavControl.time(currentTime);
                // mouseControl.time(currentTime);
                // here you would probably also do, e.g., mouse-based movement of objects, i.e. 3D
                // editor-like capabilities
                vjCamera.setTranslation(glNavControl.getPosition());
            }

            for (int i = 0; i < NUM_LIGHTS; i++)
            {
                vjLight[i].setTranslation(lights.get(i).getPosition());
            }
        }
        /**/
    }

    /** VJoint that always is updated to reflect the current camera position as determined by the navigation controls */
    public VJoint getCameraTarget()
    {
        return vjCamera;
    }

    /** VJoint that always is updated to reflect the current light position as determined by the navigation controls */
    public VJoint getLightTarget(int i)
    {
        if (i < NUM_LIGHTS)
        {
            return vjLight[i];
        }
        return null;
    }

    /*
     * ===================
     * =================== CONTROLLING LIGHTS
     * ===================
     */

    /*
     * ===================
     * =================== CONSTRUCTING BASIC OBJECTS: BOXES, SPHERES, ETC
     * ===================
     * ===================
     * =================== (UN)LOADING VHs AND OBJECTS FROM COLLADA
     * ===================
     */

    protected List<String> objectIds = new ArrayList<String>();
    protected Map<String, VJoint> objectAnimationJoints = new HashMap<String, VJoint>();
    protected Map<String, VGLNode> objectVGLNodes = new HashMap<String, VGLNode>();

    public enum RenderStyle
    {
        LINE, VERTEX, FILL
    };

    public void setObjectVisible(String id, boolean visible)
    {
        if (!objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to show or hide a rendering object with an ID that does not exist. Id: " + id);
            return;
        }
        VGLNode obj = objectVGLNodes.get(id);
        setVisualisationVisible(obj, visible);
    }

    /**
     * Unload any previously loaded object by Id. Object may have been loaded as
     * collada, or as "basic object" (checkerboardground, box, sphere, ...)
     */
    public void unloadObject(String id)
    {
        if (!objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to unload a rendering object with an ID that does not exist: {}", id);
            return;
        }
        VGLNode model = objectVGLNodes.get(id);
        synchronized (AnimationSync.getSync())
        {
            removeVisualisation(model);
            objectIds.remove(id);
            objectAnimationJoints.remove(id);
            objectVGLNodes.remove(id);
        }
    }

    /** this method provides access to the root VJoint of a previously loaded object -- useful if you want to move the object around :) */
    public VJoint getObjectRootJoint(String id)
    {
        return objectAnimationJoints.get(id);
    }

    /** make parent-child relations between objects... and removes the child from the list of "independent objects" */
    public void setObjectParent(String childId, String parentId)
    {
        VGLNode child = objectVGLNodes.get(childId);
        if (child == null)
        {
            log.error("Adding nonexisting child to parent");
        }
        // child may not have been initialized, so dismiss parent again to unitializedvisualisations
        objectVGLNodes.get(parentId).addChild(child);
        synchronized (visualisations)
        {
            if (hiddenVisualisations.contains(child)) hiddenVisualisations.add(objectVGLNodes.get(parentId));
            visualisations.remove(objectVGLNodes.get(parentId));
            visualisationsUninitialized.add(objectVGLNodes.get(parentId));
        }
        unloadObject(childId);
    }

    /** insert a VJoint above the given object, add it to the administration, and return the joint */
    public VJoint insertParentJointForObject(String id)
    {
        loadEmptyObject("parent_" + id);
        setObjectParent(id, "parent_" + id);
        return getObjectRootJoint("parent_" + id);
    }

    // TODO: misschien wil je ook calls waarmee je basicobject shapes TOEVOEGT aan een bestaand object

    /** load an object from collada file */
    public VJoint loadObject(String id, String texturedirectory, String resourcedirectory, String filename)
    {
        if (objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to load a rendering object with an ID that already exists");
            return objectAnimationJoints.get(id);
        }
        if (texturedirectory != null) GLTextureLoader.addTextureDirectory(texturedirectory);
        GLScene theGLScene = null;
        theGLScene = SceneIO.readGLScene(resourcedirectory, filename);

        VJoint modelRenderJoint = new VJoint(id);
        for (VJoint nextJoint : theGLScene.getVJointRoots())
        {
            modelRenderJoint.addChild(nextJoint);
        }
        VGLNode model = new VGLNode(modelRenderJoint, theGLScene.getGLShapeList());

        GLShape state = new GLShape();
        state.addGLState(new GLFill());
        model.getGLShapeList().prepend(state);

        VJoint modelAnimationJoint = modelRenderJoint.masterCopyTree("");
        synchronized (AnimationSync.getSync())
        {
            addVisualisation(model);
            objectIds.add(id);
            objectAnimationJoints.put(id, modelAnimationJoint);
            objectVGLNodes.put(id, model);
        }
        return modelAnimationJoint;
    }

    /** load an object with no content */
    public void loadEmptyObject(String id)
    {
        if (objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to construct an empty object with an ID that already exists: {}", id);
            return;
        }
        VJoint bsRenderJoint = new VJoint(id);
        GLRenderList shapeList = new GLRenderList(1);
        VGLNode vglBS = new VGLNode(bsRenderJoint, shapeList);
        VJoint bsAnimationJoint = vglBS.getRoot();
        bsAnimationJoint.setId(id);

        synchronized (AnimationSync.getSync())
        {
            addVisualisation(vglBS);
            objectIds.add(id);
            objectAnimationJoints.put(id, bsAnimationJoint);
            objectVGLNodes.put(id, vglBS);
        }

    }

    /** load a basic disc with given attributes. Not public, since we do not want external classes to work with the graphics packagesz */
    private VJoint loadBasicShape(String id, GLRenderObject glro, RenderStyle style, float[] diffuse, float[] specular, float[] ambient,
            float[] emission)
    {
        if (objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to construct a basic object with an ID that already exists: {}", id);
            return objectAnimationJoints.get(id);
        }

        VJoint bsRenderJoint = new VJoint("render joint " + id);
        GLShape bsShape = new GLShape("shape " + id);
        GLRenderList shapeList = new GLRenderList(1);
        GLMaterial colorState = new GLMaterial();
        colorState.setDiffuseColor(diffuse);
        colorState.setSpecularColor(specular);
        colorState.setAmbientColor(ambient);
        colorState.setEmissionColor(emission);
        // colorState.setGLShader(new GLShader("blinnBasic"));
        // colorState.setGLShader(new GLShader("simplePhongShader"));
        colorState.setGLShader(new GLShader("specular"));

        switch (style)
        {
        case FILL:
            bsShape.addGLGeometry(new GLFill());
            break;
        case LINE:
            bsShape.addGLGeometry(new GLLine());
            colorState.setGLShader(new GLShader("diffuseColorShader"));
            break;
        case VERTEX:
            bsShape.addGLGeometry(new GLPoint());
            colorState.setGLShader(new GLShader("diffuseColorShader"));
            break;
        }

        bsShape.addGLGeometry(colorState);
        bsShape.addGLGeometry(glro);
        bsShape.linkToTransformMatrix(bsRenderJoint.getGlobalMatrix());
        shapeList.add(bsShape);

        VGLNode vglBS = new VGLNode(bsRenderJoint, shapeList);
        VJoint bsAnimationJoint = vglBS.getRoot();
        bsAnimationJoint.setId(id);

        synchronized (AnimationSync.getSync())
        {
            addVisualisation(vglBS);
            objectIds.add(id);
            objectAnimationJoints.put(id, bsAnimationJoint);
            objectVGLNodes.put(id, vglBS);
        }
        return bsAnimationJoint;
    }

    /** load a basic disc with given attributes */
    public VJoint loadDisc(String id, float radius1, float radius2, float radius3, int numSlices, int numStacks, RenderStyle style,
            float[] diffuse, float[] specular, float[] ambient, float[] emission)
    {
        DiscGeometry discGeometry = new DiscGeometry(radius1, radius2, radius3, numSlices, numStacks);
        return loadBasicShape(id, discGeometry, style, diffuse, specular, ambient, emission);
    }

    /** load a basic box with given attributes */
    public VJoint loadBox(String id, float[] halfExtends, RenderStyle style, float[] diffuse, float[] specular, float[] ambient,
            float[] emission)
    {
        BoxGeometry boxGeometry = new BoxGeometry(halfExtends);
        return loadBasicShape(id, boxGeometry, style, diffuse, specular, ambient, emission);
    }

    /** Load a standard sphere with given properties */
    public VJoint loadSphere(String id, float radius, int numSlices, int numStacks, RenderStyle style, float[] diffuse, float[] specular,
            float[] ambient, float[] emission)
    {
        SphereGeometry sphereGeometry = new SphereGeometry(radius, numSlices, numStacks);
        return loadBasicShape(id, sphereGeometry, style, diffuse, specular, ambient, emission);
    }

    /** Load a standard capsule with given properties */
    public VJoint loadCapsule(String id, float radius, float height, int numSlices, int numStacks, RenderStyle style, float[] diffuse,
            float[] specular, float[] ambient, float[] emission)
    {
        CapsuleGeometry capsuleGeometry = new CapsuleGeometry(radius, height, numSlices, numStacks);
        return loadBasicShape(id, capsuleGeometry, style, diffuse, specular, ambient, emission);
    }

    /**
     * Load a standard line with given properties.
     * @param vertices the line vertices, 6 floats for a line
     */
    public VJoint loadLine(String id, float[] vertices, float[] diffuse, float[] specular, float[] ambient, float[] emission)
    {
        LineGeometry lineGeometry = new LineGeometry(vertices);
        return loadBasicShape(id, lineGeometry, RenderStyle.LINE, diffuse, specular, ambient, emission);
    }

    /** Load a colored axis cross. */
    public VJoint loadAxisCross(String id, float axisLengthCm)
    {
        if (objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to construct an axis cross object with an ID that already exists: {}", id);
            return objectAnimationJoints.get(id);
        }

        VJoint axisRenderJoint = new VJoint(id);

        // some global attributes such as fill
        GLShape axisAttrs = new GLShape("axis attributes " + id);
        GLRenderList shapeList = new GLRenderList(1);
        GLMaterial shaderMat = new GLMaterial();
        shaderMat.setGLShader(new GLShader("diffuseColorShader"));
        axisAttrs.addGLGeometry(new GLFill());
        axisAttrs.addGLGeometry(shaderMat);
        shapeList.add(axisAttrs);

        // construct actual axis cross in various colors:

        GLShape glsShape = null;
        GLRenderObject geometry = null;
        GLMaterial material = null;

        float[] red = new float[] { 1f, 0f, 0f, 1f };
        float[] green = new float[] { 0f, 1f, 0f, 1f };
        float[] blue = new float[] { 0f, 0f, 1f, 1f };
        float[] white = new float[] { 1f, 1f, 1f, 1f };

        material = new GLMaterial();
        material.setEmissionColor(white);
        material.setDiffuseColor(white);
        material.setSpecularColor(white);
        material.setAmbientColor(white);
        material.setGLShader(new GLShader("diffuseColorShader"));

        geometry = new SphereGeometry(axisLengthCm * 0.001f, 4, 4);
        glsShape = new GLShape("axis shape white " + id);
        glsShape.addGLState(material);
        glsShape.addGLGeometry(geometry);
        glsShape.linkToTransformMatrix(axisRenderJoint.getGlobalMatrix());
        shapeList.add(glsShape);

        material = new GLMaterial();
        material.setEmissionColor(red);
        material.setDiffuseColor(red);
        material.setSpecularColor(red);
        material.setAmbientColor(red);
        material.setGLShader(new GLShader("diffuseColorShader"));

        geometry = new hmi.graphics.opengl.geometry.BoxGeometry(new float[] { axisLengthCm * 0.005f, axisLengthCm * 0.0005f,
                axisLengthCm * 0.0005f }, new float[] { axisLengthCm * 0.005f, 0f, 0f });
        glsShape = new GLShape("axis shape red " + id);
        glsShape.addGLState(material);
        glsShape.addGLGeometry(geometry);
        glsShape.linkToTransformMatrix(axisRenderJoint.getGlobalMatrix());
        shapeList.add(glsShape);

        material = new GLMaterial();
        material.setEmissionColor(green);
        material.setDiffuseColor(green);
        material.setSpecularColor(green);
        material.setAmbientColor(green);
        material.setGLShader(new GLShader("diffuseColorShader"));

        geometry = new BoxGeometry(new float[] { axisLengthCm * 0.0005f, axisLengthCm * 0.005f, axisLengthCm * 0.0005f }, new float[] { 0f,
                axisLengthCm * 0.005f, 0f });
        glsShape = new GLShape("axis shape green " + id);
        glsShape.addGLState(material);
        glsShape.addGLGeometry(geometry);
        glsShape.linkToTransformMatrix(axisRenderJoint.getGlobalMatrix());
        shapeList.add(glsShape);

        material = new GLMaterial();
        material.setEmissionColor(blue);
        material.setDiffuseColor(blue);
        material.setSpecularColor(blue);
        material.setAmbientColor(blue);
        material.setGLShader(new GLShader("diffuseColorShader"));

        geometry = new BoxGeometry(new float[] { axisLengthCm * 0.0005f, axisLengthCm * 0.0005f, axisLengthCm * 0.005f }, new float[] { 0f,
                0f, axisLengthCm * 0.005f });
        glsShape = new GLShape("axis shape blue " + id);
        glsShape.addGLState(material);
        glsShape.addGLGeometry(geometry);
        glsShape.linkToTransformMatrix(axisRenderJoint.getGlobalMatrix());
        shapeList.add(glsShape);

        VGLNode vglAxis = new VGLNode(axisRenderJoint, shapeList);
        VJoint axisAnimationJoint = vglAxis.getRoot();
        axisAnimationJoint.setId(id);

        synchronized (AnimationSync.getSync())
        {
            addVisualisation(vglAxis);
            objectIds.add(id);
            objectAnimationJoints.put(id, axisAnimationJoint);
            objectVGLNodes.put(id, vglAxis);
        }
        return axisAnimationJoint;
    }

    public VJoint loadTexturedRectangle(String id, float width, float height, String texture)
    {
        VJoint joint = new VJoint("render joint " + id);

        GLShape bsShape = new GLShape("shape " + id);
        GLRenderList shapeList = new GLRenderList(1);

        // test: solid color
        bsShape.addGLGeometry(new GLFill());

        GLTexture tex = GLTextures.getGLTexture(texture);
        bsShape.addGLGeometry(tex);
        GLMaterial material = new GLMaterial();
        material.setGLShader(new GLShader("texture_fixed"));
        bsShape.addGLGeometry(material);
        bsShape.addGLGeometry(new TexturedRectangleGeometry(width, height));

        bsShape.linkToTransformMatrix(joint.getGlobalMatrix());
        shapeList.add(bsShape);

        VGLNode vglBS = new VGLNode(joint, shapeList);
        VJoint bsAnimationJoint = vglBS.getRoot();
        bsAnimationJoint.setId(id);

        synchronized (AnimationSync.getSync())
        {
            addVisualisation(vglBS);
            objectIds.add(id);
            objectAnimationJoints.put(id, bsAnimationJoint);
            objectVGLNodes.put(id, vglBS);
        }

        return joint;
    }

    /**
     * Add a checkerboardground to the list of loaded objects.
     * @param id unique id of the checkerboard object
     * @param width width and hight of a single tile in the checkerboard
     * @param height height of the entire checkerboard ground
     **/
    public VJoint loadCheckerBoardGround(String id, float width, float height)
    {
        if (objectAnimationJoints.containsKey(id))
        {
            log.error("Trying to construct a checkerboardground with an ID that already exists");
            return objectAnimationJoints.get(id);
        }

        VJoint groundRenderJoint = new VJoint("Ground render joint");
        GLShape groundShape = new GLShape("Ground shape");

        GLRenderList shapeList = new GLRenderList(1);
        groundShape.addGLGeometry(new GLCheckerBoardGround(width, height));
        groundShape.linkToTransformMatrix(groundRenderJoint.getGlobalMatrix());
        shapeList.add(groundShape);

        VGLNode vglGround = new VGLNode(groundRenderJoint, shapeList);
        VJoint groundAnimationJoint = vglGround.getRoot();// .masterCopyTree("master-");
        groundAnimationJoint.setId(id);

        synchronized (AnimationSync.getSync())
        {
            addVisualisation(vglGround);
            objectIds.add(id);
            objectAnimationJoints.put(id, groundAnimationJoint);
            objectVGLNodes.put(id, vglGround);
        }
        return groundAnimationJoint;
    }

    /** Set background color. This color is used in the preRender method when the canvas is cleared */
    public void setBackground(float r, float g, float b)
    {
        bgRed = r;
        bgGreen = g;
        bgBlue = b;
        // bgAlpha = a; background is always opaque...
    }

    protected List<String> humanoidIds = new ArrayList<String>();
    protected Map<String, VJoint> humanoidAnimationJoints = new HashMap<String, VJoint>();
    protected Map<String, VGLNode> humanoidVGLNodes = new HashMap<String, VGLNode>();
    protected Map<String, GLHead> humanoidHeads = new HashMap<String, GLHead>();
    protected Map<String, HeadManager> humanoidHeadManagers = new HashMap<String, HeadManager>();

    public void setHumanoidVisible(String id, boolean visible)
    {
        if (!humanoidAnimationJoints.containsKey(id))
        {
            log.error("Trying to show or hide a virtual human with an ID that does not exist");
            return;
        }
        VGLNode vglVH = humanoidVGLNodes.get(id);
        setVisualisationVisible(vglVH, visible);
    }

    public HumanoidLoader loadHumanoid(String id, String resourcedirectory, String texturedirectory, String shaderdirectory,
            String filename, String postprocessing, HashMap<String, Float> permanentmorphtargets)
    {
        return loadHumanoid(id, resourcedirectory, texturedirectory, shaderdirectory, filename, postprocessing, permanentmorphtargets, true);
    }

    public HumanoidLoader loadHumanoid(String id, String resourcedirectory, String texturedirectory, String shaderdirectory,
            String filename, String postprocessing, HashMap<String, Float> permanentmorphtargets, boolean adjustBindPoses)
    {
        synchronized (RenderSync.getSync())
        {
            synchronized (AnimationSync.getSync())
            {
                if (humanoidAnimationJoints.containsKey(id))
                {
                    throw new RuntimeException("Trying to load a humanoid with an ID that already exists: " + id);
                }

                if (texturedirectory != null) GLTextureLoader.addTextureDirectory(texturedirectory);
                if (shaderdirectory != null) GLTextureLoader.addTextureDirectory(shaderdirectory);
                if (shaderdirectory != null) GLShaderProgramLoader.addShaderDirectory(shaderdirectory);

                HumanoidLoader humanoidLoader = null;
                try
                {
                    humanoidLoader = new HumanoidLoader(id, resourcedirectory, filename, postprocessing, adjustBindPoses);
                }
                catch (IOException ex)
                {
                    log.error("Error loading humanoid: {}", ex);
                    return null;
                }
                VGLNode model = humanoidLoader.getAvatarRenderNode();
                VJoint humanoidAnimationRoot = humanoidLoader.getAvatarAnimationRootJoint();// model.getRoot();
                GLScene theGLScene = humanoidLoader.getGLScene();

                // apply permanent morphs
                for (Entry<String, Float> entry : permanentmorphtargets.entrySet())
                {
                    theGLScene.addMorphTargets(new String[] { entry.getKey() }, new float[] { entry.getValue().floatValue() });
                }
                synchronized (AnimationSync.getSync())
                {
                    addVisualisation(model);
                    humanoidIds.add(id);
                    addHumanoidGLScene(id, theGLScene);
                    humanoidAnimationJoints.put(id, humanoidAnimationRoot);
                    humanoidVGLNodes.put(id, model);
                }
                return humanoidLoader;
            }
        }
    }

    public void unloadHumanoid(String id)
    {
        if (!humanoidAnimationJoints.containsKey(id))
        {
            log.error("Trying to unload a humanoid with an ID that does not exist");
            return;
        }
        VGLNode model = humanoidVGLNodes.get(id);
        synchronized (AnimationSync.getSync())
        {
            removeVisualisation(model);
            humanoidIds.remove(id);
            removeHumanoidGLScene(id);
            humanoidAnimationJoints.remove(id);
            humanoidVGLNodes.remove(id);
        }
    }

    // TODO: change to getSkeletonEmboidment(id); dit komt in een interface "SkeletonEnvironment"
    public VJoint getHumanoidRootJoint(String id)
    {
        return humanoidAnimationJoints.get(id);
    }

    // gegeven een humanoi id, laadt een mpeg4 facespec in. Wat moet er dan beschikbaar komen voor dat gezicht?
    // een MPEG4 embodiment, facs embodiment, morph embodiment? Een
    // facecontroller?
    public FaceController loadFace(String id, String fapDeformFile, String fapDeformResources, String fapDeformMesh,
            Collection<String> faceExpressionMorphTargets)
    {
        ArrayList<String> faceMeshGeometryNames = new ArrayList<String>();
        faceMeshGeometryNames.add(fapDeformMesh);
        ArrayList<Integer> faceMeshPrimitiveIndices = new ArrayList<Integer>();
        faceMeshPrimitiveIndices.add(Integer.valueOf(-1));
        return loadFace(id, fapDeformFile, fapDeformResources, faceMeshGeometryNames, faceMeshPrimitiveIndices, faceExpressionMorphTargets);
    }

    // the face mesh to be deformed is defined as a list of pairs <Geometry name, index of primitive mesh contained in that geometry)
    public FaceController loadFace(String id, String fapDeformFile, String fapDeformResources, ArrayList<String> faceMeshGeometryNames,
            ArrayList<Integer> faceMeshPrimitiveIndices, Collection<String> faceExpressionMorphTargets)
    {
        HMIFaceController fc = null;
        synchronized (RenderSync.getSync())
        {
            synchronized (AnimationSync.getSync())
            {
                GLHead head = null;
                GLScene gls = glScenes.get(id);
                HeadManager headManager = null;

                if (fapDeformFile != null)
                {
                    BufferedReader br = new Resources(fapDeformResources).getReader(fapDeformFile);
                    headManager = new HeadManager(br);
                    headManager.setHead(new GLHead());
                    head = (GLHead) headManager.readXMLFile();

                    // FIXME: this should have been verified in the loader, OR else we should now load a null head (cf the empty template face parameters file)
                    if (head == null) throw new RuntimeException("did not manage to load facemesh data from " + fapDeformFile);

                    // get all meshes from faceMeshGeometryNames,faceMeshPrimitiveIndices
                    for (int i = 0; i < faceMeshGeometryNames.size(); i++)
                    {
                        String geometryName = faceMeshGeometryNames.get(i);
                        int primitiveIndex = faceMeshPrimitiveIndices.get(i).intValue();

                        if (primitiveIndex == -1)
                        {
                            GLRenderList geomList = getFaceShape(geometryName, humanoidVGLNodes.get(id)).getGeometryList();
                            // System.out.println("old loading");
                            GLSkinnedMesh faceMesh = (GLSkinnedMesh) geomList.get(0);
                            head.addFaceMesh(faceMesh);
                        }
                        else
                        {
                            String name = geometryName;
                            if (primitiveIndex != 0) name += "-" + primitiveIndex;
                            GLRenderList geomList = getFaceShape(name, humanoidVGLNodes.get(id)).getGeometryList();

                            GLSkinnedMesh faceMesh = (GLSkinnedMesh) geomList.get(0);
                            head.addFaceMesh(faceMesh);
                        }
                    }
                    head.prepareVertexData();
                    // Eye leftEye = new
                    // Eye(((GenericVirtualHuman)getVirtualHuman()).getNextAnimationRootJoint().getPart("l_eyeball_joint"),
                    // head, true);
                    // Eye rightEye = new
                    // Eye(((GenericVirtualHuman)getVirtualHuman()).getNextAnimationRootJoint().getPart("r_eyeball_joint"),
                    // head, false);
                    // head.setEyes(leftEye, rightEye);
                    LowerJaw lowerJaw = null;
                    if (humanoidAnimationJoints.get(id).getPart("temporomandibular") != null)
                    {
                        lowerJaw = new LowerJaw(humanoidAnimationJoints.get(id).getPart("temporomandibular"));
                    }
                    head.setLowerJaw(lowerJaw);

                    // Neck movements (head rotation) do not work, mesh is not deformed. We need
                    // Bip01_T_te-node.
                    // Neck neck = new
                    // Neck(((GenericVirtualHuman)getVirtualHuman()).getNextAnimationRootJoint().getPart("skullbase"),
                    // head);
                    // head.setNeck(neck);
                }

                fc = new HMIFaceController(gls, head);
                humanoidHeads.put(id, head);
                humanoidHeadManagers.put(id, headManager);
                fc.setPossibleFaceMorphTargetNames(faceExpressionMorphTargets);
            }
        }
        return fc;
    }

    // tokenizer only to throw slightly more meaningful exception
    private GLShape getFaceShape(String shapeId, VGLNode renderNode)
    {
        GLRenderList shapeList = renderNode.getGLShapeList();

        GLShape shape;
        for (int i = 0; i < shapeList.size(); i++)
        {
            shape = (GLShape) shapeList.get(i);
            // System.out.println("NODE: " +shape.getId());
            if (shape.getId() == null) continue;
            if (shape.getId().equals(shapeId)) return shape;
        }
        throw new RuntimeException("Cannot find face shape with id " + shapeId);
    }

}
