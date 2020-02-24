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
import hmi.environmentbase.Embodiment;
import hmi.environmentbase.EmbodimentLoader;
import hmi.environmentbase.Environment;
import hmi.environmentbase.Loader;
import hmi.faceanimation.FaceController;
import hmi.faceembodiments.loader.EyelidMorpherLoader;
import hmi.graphics.util.HumanoidLoader;
import hmi.util.ArrayUtils;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the embodiments that reside in a standard HmiRenderEnvironment. */
public class HmiRenderEmbodimentLoader implements EmbodimentLoader
{
    private Logger logger = LoggerFactory.getLogger(HmiRenderEmbodimentLoader.class.getName());

    private XMLStructureAdapter adapter = new XMLStructureAdapter();

    private Embodiment embodiment = null;
    @Getter
    private String id = "";
    private String vhId = "";
    // some variables cached during loading
    // no face specified? Use HmiRenderEMbodimentNoFace instead of HmiRenderEMbodiment
    String textureDir;
    String shaderDir;
    String resourceDir;
    String bodyFilename;
    String postprocessing;
    private HashMap<String, Float> permanentmorphtargets = new HashMap<String, Float>();

    private boolean includeFace = false;
    @Getter
    private ArrayList<String> faceExpressionMorphTargets = new ArrayList<String>();
    @Getter
    private String fapDeformFile = null;
    @Getter
    private String fapDeformResources = null;
    // the list of face meshes in faceMeshGeometryNames,faceMeshPrimitiveIndices
    @Getter
    private ArrayList<String> faceMeshGeometryNames = new ArrayList<String>();
    // if -1: take first mesh
    @Getter
    private ArrayList<Integer> faceMeshPrimitiveIndices = new ArrayList<Integer>();

    private HmiRenderEnvironment hre = null;
    private HumanoidLoader hl = null;

    @Override
    public void readXML(XMLTokenizer tokenizer, String loaderId, String vhId, String vhName, Environment[] environments,
            Loader... requiredLoaders) throws IOException
    {
        id = loaderId;
        this.vhId = vhId;
        for (Environment e : environments)
        {
            if (e instanceof HmiRenderEnvironment) hre = (HmiRenderEnvironment) e;
        }
        if (hre == null)
        {
            throw new RuntimeException("HmiRenderEmbodimentLoader requires an Environment of type HmiRenderEnvironment");
        }

        EyelidMorpherLoader eml = ArrayUtils.getFirstClassOfType(requiredLoaders, EyelidMorpherLoader.class);
        while (!tokenizer.atETag("Loader"))
        {
            readSection(tokenizer);
        }
        constructEmbodiment(tokenizer, loaderId, this.vhId);
        if (embodiment instanceof HmiRenderBodyAndFaceEmbodiment && eml != null)
        {
            ((HmiRenderBodyAndFaceEmbodiment) embodiment).setEyelidMorpher(eml.getEmbodiment());
        }
    }

    protected void readSection(XMLTokenizer tokenizer) throws IOException
    {
        HashMap<String, String> attrMap = null;
        if (tokenizer.atSTag("Body"))
        {
            attrMap = tokenizer.getAttributes();
            tokenizer.takeSTag("Body");
            textureDir = adapter.getOptionalAttribute("texturedir", attrMap, "");
            shaderDir = adapter.getOptionalAttribute("shaderdir", attrMap, "");
            resourceDir = adapter.getOptionalAttribute("resourcedir", attrMap, "");
            bodyFilename = adapter.getRequiredAttribute("filename", attrMap, tokenizer);
            postprocessing = adapter.getOptionalAttribute("postprocessing", attrMap, "NONE");

            tokenizer.takeETag("Body");
        }
        else if (tokenizer.atSTag("EmbodimentSettings"))
        {
            attrMap = tokenizer.getAttributes();
            tokenizer.takeSTag("EmbodimentSettings");
            vhId = adapter.getOptionalAttribute("embodimentId", attrMap, vhId);
            tokenizer.takeETag("EmbodimentSettings");
        }
        else if (tokenizer.atSTag("PermanentMorphs"))
        {
            tokenizer.takeSTag("PermanentMorphs");
            while (tokenizer.atSTag())
            {
                String tag = tokenizer.getTagName();
                if (!tag.equals("PermanentMorph")) throw new XMLScanException("Unknown tag in PermanentMorphs: " + tag);
                HashMap<String, String> attrMap2 = tokenizer.getAttributes();
                String target = adapter.getRequiredAttribute("target", attrMap2, tokenizer);
                float amount = adapter.getRequiredFloatAttribute("amount", attrMap2, tokenizer);
                permanentmorphtargets.put(target, new Float(amount));
                tokenizer.takeSTag(tag);
                tokenizer.takeETag(tag);
            }
            tokenizer.takeETag("PermanentMorphs");
        }
        else if (tokenizer.atSTag("Face"))
        {
            includeFace = true;
            tokenizer.takeSTag("Face");
            while (tokenizer.atSTag())
            {
                if (tokenizer.atSTag("FapDeform"))
                {
                    HashMap<String, String> attrMap2 = tokenizer.getAttributes();
                    fapDeformFile = adapter.getRequiredAttribute("filename", attrMap2, tokenizer);
                    fapDeformResources = adapter.getOptionalAttribute("resources", attrMap2, "");
                    String faceDeformMesh = adapter.getOptionalAttribute("facemesh", attrMap2);
                    tokenizer.takeSTag("FapDeform");
                    // There was no face deform mesh attribute? then a list of face deforms is given as child elements
                    if (faceDeformMesh == null)
                    {
                        while (!tokenizer.atETag("FapDeform"))
                        {
                            if (tokenizer.atSTag("Mesh"))
                            {
                                HashMap<String, String> attrMap3 = tokenizer.getAttributes();
                                // System.out.println(attrMap3);
                                faceMeshGeometryNames.add(adapter.getRequiredAttribute("geometry", attrMap3, tokenizer));
                                faceMeshPrimitiveIndices
                                        .add(Integer.valueOf(adapter.getRequiredIntAttribute("primitiveindex", attrMap3, tokenizer)));
                                tokenizer.takeSTag("Mesh");
                                tokenizer.takeETag("Mesh");
                            }
                            else
                            {
                                throw tokenizer.getXMLScanException("FapDeform can only contain Mesh children");
                            }
                        }
                    }
                    else
                    {
                        // there was a face deform mesh attribute? then you'll want first mesh below that name.
                        faceMeshGeometryNames.add(faceDeformMesh);
                        faceMeshPrimitiveIndices.add(Integer.valueOf(-1));
                    }
                    tokenizer.takeETag("FapDeform");
                }
                if (faceMeshGeometryNames.size() == 0)
                    throw tokenizer.getXMLScanException("FapDeform needs to have either a fapDeformMesh attribute or Mesh children!");
                else if (tokenizer.atSTag("FaceExpressionMorphTargets"))
                {
                    tokenizer.takeSTag("FaceExpressionMorphTargets");
                    while (tokenizer.atSTag())
                    {
                        HashMap<String, String> attrMap2 = tokenizer.getAttributes();
                        tokenizer.takeSTag("Target");
                        faceExpressionMorphTargets.add(adapter.getRequiredAttribute("name", attrMap2, tokenizer));
                        tokenizer.takeETag("Target");
                    }
                    tokenizer.takeETag("FaceExpressionMorphTargets");
                }
            }
            tokenizer.takeETag("Face");
        }
        else
        {

        }
    }

    /** tokenizer used for throwing scanexceptions */
    private void constructEmbodiment(XMLTokenizer tokenizer, String loaderId, String vhId)
    {
        if (bodyFilename == null)
        {
            throw tokenizer.getXMLScanException("Missing Body tag in VHLoader data");
        }
        hl = hre.loadHumanoid(vhId, resourceDir, textureDir, shaderDir, bodyFilename, postprocessing, permanentmorphtargets);
        // create embodiment

        // create embodiment
        if (includeFace)
        {
            embodiment = new HmiRenderBodyAndFaceEmbodiment();
        }
        else
        {
            embodiment = new HmiRenderBodyEmbodiment();
        }

        // set body stuff
        VJoint root = hre.getHumanoidRootJoint(vhId);
        HmiRenderBodyEmbodiment bodyEmbodiment = (HmiRenderBodyEmbodiment) embodiment;
        bodyEmbodiment.setAnimationVJoint(root);
        bodyEmbodiment.setGLScene(hl.getGLScene());

        if (includeFace)
        {

            FaceController fc = hre.loadFace(vhId, fapDeformFile, fapDeformResources, faceMeshGeometryNames, faceMeshPrimitiveIndices,
                    faceExpressionMorphTargets);
            ((HmiRenderBodyAndFaceEmbodiment) embodiment).setFaceController(fc);
        }

        hre.addCopyEmbodiment((CopyEmbodiment) embodiment);

    }

    @Override
    public void unload()
    {
        // HmiRenderBodyEmbodiment bodyEmbodiment = (HmiRenderBodyEmbodiment)embodiment;
        // removing from environments
        logger.debug("Removing visualisation");
        hre.removeCopyEmbodiment((CopyEmbodiment) embodiment);
        hre.unloadHumanoid(vhId);

    }

    @Override
    public Embodiment getEmbodiment()
    {
        return embodiment;
    }
}
