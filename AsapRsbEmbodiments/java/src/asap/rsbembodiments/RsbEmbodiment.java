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

import hmi.environmentbase.Embodiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.RemoteServer;
import asap.rsbembodiments.Rsbembodiments.AnimationData;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigReply;
import asap.rsbembodiments.Rsbembodiments.AnimationDataConfigRequest;
import asap.rsbembodiments.Rsbembodiments.AnimationSelection;
import asap.rsbembodiments.Rsbembodiments.Skeleton;

import com.google.common.collect.ImmutableList;

/**
 * Interfaces with an Rsb graphical environment.
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class RsbEmbodiment implements Embodiment
{
    @Getter
    private String id;

    private List<String> selectedJoints = Collections.synchronizedList(new ArrayList<String>());
    private List<String> selectedMorphs = Collections.synchronizedList(new ArrayList<String>());

    private AtomicBoolean initialized = new AtomicBoolean(false);

    private Informer<AnimationData> jointDataInformer;
    private Informer<AnimationSelection> animationSelectionInformer;
    @Getter
    private Skeleton skeleton;
    @Getter
    private ImmutableList<String> availableMorphs;
    private String characterId = "";
    private String characterScope = "";
    
    private void setupConverters()
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
    }

    public void initialize(String characterId)
    {
        initialize(characterId,characterId);
    }
    
    /**
     * @param characterScope the subscope used in RSB messaging. An empty String means no subscope. 
     */
    public void initialize(String characterId, String characterScope)
    {
        if (initialized.getAndSet(true))
        {
            return;
        }
        this.characterId = characterId;
        this.characterScope = characterScope;
        
        setupConverters();
        initInformers();
        AnimationDataConfigReply reply = getAnimationConfig();
        availableMorphs = ImmutableList.copyOf(reply.getAvailableMorphsList());
        skeleton = reply.getSkeleton();
    }

    private AnimationDataConfigReply getAnimationConfig()
    {
        AnimationDataConfigReply reply;
        String scope = RSBEmbodimentConstants.ANIMATIONDATACONFIG_CATEGORY+(characterScope.isEmpty()?"":"/"+characterScope);
        final RemoteServer server = Factory.getInstance().createRemoteServer(scope);
        try
        {
            server.activate();
            reply = server.call(RSBEmbodimentConstants.ANIMATIONDATACONFIG_REQUEST_FUNCTION, AnimationDataConfigRequest.newBuilder()
                    .setCharacterId(characterId).build());
        }
        catch (RSBException e)
        {
            log.error("Did not get AnimationDataConfigReply for character \"{}\" on scope \"{}\"", characterId, characterScope);
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            log.error("Did not get AnimationDataConfigReply for character \"{}\" on scope \"{}\"", characterId, characterScope);
            throw new RuntimeException(e);
        }
        catch (TimeoutException e)
        {
            log.error("Did not get AnimationDataConfigReply for character \"{}\" on scope \"{}\"", characterId, characterScope);
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            log.error("Did not get AnimationDataConfigReply for character \"{}\" on scope \"{}\"", characterId, characterScope);
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                server.deactivate();
            }
            catch (RSBException e)
            {
                log.warn("error deactivating server ", e);
            }
            catch (InterruptedException e)
            {
                Thread.interrupted();
                log.warn("error deactivating server ", e);
            }
        }
        return reply;
    }

    public void shutdown()
    {
        try
        {
            jointDataInformer.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("RSB Exception", e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            log.warn("InterruptedException", e);
        }
        try
        {
            animationSelectionInformer.deactivate();
        }
        catch (RSBException e)
        {
            log.warn("RSB Exception", e);
        }
        catch (InterruptedException e)
        {
            Thread.interrupted();
            log.warn("InterruptedException", e);
        }
    }

    private void initInformers()
    {
        try
        {
            String subScope = characterScope.isEmpty()?"":"/"+characterScope;
            jointDataInformer = Factory.getInstance().createInformer(RSBEmbodimentConstants.ANIMATIONDATA_CATEGORY+subScope);
            animationSelectionInformer = Factory.getInstance().createInformer(RSBEmbodimentConstants.ANIMATIONSELECTION_CATEGORY+subScope);
            jointDataInformer.activate();
            animationSelectionInformer.activate();
        }
        catch (InitializeException e)
        {
            throw new RuntimeException(e);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }

    public RsbEmbodiment()
    {

    }

    public void selectJoints(List<String> joints)
    {
        selectedJoints.clear();
        selectedJoints.addAll(joints);
        selectAnimation();
    }
    
    public void selectMorphs(List<String> morphs)
    {
        selectedMorphs.clear();
        selectedMorphs.addAll(morphs);
        selectAnimation();
    }

    private void selectAnimation()
    {
        try
        {
            animationSelectionInformer.publish(AnimationSelection.newBuilder().addAllSelectedJoints(selectedJoints)
                    .addAllSelectedMorphs(selectedMorphs).setCharacterId(characterId).build());
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void sendAnimationData(AnimationData data)
    {
        try
        {
            jointDataInformer.publish(data);
        }
        catch (RSBException e)
        {
            throw new RuntimeException(e);
        }
    }
}
