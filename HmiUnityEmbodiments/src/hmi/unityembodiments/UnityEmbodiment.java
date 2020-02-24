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
package hmi.unityembodiments;

import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.array;
import static nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import hmi.animation.VJoint;
import hmi.animationembodiments.SkeletonEmbodiment;
import hmi.environment.bodyandfaceembodiments.BodyAndFaceEmbodiment;
import hmi.environmentbase.CopyEnvironment;
import hmi.faceanimation.FaceController;
import hmi.faceanimation.model.MPEG4Configuration;
import hmi.faceembodiments.FaceEmbodiment;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import lombok.extern.slf4j.Slf4j;

import hmi.unityembodiments.UnityEmbodimentConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import nl.utwente.hmi.middleware.Middleware;
import nl.utwente.hmi.middleware.MiddlewareListener;
import nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.ArrayNodeBuilder;
import nl.utwente.hmi.middleware.helpers.JsonNodeBuilders.ObjectNodeBuilder;
import nl.utwente.hmi.middleware.worker.AbstractWorker;

/**
 * Interfaces with Virtual Humans in Unity.
 * For the Unity code, see <a href="http://google.com">https://github.com/hmi-utwente/UnityAsapIntegration</a>
 * @author jankolkmeier@gmail.com
 */
@Slf4j
public class UnityEmbodiment extends AbstractWorker
        implements MiddlewareListener, SkeletonEmbodiment, FaceEmbodiment, BodyAndFaceEmbodiment, FaceController
{

    private Middleware middleware;

    byte[] msgbuf;
    private VJoint animationRoot = null;
    private List<VJoint> jointList;
    private String vhId = "";
    private String loaderId = "";
    private boolean configured = false;

    private MPEG4Configuration currentConfig = new MPEG4Configuration();
    private CopyEnvironment ce = null;

    private boolean useBinary = false;

    private LinkedHashMap<String, Float> faceMorphTargets;
    private LinkedBlockingQueue<WorldObjectUpdate> objectUpdates;

    private WorldObjectEnvironment woe;

    public UnityEmbodiment(String vhId, String loaderId, Middleware m, boolean useBinary,
            WorldObjectEnvironment woe, CopyEnvironment ce)
    {
        this.vhId = vhId;
        this.loaderId = loaderId;
        this.woe = woe;
        this.ce = ce;
        this.useBinary = useBinary;
        msgbuf = new byte[32768]; // Buffer: ~100bones * (4bytes * (3pos + 4rot) + 255) = ~28300
        objectUpdates = new LinkedBlockingQueue<WorldObjectUpdate>();

        configured = false;
        m.addListener(this);
        (new Thread(this)).start();
        middleware = m;
    }
    
    public void shutdown()
    {
        // TODO: should we try to close middleware connections here?
    }
    
    public boolean isConfigured()
    {
        return configured;
    }

    @Override
    public void receiveData(JsonNode jn)
    {
        addDataToQueue(jn);
    }

    @Override
    public void processData(JsonNode jn)
    {
        if (jn.has(UnityEmbodimentConstants.AUPROT_PROP_MSGTYPE))
        {
            String msgType = jn.get(UnityEmbodimentConstants.AUPROT_PROP_MSGTYPE).asText();
            switch (msgType)
            {
            case UnityEmbodimentConstants.AUPROT_MSGTYPE_AGENTSPEC: // Description of a Virtual Human
                ParseAgentSpec(jn);
                break;
            case UnityEmbodimentConstants.AUPROT_MSGTYPE_WORLDOBJECTUPDATE: // Informs ASAP about (changed) objects in Unity.
                ParseWorldObjectUpdate(jn);
                break;
            case UnityEmbodimentConstants.AUPROT_MSGTYPE_AGENTSTATE: // For feedback during unity-driven animations
                // NOT IMPLEMENTED YET
                break;
            default:
                break;
            }
        }
    }

    public void SendAgentSpecRequest(String id, String source)
    {
        JsonNode msg = object(UnityEmbodimentConstants.AUPROT_PROP_MSGTYPE, UnityEmbodimentConstants.AUPROT_MSGTYPE_AGENTSPECREQUEST,
                UnityEmbodimentConstants.AUPROT_PROP_AGENTID, id, UnityEmbodimentConstants.AUPROT_PROP_SOURCE, source).end();

        middleware.sendData(msg);
    }

    
    // TODO: MA - this is called once for each UnityEmbodiment agent...
    void ParseWorldObjectUpdate(JsonNode jn)
    {
        int nObjects = jn.get(UnityEmbodimentConstants.AUPROT_PROP_N_OBJECTS).asInt();

        for (Iterator<JsonNode> objects_iter = jn.get(UnityEmbodimentConstants.AUPROT_PROP_OBJECTS).elements(); objects_iter.hasNext();)
        {
            JsonNode object = objects_iter.next();
            String objectName = object.get(UnityEmbodimentConstants.AUPROT_PROP_OBJECT_ID).asText();
            JsonNode transform = object.get(UnityEmbodimentConstants.AUPROT_PROP_TRANSFORM);

            float x = transform.get(0).floatValue();
            float y = transform.get(1).floatValue();
            float z = transform.get(2).floatValue();
            float qx = transform.get(3).floatValue();
            float qy = transform.get(4).floatValue();
            float qz = transform.get(5).floatValue();
            float qw = transform.get(6).floatValue();
            float[] translation = { x, y, z, qw, qx, qy, qz };

            try
            {
                objectUpdates.put(new WorldObjectUpdate(objectName, translation));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    void ParseAgentSpec(JsonNode jn)
    {
        log.info("reading agent spec (V2)");
        String id = jn.get(UnityEmbodimentConstants.AUPROT_PROP_AGENTID).asText();
        int nBones = jn.get(UnityEmbodimentConstants.AUPROT_PROP_N_BONES).asInt(0);
        int nFaceTargets = jn.get(UnityEmbodimentConstants.AUPROT_PROP_N_FACETARGETS).asInt(0);
        
        if (!id.equals(vhId)) {
            log.info("Ignoring agentspec for different vhId: {} (this is {})", id, vhId);
            return;
        }

        HashMap<String, VJoint> jointsLUT = new HashMap<String, VJoint>();
        faceMorphTargets = new LinkedHashMap<String, Float>();
        jointList = new ArrayList<VJoint>();
        
        log.info("Parsing skeleton {}, with {} bones...", id, nBones);

        for (Iterator<JsonNode> bones_iter = jn.get(UnityEmbodimentConstants.AUPROT_PROP_BONES).elements(); bones_iter.hasNext();)
        {
            JsonNode bone = bones_iter.next();
            String bName = bone.get(UnityEmbodimentConstants.AUPROT_PROP_BONE_ID).asText();
            String pName = bone.get(UnityEmbodimentConstants.AUPROT_PROP_BONE_PARENTID).asText();
            String hAnimName = bone.get(UnityEmbodimentConstants.AUPROT_PROP_BONE_HANIMNAME).asText();

            JsonNode transform = bone.get(UnityEmbodimentConstants.AUPROT_PROP_TRANSFORM);

            float x = transform.get(0).floatValue();
            float y = transform.get(1).floatValue();
            float z = transform.get(2).floatValue();
            float qx = transform.get(3).floatValue();
            float qy = transform.get(4).floatValue();
            float qz = transform.get(5).floatValue();
            float qw = transform.get(6).floatValue();
            VJoint current = new VJoint();
            current.setName(bName);
            current.setSid(hAnimName);
            current.setId(bName); // could be prefixed by vhId to be "truly" unique?
            current.setTranslation(x, y, z);
            current.setRotation(qw, qx, qy, qz);

            if (pName.length() == 0)
            {
                animationRoot = current;
            }
            else
            {
                jointsLUT.get(pName).addChild(current);
            }

            jointList.add(current);
            jointsLUT.put(bName, current);
            log.debug(String.format("    Bone %s, child of %s. HAnim: %s // [%.2f %.2f %.2f] [%.2f %.2f %.2f %.2f]", bName, pName, hAnimName,
                    x, y, z, qw, qx, qy, qz));
        }

        log.info("...and {} face targets...", nFaceTargets);
        for (Iterator<JsonNode> faceTargets_iter = jn.get(UnityEmbodimentConstants.AUPROT_PROP_FACETARGETS).elements(); faceTargets_iter
                .hasNext();)
        {
            JsonNode faceTarget = faceTargets_iter.next();
            faceMorphTargets.put(faceTarget.asText(), 0.0f);
            log.debug(String.format("    Face Target: {}", faceTarget.asText()));
        }
        ce.addCopyEmbodiment(this);
        configured = true;
    }

    public VJoint getAnimationVJoint()
    {
        return animationRoot;
    }

    @Override
    public synchronized void copy()
    {
        while (!objectUpdates.isEmpty())
        {
            // TODO: Is poll() better than take() here? If take() blocks the copy()
            // until we receive an object update, that might really affect framerate, no?
            WorldObjectUpdate u = objectUpdates.poll();
            WorldObject o = woe.getWorldObjectManager().getWorldObject(u.id);
            if (o == null)
            {
                VJoint newJoint = new VJoint();
                newJoint.setTranslation(u.data);
                woe.getWorldObjectManager().addWorldObject(u.id, new VJointWorldObject(newJoint));
            }
            else
            {
                o.setTranslation(u.data);
            }
        }

        ObjectNodeBuilder msgBuilder = object(UnityEmbodimentConstants.AUPROT_PROP_MSGTYPE,
                UnityEmbodimentConstants.AUPROT_MSGTYPE_AGENTSTATE);
        msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_AGENTID, vhId);
        msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_N_BONES, jointList.size());
        msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_N_FACETARGETS, faceMorphTargets.size());

        if (!useBinary)
        {
            ArrayNodeBuilder boneTranslationArrayBuilder = array();
            ArrayNodeBuilder boneValueArrayBuilder = array();
            for (int j = 0; j < jointList.size(); j++)
            {
                VJoint cur = jointList.get(j);
                if (j<2) {
                    ArrayNodeBuilder translationArrayBuilder = array();
                    float[] translation = new float[3];
                    cur.getTranslation(translation);
                    translationArrayBuilder.with(UnityEmbodiment.round(translation[0], 4));
                    translationArrayBuilder.with(UnityEmbodiment.round(translation[1], 4));
                    translationArrayBuilder.with(UnityEmbodiment.round(translation[2], 4));
                    boneTranslationArrayBuilder.with(object().with("t", translationArrayBuilder.end()));
                }
                ArrayNodeBuilder rotationArrayBuilder = array();
                float[] rotation = new float[4];
                cur.getRotation(rotation);
                rotationArrayBuilder.with(UnityEmbodiment.round(rotation[1], 4));
                rotationArrayBuilder.with(UnityEmbodiment.round(rotation[2], 4));
                rotationArrayBuilder.with(UnityEmbodiment.round(rotation[3], 4));
                rotationArrayBuilder.with(UnityEmbodiment.round(rotation[0], 4));
                boneValueArrayBuilder.with(object().with("r", rotationArrayBuilder.end()));
            }

            msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_BONE_TRANSLATIONS, boneTranslationArrayBuilder.end());
            msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_BONE_VALUES, boneValueArrayBuilder.end());
        }
        else
        {
            ByteBuffer out = ByteBuffer.wrap(msgbuf);
            out.order(ByteOrder.LITTLE_ENDIAN);
            out.rewind();

            for (int j = 0; j < jointList.size(); j++)
            {
                VJoint cur = jointList.get(j);
                if (j<2) {
                    float[] translation = new float[3];
                    cur.getTranslation(translation);
                    out.putFloat(translation[0]);
                    out.putFloat(translation[1]);
                    out.putFloat(translation[2]);
                }
                float[] rotation = new float[4];
                cur.getRotation(rotation);
                out.putFloat(rotation[1]);
                out.putFloat(rotation[2]);
                out.putFloat(rotation[3]);
                out.putFloat(rotation[0]);
            }

            msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_BINARY_BONE_VALUES,
                    Base64.encode(Arrays.copyOf(out.array(), out.position())));
        }

        if (!useBinary)
        {
            ArrayNodeBuilder faceTargetArrayBuilder = array();
            for (Map.Entry<String, Float> entry : faceMorphTargets.entrySet())
            {
                faceTargetArrayBuilder.with(entry.getValue());
            }
            msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_FACETARGET_VALUES, faceTargetArrayBuilder.end());
        }
        else
        {
            ByteBuffer out = ByteBuffer.wrap(msgbuf);
            out.order(ByteOrder.LITTLE_ENDIAN);
            out.rewind();

            for (Map.Entry<String, Float> entry : faceMorphTargets.entrySet())
            {
                out.putFloat(entry.getValue());
            }

            msgBuilder.with(UnityEmbodimentConstants.AUPROT_PROP_BINARY_FACETARGET_VALUES,
                    Base64.encode(Arrays.copyOf(out.array(), out.position())));
        }

        JsonNode msg = msgBuilder.end();
        middleware.sendData(msg);
    }

    public static float round(float number, int scale)
    {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    // FaceEmbodiment
    @Override
    public FaceController getFaceController()
    {
        return this;
    }

    @Override
    public void setMorphTargets(String[] targetNames, float[] weights)
    {
        for (int i = 0; i < targetNames.length; i++)
        {
            faceMorphTargets.replace(targetNames[i], weights[i]);
        }
    }

    @Override
    public float getCurrentWeight(String targetName)
    {
        log.debug("getCurrentWeight " + targetName);
        // TODO: unimplemented for now because never called?
        return 0.0f;
    }

    @Override
    public void addMorphTargets(String[] targetNames, float[] weights)
    {
        // TODO: unimplemented for now because never called?
        log.debug("addMorphTargets");
        for (int i = 0; i < targetNames.length; i++)
        {
            log.debug(targetNames[i] + " " + weights[i]);
        }
    }

    @Override
    public void removeMorphTargets(String[] targetNames, float[] weights)
    {
        // TODO: unimplemented for now because never called?
        log.debug("removeMorphTargets");
        for (int i = 0; i < targetNames.length; i++)
        {
            log.debug(targetNames[i] + " " + weights[i]);
        }
    }

    @Override
    public Collection<String> getPossibleFaceMorphTargetNames()
    {
        return faceMorphTargets.keySet();
    }
    /////////////////

    // FaceController
    @Override
    public void setMPEG4Configuration(MPEG4Configuration config)
    {
        currentConfig.setValues(Arrays.copyOf(config.getValues(), config.getValues().length));
    }

    @Override
    public void addMPEG4Configuration(MPEG4Configuration config)
    {
        log.info("addMPEG4Configuration");
        currentConfig.addValues(config);
    }

    @Override
    public void removeMPEG4Configuration(MPEG4Configuration config)
    {
        log.info("removeMPEG4Configuration");
        currentConfig.removeValues(config);

    }
    /////////////////

    @Override
    public String getId()
    {
        return loaderId;
    }
}
