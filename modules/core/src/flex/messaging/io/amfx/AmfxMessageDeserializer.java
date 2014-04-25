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
package flex.messaging.io.amfx;

import flex.messaging.io.MessageDeserializer;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.AmfTrace;
import flex.messaging.io.SerializationContext;
import flex.messaging.MessageException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SAX based AMFX Parser.
 *
 * @author Peter Farland
 */
public class AmfxMessageDeserializer extends DefaultHandler implements MessageDeserializer
{
    protected InputStream in;

    protected Locator locator;

    protected AmfxInput amfxIn;

    /*
     *  DEBUG LOGGING
     */
    protected AmfTrace debugTrace;
    protected boolean isDebug;

    /**
     * Constructor.
     * Create a new AmfxMessageDeserializer object
     * <p/>
     */
    public AmfxMessageDeserializer()
    {
    }

    /**
     * Establishes the context for reading in data from the given InputStream.
     * A null value can be passed for the trace parameter if a record of the
     * AMFX data should not be made.
     * <p/>
     * @param context SerializationContext object
     * @param in InputStream to process
     * @param trace AmfTrace object
     */
    public void initialize(SerializationContext context, InputStream in, AmfTrace trace)
    {
        amfxIn = new AmfxInput(context);
        this.in = in;

        debugTrace = trace;
        isDebug = debugTrace != null;

        if (debugTrace != null)
            amfxIn.setDebugTrace(debugTrace);
    }

    /**
     * Set the SerializationContext.
     * <p/>
     * @param context the SerializationContext object
     */
    public void setSerializationContext(SerializationContext context)
    {
        amfxIn = new AmfxInput(context);
    }

    /**
     * Read message from the ActionMessage and ActionContext.
     * <p/>
     * @param m current ActionMessage
     * @param context current ActionContext
     * @throws IOException when the read message process failed
     */
    public void readMessage(ActionMessage m, ActionContext context) throws IOException
    {
        if (isDebug)
            debugTrace.startRequest("Deserializing AMFX/HTTP request");

        amfxIn.reset();
        amfxIn.setDebugTrace(debugTrace);
        amfxIn.setActionMessage(m);

        parse(m);

        context.setVersion(m.getVersion());
    }

    /**
     * Read Object.
     * @return Object the object read from AmfxInput
     * @throws ClassNotFoundException, IOException when exceptions occurs in reading the object
     */
    public Object readObject() throws ClassNotFoundException, IOException
    {
        return amfxIn.readObject();
    }

    protected void parse(ActionMessage m)
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, this);
        }
        catch (MessageException ex)
        {
            clientMessageEncodingException(m, ex);
        }
        catch (SAXParseException e)
        {
            if (e.getException() != null)
            {
                clientMessageEncodingException(m, e.getException());
            }
            else
            {
                clientMessageEncodingException(m, e);
            }
        }
        catch (Exception ex)
        {
            clientMessageEncodingException(m, ex);
        }
    }

    /**
     * Implement {@link org.xml.sax.EntityResolver#resolveEntity(String, String)}.
     * 
     * AMFX does not need or use external entities, so disallow external entities
     * to prevent external entity injection attacks. 
     * <p/>
     * @param publicId the public Id
     * @param systemId the system Id
     * @return InputSource the InputSource after entity resolution
     * @throws SAXException, IOException if the process failed
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        throw new MessageException("External entities are not allowed");
    }
     
    protected void clientMessageEncodingException(ActionMessage m, Throwable t)
    {
        MessageException me;
        if (t instanceof MessageException)
        {
            me = (MessageException)t;
        }
        else
        {
            me = new MessageException("Error occurred parsing AMFX: " + t.getMessage());
        }

        me.setCode("Client.Message.Encoding");
        throw me;
    }

    /**
     * Start process of an Element.
     * <p/>
     * @param uri the URI of the element
     * @param localName the local name of the element
     * @param qName the qualify name of the element
     * @param attributes the Attributes in the element
     * @throws SAXException if the process failed
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        try
        {
            String methodName = "start_" + localName;
            Method method = amfxIn.getClass().getMethod(methodName, attribArr);
            method.invoke(amfxIn, new Object[]{attributes});
        }
        catch (NoSuchMethodException e)
        {
            fatalError(new SAXParseException("Unknown type: " + qName, locator));
        }
        catch (IllegalAccessException e)
        {
            fatalError(new SAXParseException(e.getMessage(), locator, e));
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            if (t instanceof SAXException)
            {
                throw (SAXException)t;
            }
            else if (t instanceof Exception)
            {
                fatalError(new SAXParseException(t.getMessage(), locator, (Exception)t));
            }
            else
            {
                fatalError(new SAXParseException(e.getMessage(), locator, e));
            }
        }
    }

    /**
     * End process of an Element.
     * <p/>
     * @param uri the URI of the element
     * @param localName the local name of the element
     * @param qName the qualify name of the element
     * @throws SAXException if the process failed
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        try
        {
            String methodName = "end_" + localName;
            Method method = amfxIn.getClass().getMethod(methodName, new Class[]{});
            method.invoke(amfxIn, new Object[]{});
        }
        catch (NoSuchMethodException e)
        {
            fatalError(new SAXParseException("Unfinished type: " + qName, locator));
        }
        catch (IllegalAccessException e)
        {
            fatalError(new SAXParseException(e.getMessage(), locator, e));
        }
        catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            if (t instanceof SAXException)
            {
                throw (SAXException)t;
            }
            else if (t instanceof Error)
            {
                throw (Error)t;
            }
            else
            {
                fatalError(new SAXParseException(t.getMessage(), locator));
            }
        }
    }

    /**
     * Process a char array.
     * <p/>
     * @param ch the char array
     * @param start the start position in the char array
     * @param length the length of chars to process
     * @throws SAXException if the process failed
     */
    public void characters(char ch[], int start, int length) throws SAXException
    {
        String chars = new String(ch, start, length);
        if (chars.length() > 0)
        {
            amfxIn.text(chars);
        }
    }

    /**
     * Set the DocumentLocator object.
     * <p/>
     * @param l the DocumentLocator object
     */
    public void setDocumentLocator(Locator l)
    {
        locator = l;
    }


    /**
     * Process Error of a SAXParseException.
     * <p/>
     * @param exception SAXParseException
     * @throws SAXException rethrow the SAXException
     */
    public void error(SAXParseException exception) throws SAXException
    {
        throw new MessageException(exception.getMessage());
    }

    /**
     * Process FatalError of a SAXParseException.
     * <p/>
     * @param exception SAXParseException
     * @throws SAXException rethrow the SAXException
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
        if ((exception.getException() != null) && (exception.getException() instanceof MessageException))
            throw (MessageException)exception.getException();
        throw new MessageException(exception.getMessage());
    }

    /**
     * Process warning of a SAXParseException.
     * <p/>
     * @param exception SAXParseException
     * @throws SAXException rethrow the SAXException
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        throw new MessageException(exception.getMessage());
    }

    private static Class[] attribArr = new Class[]{Attributes.class};
}
