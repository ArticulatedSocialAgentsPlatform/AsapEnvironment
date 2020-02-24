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
package hmi.texturedrectanglespictureembodiment;

import hmi.animation.VJoint;
import hmi.graphics.opengl.GLTextures;
import hmi.renderenvironment.HmiRenderEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Provides a 3D rectangle on which pictures can be shown 
 * @author Herwin
 */
public class RectanglesPictureEmbodiment
{
    private final HmiRenderEnvironment renderEnvironment;
    private final VJoint rootJoint;
    private NavigableMap<Float,String> layerMap = new TreeMap<Float,String>();
    private Map<String,String> textureMap = new HashMap<String,String>();
    
    
    public RectanglesPictureEmbodiment(HmiRenderEnvironment env, VJoint root)
    {
        renderEnvironment = env;
        rootJoint = root;
    }
    
    public void preloadImage(String imageId, String resourcePath, String fileName)
    {
        GLTextures.addTextureDirectory(resourcePath);
        GLTextures.getGLTexture(fileName);
        textureMap.put(imageId, fileName);
    }
    
    public void setImage(String imageId, float layer)
    {
        if(!textureMap.containsKey(imageId))
        {
            throw new RuntimeException("image "+imageId+" not preloaded");
        }
        VJoint vj = renderEnvironment.loadTexturedRectangle(constructId(imageId,layer), 1, 1, textureMap.get(imageId));
        rootJoint.addChild(vj);
        layerMap.put(layer, imageId);
        depthSort();
    }
    
    public void removeImage(float layer)
    {
        if(layerMap.containsKey(layer))
        {
            String id = constructId(layerMap.get(layer),layer);
            rootJoint.removeChild(renderEnvironment.getObjectRootJoint(id));
            renderEnvironment.unloadObject(id);
        }
        layerMap.remove(layer);
        depthSort();
    }
    
    private String constructId(String imageId, float layer)
    {
        return imageId+layer;
    }
    
    private void depthSort()
    {
        float depth = 0;
        for(Float f:layerMap.navigableKeySet())
        {
            renderEnvironment.getObjectRootJoint(constructId(layerMap.get(f),f)).setTranslation(0,0,depth);
            depth -= 0.001f;
        }
    }
}
