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
package flex.messaging;

import java.util.Locale;

import flex.messaging.util.PropertyStringResourceLoader;
import flex.messaging.util.ResourceLoader;

/**
 * The LocalizedException class is the base class for server
 * exceptions that use localized error message and details strings. This class overloads
 * <code>setMessage</code> and <code>setDetails</code> to support passing an error
 * number and an optional message variant that is used to look up a localized error
 * message or details string using a <code>ResourceLoader</code>. These methods also
 * set the number property of the exception instance.
 * <p>
 * The various overloads optionally support specifying a target locale as well as
 * arguments to substitute into the localized string if it is parameterized.
 * </p><p>
 * Localized error message and details strings are stored in the flex.messaging.errors
 * resource bundle. Entries must have the following format.
 * <ul>
 *  <li>Error message: {number}[-{variant}]={message}</li>
 *  <li>Error details: {number}[-{variant}]-details={details}</li>
 * </ul>
 * </p>
 *
 * @see ResourceLoader
 */
public class LocalizedException extends RuntimeException
{
    /** @exclude - transient, the resourceLoader for localized strings doesn't need to serialize.  */
    protected transient ResourceLoader resourceLoader;
    /** @exclude */
    protected int number;
    /** @exclude */
    protected String message;
    /** @exclude */
    protected String details;
    /** @exclude */
    protected Throwable rootCause;

    /**
     * This number was generated using the 'serialver' command line tool.
     * This number should remain consistent with the version used by
     * ColdFusion to communicate with the message broker over RMI.
     */
    private static final long serialVersionUID = 7980539484335065853L;

    /**
     * Create a LocalizedException with the default ResourceLoader.
     */
    public LocalizedException()
    {
        super();
    }

    /**
     * Create a LocalizedException that will use the specified
     * ResourceLoader.
     *
     * @param resourceLoader The resource loader to use.
     */
    public LocalizedException(ResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Returns the exception details.
     *
     * @return The exception details.
     */
    public String getDetails()
    {
        return details;
    }

    /**
     * Sets the exception details.
     *
     * @param details The exception details.
     */
    public void setDetails(String details)
    {
        this.details = details;
    }

    /**
     * Sets the message property to a localized string based on error number.
     *
     * @param number The error number for this exception instance.
     */
    public void setMessage(int number)
    {
        setMessage(number, null, null, null);
    }

    /**
     * Sets the message property to a localized string based on error number and target locale.
     *
     * @param number The error number for this exception instance.
     * @param locale The target locale for error message lookup.
     */
    public void setMessage(int number, Locale locale)
    {
        setMessage(number, null, locale, null);
    }

    /**
     * Sets the message property to a localized string based on error number.
     * The passed arguments are substituted into the parameterized error message string.
     *
     * @param number The error number for this exception instance.
     * @param arguments The arguments to substitute into the error message.
     */
    public void setMessage(int number, Object[] arguments)
    {
        setMessage(number, null, null, arguments);
    }

    /**
     * Sets the message property to a localized string based on error number and variant.
     *
     * @param number The error number for this exception instance.
     * @param variant The variant of the error message for this instance.
     */
    public void setMessage(int number, String variant)
    {
        setMessage(number, variant, null, null);
    }

    /**
     * Sets the message property to a localized string based on error number, variant
     * and target locale.
     *
     * @param number The error number for this exception instance.
     * @param variant The variant of the error message for this instance.
     * @param locale The target locale for error message lookup.
     */
    public void setMessage(int number, String variant, Locale locale)
    {
        setMessage(number, variant, locale, null);
    }

    /**
     * Sets the message property to a localized string based on error number and variant.
     * The passed arguments are substituted into the parameterized error message string.
     *
     * @param number The error number for this exception instance.
     * @param variant The varient of the error message for this instance.
     * @param arguments The arguments to substitute into the error message.
     */
    public void setMessage(int number, String variant, Object[] arguments)
    {
        setMessage(number, variant, null, arguments);
    }

    /**
     * Sets the message property to a localized string based on error number, variant and
     * target locale. The passed arguments are substituted into the parameterized error
     * message string.
     *
     * @param number The error number for this exception instance.
     * @param variant The variant of the error message for this instance.
     * @param locale The target locale for error message lookup.
     * @param arguments The arguments to substitute into the error message.
     */
    public void setMessage(int number, String variant, Locale locale, Object[] arguments)
    {
        setNumber(number);
        ResourceLoader resources = getResourceLoader();
        setMessage(resources.getString(generateFullKey(number, variant), locale, arguments));
    }

    /**
     * Returns the exception message.
     *
     * @return The exception message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Sets the exception message.
     *
     * @param message The exception message.
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * Sets the localized exception number.
     *
     * @param number The localized exception number.
     */
    public void setNumber(int number)
    {
        this.number = number;
    }

    /**
     * Returns the localized exception number.
     *
     * @return The localized exception number.
     */
    public int getNumber()
    {
        return number;
    }

    /**
     * Sets the details property to a localized string based on error number.
     *
     * @param number The error number to lookup details for.
     */
    public void setDetails(int number)
    {
        setDetails(number, null, null, null);
    }

    /**
     * Sets the details property to a localized string based on error number and variant.
     *
     * @param number The error number to lookup details for.
     * @param variant The variant of the details string to lookup.
     */
    public void setDetails(int number, String variant)
    {
        setDetails(number, variant, null, null);
    }

    /**
     * Sets the details property to a localized string based on error number, variant
     * and target locale.
     *
     * @param number The error number to lookup details for.
     * @param variant The variant of the details string to lookup.
     * @param locale The target locale for the lookup.
     */
    public void setDetails(int number, String variant, Locale locale)
    {
        setDetails(number, variant, locale, null);
    }

    /**
     * Sets the details property to a localized string based on error number and variant.
     * The passed arguments are substituted into the parameterized error details string.
     *
     * @param number The error number to lookup details for.
     * @param variant The variant of the details string to lookup.
     * @param arguments The arguments to substitute into the details string.
     */
    public void setDetails(int number, String variant, Object[] arguments)
    {
        setDetails(number, variant, null, arguments);
    }


    /**
     * Sets the details property to a localized string based on error number, variant,
     * and target locale. The passed arguments are substituted into the parameterized error
     * details string.
     *
     * @param number The error number to lookup a localized details string for.
     * @param variant The variant of the details string to lookup.
     * @param locale The target locale for the lookup.
     * @param arguments The arguments to substitute into the details string.
     */
    public void setDetails(int number, String variant, Locale locale, Object[] arguments)
    {
        setNumber(number);
        ResourceLoader resources = getResourceLoader();
        setDetails(resources.getString(generateDetailsKey(number, variant), locale, arguments));
    }

    /**
     * Returns the root cause for this exception.
     *
     * @return The root cause for this exception.
     */
    public Throwable getRootCause()
    {
        return rootCause;
    }

    /**
     * Sets the root cause for this exception.
     *
     * @param cause The root cause for this exception.
     */
    public void setRootCause(Throwable cause)
    {        
        rootCause = cause;
        // Assign through to the base cause property to include it in general stack traces.
        initCause(cause);
    }

    /**
     * Returns the <code>ResourceLoader</code> used to load localized strings.
     *
     * @return The <code>ResourceLoader</code> used to load localized strings.
     */
    protected ResourceLoader getResourceLoader()
    {
        if (resourceLoader == null)
            resourceLoader = new PropertyStringResourceLoader();

        return resourceLoader;
    }

    /**
     * Generates the full key to lookup a localized error message based on an error number
     * and an optional variant followed by a "-details" suffix. If the variant is null, the
     * lookup key is the error number.
     *
     * @param number The error number.
     * @param variant The variant of the error message.
     * @return The full lookup key for a localized error message.
     */
    private String generateFullKey(int number, String variant)
    {
        return (variant != null) ? (number + "-" + variant) : String.valueOf(number);
    }

    /**
     * Generates the full key to lookup a localized details string based on an error number
     * and an optional variant followed by a "-details" suffix. If the variant is null, the
     * lookup key is the error number followed by a "-details" suffix.
     *
     * @param number The error number.
     * @param variant The variant of the details message.
     * @return The full lookup key for a localized details message.
     */
    private String generateDetailsKey(int number, String variant)
    {
        return (generateFullKey(number, variant) + "-details");
    }

    /**
     * Returns a string represenation of the exception.
     *
     * @return A string representation of the exception.
     */
    public String toString()
    {
        String result = super.toString();
        if (details != null)
        {
            StringBuffer buffer = new StringBuffer(result);
            if (!result.endsWith("."))
            {
                buffer.append(".");
            }
            buffer.append(' ').append(details);
            result = buffer.toString();
        }
        return result;
    }
}
