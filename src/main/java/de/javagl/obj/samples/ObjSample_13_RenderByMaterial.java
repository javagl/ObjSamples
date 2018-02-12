/*
 * www.javagl.de - Obj
 *
 * Copyright (c) 2008-2018 Marco Hutter - http://www.javagl.de
 */
package de.javagl.obj.samples;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;

/**
 * An example showing how to read an OBJ file and the associated MTL files,
 * and obtain the "material groups" of the OBJ. These are the groups that
 * have the same material and may be rendered with the same shader.
 */
@SuppressWarnings("javadoc")
public class ObjSample_13_RenderByMaterial 
{
    public static void main(String[] args) throws Exception
    {
        // Note: This sample shows the essential parts of the workflow
        // for extracting the material groups. In real applications,
        // the input streams should be closed after they have been 
        // read, and additional error handling should be added for
        // the case that some input files or materials are not found.
        
        // Read an OBJ file
        InputStream objInputStream = 
            new FileInputStream("./data/simpleSample.obj");
        Obj originalObj = ObjReader.read(objInputStream);
        
        // Convert the OBJ into a "renderable" OBJ. 
        // (See ObjUtils#convertToRenderable for details)
        Obj obj = ObjUtils.convertToRenderable(originalObj);
        
        // The OBJ may refer to multiple MTL files using the "mtllib"
        // directive. Each MTL file may contain multiple materials.
        // Here, all materials (in form of Mtl objects) are collected.
        List<Mtl> allMtls = new ArrayList<Mtl>();
        for (String mtlFileName : obj.getMtlFileNames())
        {
            InputStream mtlInputStream = 
                new FileInputStream("./data/" + mtlFileName);
            List<Mtl> mtls = MtlReader.read(mtlInputStream);
            allMtls.addAll(mtls);
        }
        
        // Split the OBJ into multiple parts. Each key of the resulting
        // map will be the name of one material. Each value will be 
        // an OBJ that contains the OBJ data that has to be rendered
        // with this material.
        Map<String, Obj> materialGroups = 
            ObjSplitting.splitByMaterialGroups(obj);
        
        for (Entry<String, Obj> entry : materialGroups.entrySet())
        {
            String materialName = entry.getKey();
            Obj materialGroup = entry.getValue();
            
            //System.out.println("Material name  : " + materialName);
            //System.out.println("Material group : " + materialGroup);
            
            // Find the MTL that defines the material with the current name
            Mtl mtl = findMtlForName(allMtls, materialName);
            
            // Render the current material group with this material:
            sendToRenderer(mtl, materialGroup);
        }
    }
    
    /**
     * Dummy method showing how to combine the information from an MTL
     * and an OBJ in order to render it with OpenGL
     * 
     * @param mtl The MTL containing the material properties
     * @param obj The OBJ containing the geometry data
     */
    @SuppressWarnings("unused")
    private static void sendToRenderer(Mtl mtl, Obj obj)
    {
        // Extract the relevant material properties. These properties can 
        // be used to set up the renderer. For example, they may be passed
        // as uniform variables to a shader
        FloatTuple diffuseColor = mtl.getKd();
        FloatTuple specularColor = mtl.getKs();
        // ...
        
        // Extract the geometry data. This data can be used to create
        // the vertex buffer objects and vertex array objects for OpenGL
        IntBuffer indices = ObjData.getFaceVertexIndices(obj);
        FloatBuffer vertices = ObjData.getVertices(obj);
        FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
        FloatBuffer normals = ObjData.getNormals(obj);        
        // ...
        
        // Print some information about the object that would be rendered 
        System.out.println(
            "Rendering an object with " + (indices.capacity() / 3) 
            + " triangles with " + mtl.getName() 
            + ", having diffuse color " + diffuseColor);
        
    }

    /**
     * Returns the Mtl from the given sequence that has the given name,
     * or <code>null</code> if no such Mtl can be found
     * 
     * @param mtls The Mtl instances
     * @param name The material name
     * @return The Mtl with the given material name
     */
    private static Mtl findMtlForName(Iterable<? extends Mtl> mtls, String name)
    {
        for (Mtl mtl : mtls)
        {
            if (mtl.getName().equals(name))
            {
                return mtl;
            }
        }
        return null;
    }
}
