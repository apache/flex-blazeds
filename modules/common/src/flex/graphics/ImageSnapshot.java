/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package flex.graphics;

import java.util.HashMap;
import java.util.Map;

/**
 * This class corresponds to mx.graphics.ImageSnapshot on the client.  Clients may choose
 * to capture images and send them to the server via a RemoteObject call.  The PDF generation 
 * feature of LCDS may then be used to generate PDF's from these images.  
 *
 */
public class ImageSnapshot extends HashMap
{
    private static final long serialVersionUID = 7914317354403674061L;

    /**
     * Default constructor.
     */
    public ImageSnapshot()
    {
    }

    private Map properties;
    private String contentType;
    private byte[] data;
    private int height;
    private int width;

    /**
     * The content type for the image encoding format that was used to capture
     * this snapshot.
     * 
     * @return content type of this image
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Sets content type of this snapshot.
     * 
     * @param value content type
     */
    public void setContentType(String value)
    {
        contentType = value;
    }

    /**
     * The encoded data representing the image snapshot.
     * 
     * @return encoded image data
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * Set the encoded data representing the image snapshot.
     * 
     * @param value byte array of image data
     */
    public void setData(byte[] value)
    {
        data = value;
    }

    /**
     * The image height in pixels.
     * 
     * @return image height in pixels
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * Set image height in pixels.
     * 
     * @param value image height in pixels.
     */
    public void setHeight(int value)
    {
        height = value;
    }

    /**
     * Additional properties of the image.
     * 
     * @return a map containing the dynamically set properties on this snapshot
     */
    public Map getProperties()
    {
        return properties;
    }

    /**
     * Sets the map of dynamic properties for this snapshot.
     * 
     * @param value map containing dynamic properties for this snapshot
     */
    public void setProperties(Map value)
    {
        properties = value;
    }

    /**
     * The image width in pixels.
     * 
     * @return width in pixels
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Set width in pixels.
     * 
     * @param value width in pixels.
     */
    public void setWidth(int value)
    {
        width = value;
    }

}
