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
package hmi.rsbrenderenvironment;

import hmi.animation.VJoint;
import hmi.faceanimation.FaceController;

import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import rsb.AbstractDataHandler;
import rsb.Event;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rsb.patterns.LocalServer;
import asap.rsbembodiments.RSBEmbodimentConstants;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.Rsbembodiments.AnimationSelection;
import asap.rsbembodiments.util.VJointRsbUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Floats;

/**
 * Steers a FaceController and a VJoint on the basis of Rsb messages
 * @author hvanwelbergen
 * 
 */
public class RsbRenderConnector
{
    private final FaceController faceController;
    private final VJoint rootJoint;

    @Getter
    private final String characterId;
    private LocalServer server;

    private AtomicReference<ImmutableList<String>> usedMorphs = new AtomicReference<>();
    private AtomicReference<ImmutableList<String>> jointList = new AtomicReference<>();

    public RsbRenderConnector(String characterId, FaceController controller, VJoint rootJoint)
    {
        this.faceController = controller;
        this.rootJoint = rootJoint;
        this.characterId = characterId;
        usedMorphs.set(new ImmutableList.Builder<String>().build());
    }

    public void deactivate() throws RSBException, InterruptedException
    {
        server.deactivate();
    }

    private class AnimationDataHandler extends AbstractDataHandler<AnimationData>
    {
        @Override
        public void handleEvent(AnimationData aData)
        {
            float q[] = Floats.toArray(aData.getJointQuatsList());
            ImmutableList<String> jList = jointList.get();
            for (int i = 0; i < jList.size(); i++)
            {
                rootJoint.getPart(jList.get(i)).setRotation(q, i * 4);
            }
            faceController.setMorphTargets(usedMorphs.get().toArray(new String[usedMorphs.get().size()]),
                    Floats.toArray(aData.getMorphWeightsList()));
        }

    }

    private class AnimationDataConfigCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) //throws Throwable
        {
            return new Event(AnimationDataConfigReply.class, AnimationDataConfigReply.newBuilder()
                    .setSkeleton(VJointRsbUtils.toRsbSkeleton(rootJoint))
                    .addAllAvailableMorphs(faceController.getPossibleFaceMorphTargetNames()).build());
        }
    }

    private class AnimationSelectionHandler extends AbstractDataHandler<AnimationSelection>
    {
        @Override
        public void handleEvent(AnimationSelection aSelection)
        {
            jointList.set(ImmutableList.copyOf(aSelection.getSelectedJointsList()));
            usedMorphs.set(ImmutableList.copyOf(aSelection.getSelectedMorphsList()));
        }
    }

    public void initialize()
    {
        final ProtocolBufferConverter<AnimationData> jointDataConverter = new ProtocolBufferConverter<AnimationData>(
                AnimationData.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigRequest> jointDataReqConverter = new ProtocolBufferConverter<AnimationDataConfigRequest>(
                AnimationDataConfigRequest.getDefaultInstance());
        final ProtocolBufferConverter<AnimationDataConfigReply> jointDataConfigReplyConverter = new ProtocolBufferConverter<AnimationDataConfigReply>(
                AnimationDataConfigReply.getDefaultInstance());
        final ProtocolBufferConverter<AnimationSelection> animationSelection = new ProtocolBufferConverter<AnimationSelection>(
                AnimationSelection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataReqConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(jointDataConfigReplyConverter);
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(animationSelection);

        final Factory factory = Factory.getInstance();
        server = factory.createLocalServer(RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY);
        try
        {
            server.activate();
            final Listener jointDataListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONDATA_CATEGORY);
            final Listener jointSelectionListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONSELECTION_CATEGORY);
            jointDataListener.activate();
            jointSelectionListener.activate();
            jointDataListener.addHandler(new AnimationDataHandler(), true);
            jointSelectionListener.addHandler(new AnimationSelectionHandler(), true);

            server.addMethod(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION, new AnimationDataConfigCallback());
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
