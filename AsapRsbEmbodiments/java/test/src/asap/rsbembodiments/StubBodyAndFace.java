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
package asap.rsbembodiments;

import hmi.animation.VJoint;
import hmi.faceanimation.FaceController;

import java.util.ArrayList;
import java.util.List;

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
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.Rsbembodiments.AnimationSelection;
import asap.rsbembodiments.util.VJointRsbUtils;

import com.google.common.primitives.Floats;

public class StubBodyAndFace
{
    private final LocalServer server;
    private final VJoint vjoint;
    private final FaceController faceController;

    @Getter
    private List<String> jointList = new ArrayList<String>();
    @Getter
    private List<String> morphList = new ArrayList<String>();
    
    private class JointDataConfigCallback extends EventCallback
    {
        @Override
        public Event invoke(final Event request) //throws Throwable
        {
            return new Event(AnimationDataConfigReply.class, AnimationDataConfigReply.newBuilder()
                    .setSkeleton(VJointRsbUtils.toRsbSkeleton(vjoint))
                    .addAllAvailableMorphs(faceController.getPossibleFaceMorphTargetNames()).build());
        }
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
            for (int i = 0; i < jointList.size(); i++)
            {
                vjoint.getPart(jointList.get(i)).setRotation(q, i * 4);
            }
            if(aData.getRootTranslationCount()>0)
            {
                float tRoot[] = Floats.toArray(aData.getRootTranslationList());
                vjoint.setTranslation(tRoot);
            } 
            faceController.setMorphTargets(morphList.toArray(new String[morphList.size()]), Floats.toArray(aData.getMorphWeightsList()));
        }

    }

    private class AnimationSelectionHandler extends AbstractDataHandler<AnimationSelection>
    {
        @Override
        public void handleEvent(AnimationSelection aSelection)
        {
            jointList = new ArrayList<String>(aSelection.getSelectedJointsList());
            morphList = new ArrayList<String>(aSelection.getSelectedMorphsList());
        }
    }

    public StubBodyAndFace(VJoint root, FaceController fc, String characterId) throws RSBException, InterruptedException
    {
        this.vjoint = root;
        this.faceController = fc;
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
        server = factory.createLocalServer(RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY+"/"+characterId);
        server.activate();

        final Listener jointDataListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONDATA_CATEGORY+"/"+characterId);
        final Listener jointSelectionListener = factory.createListener(RSBEmbodimentConstants.ANIMATIONSELECTION_CATEGORY+"/"+characterId);
        jointDataListener.activate();
        jointSelectionListener.activate();
        jointDataListener.addHandler(new AnimationDataHandler(), true);
        jointSelectionListener.addHandler(new AnimationSelectionHandler(), true);

        server.addMethod(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION, new JointDataConfigCallback());
    }
}
