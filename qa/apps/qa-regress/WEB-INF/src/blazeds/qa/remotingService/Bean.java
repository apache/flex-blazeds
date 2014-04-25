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

package blazeds.qa.remotingService;

public class Bean
{
    public Bean()
    {
        // these sets are merely to avoid warnings.  Values are hardcoded.
        setPrivateProperty(getPrivateProperty());
        setPrivateStaticProperty(getPrivateStaticProperty());
    }

    // Instance Properties

    // Read Only
    public String getPublicReadOnlyProperty()
    {
        return _publicReadOnlyProperty;
    }

    // Write Only
    public void setPublicWriteOnlyProperty(String value)
    {
        _publicWriteOnlyProperty = value;
    }

    // Read-Write
    public String getPublicProperty()
    {
        return _publicProperty;
    }

    public void setPublicProperty(String value)
    {
        _publicProperty = value;
    }

    private String getPrivateProperty()
    {
        return _privateProperty;
    }

    private void setPrivateProperty(String value)
    {
        _privateProperty = value;
    }

    protected String getProtectedProperty()
    {
        return _protectedProperty;
    }

    protected void setprotectedProperty(String value)
    {
        _protectedProperty = value;
    }

    // Static Properties

    // Read Only
    public String getPublicStaticReadOnlyProperty()
    {
        return _publicStaticReadOnlyProperty;
    }

    // Write Only
    public void setPublicStaticWriteOnlyProperty(String value)
    {
        _publicStaticWriteOnlyProperty = value;
    }

    // Read-Write
    public static String getPublicStaticProperty()
    {
        return _publicStaticProperty;
    }

    public static void setPublicStaticProperty(String value)
    {
        _publicStaticProperty = value;
    }

    private static String getPrivateStaticProperty()
    {
        return _privateStaticProperty;
    }

    private static void setPrivateStaticProperty(String value)
    {
        _privateStaticProperty = value;
    }

    protected static String getProtectedStaticProperty()
    {
        return _protectedStaticProperty;
    }

    protected static void setprotectedStaticProperty(String value)
    {
        _protectedStaticProperty = value;
    }

    public String toString() 
    {
	     return "Bean:  publicField = " + publicField + "\n" +
	     "privateField = " + privateField + "\n" + 
	     "protectedField = " + protectedField + "\n" + 
	     "transientField = " + transientField + "\n" + 
	     "publicStaticField = " + publicStaticField + "\n" + 
	     "privateStaticField = " + privateStaticField + "\n" + 
	     "protectedStaticField = " + protectedStaticField + "\n" + 
	     "transientStaticField = " + transientStaticField + "\n" + 
	     "publicStaticConstField = " + publicStaticConstField + "\n" + 
	     "privateStaticConstField = " + privateStaticConstField + "\n" + 
	     "protectedStaticConstField = " + protectedStaticConstField + "\n" + 
	     "transientStaticConstField = " + transientStaticConstField + "\n" + 
	     "publicConstField = " + publicConstField + "\n" + 
	     "privateConstField = " + privateConstField + "\n" + 
	     "protectedConstField = " + protectedConstField + "\n" + 
	     "transientConstField = " + transientConstField + "\n" + 
	     "_publicProperty = " + _publicProperty + "\n" + 
	     "_privateProperty = " + getPrivateProperty() + "\n" + 
	     "_protectedProperty = " + _protectedProperty + "\n" + 
	     "_publicStaticProperty = " + _publicStaticProperty + "\n" + 
	     "_privateStaticProperty = " + getPrivateStaticProperty() + "\n" + 
	     "_protectedStaticProperty = " + _protectedStaticProperty + "\n" + 
	     "_publicReadOnlyProperty = " + _publicReadOnlyProperty + "\n" + 
	     "_publicWriteOnlyProperty = " + _publicWriteOnlyProperty + "\n" + 
	     "_publicStaticReadOnlyProperty = " + _publicStaticReadOnlyProperty + "\n" + 
	     "_publicStaticWriteOnlyProperty = " + _publicStaticWriteOnlyProperty + "\n";	    	
    }
    
    // ---------------------------------------------------------------
    //
    // Variables (Fields)
    //
    // ---------------------------------------------------------------

    // Instance Fields
    public String publicField = "publicFieldServerValue";
    private String privateField = "privateFieldServerValue";
    protected String protectedField = "protectedFieldServerValue";
    transient String transientField = "transientFieldServerValue";

    // Static Fields
    public static String publicStaticField = "publicStaticFieldServerValue";
    private static String privateStaticField = "privateStaticFieldServerValue";
    protected static String protectedStaticField = "protectedStaticFieldServerValue";
    transient static String transientStaticField = "transientStaticFieldServerValue";

    // Static Const Fields
    public static final String publicStaticConstField = "publicStaticConstFieldServerValue";
    private static final String privateStaticConstField = "privateStaticConstFieldServerValue";
    protected static final String protectedStaticConstField = "protectedStaticConstFieldServerValue";
    transient static final String transientStaticConstField = "transientStaticConstFieldServerValue";

    // Instance Const Fields
    public final String publicConstField = "publicConstFieldServerValue";
    private final String privateConstField = "privateConstFieldServerValue";
    protected final String protectedConstField = "protectedConstFieldServerValue";
    transient final String transientConstField = "transientConstFieldServerValue";




    // Private property backing fields

    private String _publicProperty = "publicPropertyServerValue";
    private String _privateProperty = "privatePropertyServerValue";
    private String _protectedProperty = "protectedPropertyServerValue";

    private static String _publicStaticProperty = "publicStaticPropertyServerValue";
    private static String _privateStaticProperty = "privateStaticPropertyServerValue";
    private static String _protectedStaticProperty = "protectedStaticPropertyServerValue";

    private String _publicReadOnlyProperty = "publicReadOnlyPropertyServerValue";

    private String _publicWriteOnlyProperty = "publicWriteOnlyPropertyServerValue";

    private static String _publicStaticReadOnlyProperty = "publicStaticReadOnlyPropertyServerValue";

    private static String _publicStaticWriteOnlyProperty = "publicStaticWriteOnlyPropertyServerValue";
}
