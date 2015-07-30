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
package flex.messaging.util;

import flex.messaging.io.TypeMarshaller;
import flex.messaging.io.TypeMarshallingContext;
import flex.messaging.MessageException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;

/**
 * A utility class used to find a suitable method based on matching
 * signatures to the types of set of arguments. Since the arguments
 * may be from more loosely typed environments such as ActionScript,
 * a translator can be employed to handle type conversion. Note that
 * there isn't a great guarantee for which method will be selected
 * when several overloaded methods match very closely through the use
 * of various combinations of generic types.
 *
 *
 */
public class MethodMatcher
{
    private final Map<MethodKey, Method> methodCache = new HashMap<MethodKey, Method>();
    private static final int ARGUMENT_CONVERSION_ERROR = 10006;
    private static final int CANNOT_INVOKE_METHOD = 10007;

    /**
     * Default constructor.
     */
    public MethodMatcher()
    {
    }

    /**
     * Utility method that searches a class for a given method, taking
     * into account the supplied parameters when evaluating overloaded
     * method signatures.
     *
     * @param c The class.
     * @param methodName Desired method to search for
     * @param parameters Required to distinguish between overloaded methods of the same name
     * @return The best-match <tt>Method</tt>.
     */
    public Method getMethod(Class c, String methodName, List parameters)
    {
        // Keep track of the best method match found
        Match bestMatch = new Match(methodName);

        // Determine supplied parameter types.
        Class[] suppliedParamTypes = paramTypes(parameters);

        // Create a key to search our method cache
        MethodKey methodKey = new MethodKey(c, methodName, suppliedParamTypes);

        Method method = null;
        if (methodCache.containsKey(methodKey))
        {
            method = methodCache.get(methodKey);

            String thisMethodName = method.getName();
            bestMatch.matchedMethodName = thisMethodName;
        }
        else
        {
            try // First, try an exact match.
            {
                method = c.getMethod(methodName, suppliedParamTypes);
                synchronized(methodCache)
                {
                    Method method2 = methodCache.get(methodKey);
                    if (method2 == null)
                        methodCache.put(methodKey, method);
                    else
                        method = method2;
                }
            }
            catch (SecurityException e)
            {
                // NOWARN
            }
            catch (NoSuchMethodException e)
            {
                // NOWARN
            }

            if (method == null) // Otherwise, search the long way.
            {
                Method[] methods = c.getMethods();
                for (Method thisMethod : c.getMethods())
                {
                    String thisMethodName = thisMethod.getName();

                    // FIXME: Do we want to do this case-insensitively in Flex 2.0?
                    // First, search by name; for backwards compatibility
                    // we continue to check case-insensitively
                    if (!thisMethodName.equalsIgnoreCase(methodName))
                        continue;

                    // Next, search on params
                    Match currentMatch = new Match(methodName);
                    currentMatch.matchedMethodName = thisMethodName;

                    // If we've not yet had a match, this is our best match so far.
                    if (bestMatch.matchedMethodName == null)
                        bestMatch = currentMatch;

                    // Number of parameters must match.
                    Class[] desiredParamTypes = thisMethod.getParameterTypes();
                    currentMatch.methodParamTypes = desiredParamTypes;

                    if (desiredParamTypes.length != suppliedParamTypes.length)
                        continue;

                    currentMatch.matchedByNumberOfParams = true;

                    // If we've not yet matched any params, this is our best match so far.
                    if (!bestMatch.matchedByNumberOfParams && bestMatch.matchedParamCount == 0)
                        bestMatch = currentMatch;

                    // Parameter types must also be compatible. Don't actually convert
                    // the parameter just yet, only count the matches and exact matches.
                    convertParams(parameters, desiredParamTypes, currentMatch, false);

                    // If we've not yet had this many params match, this is our best match so far.
                    if (currentMatch.matchedParamCount >= bestMatch.matchedParamCount
                            && currentMatch.exactMatchedParamCount >= bestMatch.exactMatchedParamCount)
                        bestMatch = currentMatch;

                    // If all types were compatible, we have a match.
                    if (currentMatch.matchedParamCount == desiredParamTypes.length
                            && bestMatch == currentMatch)
                    {
                        method = thisMethod;
                        synchronized(methodCache)
                        {
                            Method method2 = methodCache.get(methodKey);
                            if (method2 == null || method2 != method)
                                methodCache.put(methodKey, method);
                            else
                                method = method2;
                        }
                        // Don't break as there might be other methods with the
                        // same number of arguments but with better match count.
                        // break;
                    }
                }
            }
        }

        if (method == null)
        {
            methodNotFound(methodName, suppliedParamTypes, bestMatch);
        }
        else if (bestMatch.paramTypeConversionFailure != null)
        {
            //Error occurred while attempting to convert an input argument's type.
            MessageException me = new MessageException();
            me.setMessage(ARGUMENT_CONVERSION_ERROR);
            me.setCode("Server.Processing");
            me.setRootCause(bestMatch.paramTypeConversionFailure);
            throw me;
        }

        // Call convertParams one last time before returning method. This ensures
        // that parameters List is converted using bestMatch.
        Class<?>[] desiredParamTypes = method.getParameterTypes();
        bestMatch.methodParamTypes = desiredParamTypes;
        convertParams(parameters, desiredParamTypes, bestMatch, true);
        return method;
    }


    /**
     * Utility method to convert a collection of parameters to desired types. We keep track
     * of the progress of the conversion to allow callers to gauge the success of the conversion.
     * This is important for ranking overloaded-methods and debugging purposes.
     *
     * @param parameters actual parameters for an invocation
     * @param desiredParamTypes classes in the signature of a potential match for the invocation
     * @param currentMatch the currently best known match
     * @param convert determines whether the actual conversion should take place.
     */
    public static void convertParams(List parameters, Class[] desiredParamTypes, Match currentMatch, boolean convert)
    {
        int matchCount = 0;
        int exactMatchCount = 0;

        currentMatch.matchedParamCount = 0;
        currentMatch.convertedSuppliedTypes = new Class[desiredParamTypes.length];

        TypeMarshaller marshaller = TypeMarshallingContext.getTypeMarshaller();

        for (int i = 0; i < desiredParamTypes.length; i++)
        {
            Object param = parameters.get(i);

            // Consider null param to match
            if (param != null)
            {
                Object obj = null;
                Class objClass = null;

                if (marshaller != null)
                {
                    try
                    {
                        obj = marshaller.convert(param, desiredParamTypes[i]);
                    }
                    catch (MessageException ex)
                    {
                        currentMatch.paramTypeConversionFailure = ex;
                        break;
                    }
                    catch (ClassCastException ex)
                    {
                        currentMatch.paramTypeConversionFailure = ex;
                        break;
                    }
                    // We need to catch the exception here as the conversion of parameter types could fail
                    catch(Exception e)
                    {
                        currentMatch.paramTypeConversionFailure = e;
                        break;
                    }
                }
                else
                {
                    obj = param;
                }

                currentMatch.convertedSuppliedTypes[i] = (obj != null ? (objClass = obj.getClass()) : null);

                // Things match if we now have an object which is assignable from the
                // method param class type or if we have an Object which corresponds to
                // a primitive
                if (objClass != null && isAssignableFrom(desiredParamTypes[i], objClass))
                {
                    // See if there's an exact match before parameter is converted.
                    if (isAssignableFrom(desiredParamTypes[i], param.getClass()))
                        exactMatchCount++;

                    if (convert) // Convert the parameter.
                        parameters.set(i, obj);

                    matchCount++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                matchCount++;
            }
        }

        currentMatch.matchedParamCount = matchCount;
        currentMatch.exactMatchedParamCount = exactMatchCount;
    }

    private static boolean isAssignableFrom(Class first, Class second)
    {
        return (first.isAssignableFrom(second)) ||
            (first == Integer.TYPE && Integer.class.isAssignableFrom(second)) ||
            (first == Double.TYPE && Double.class.isAssignableFrom(second)) ||
            (first == Long.TYPE && Long.class.isAssignableFrom(second)) ||
            (first == Boolean.TYPE && Boolean.class.isAssignableFrom(second)) ||
            (first == Character.TYPE && Character.class.isAssignableFrom(second)) ||
            (first == Float.TYPE && Float.class.isAssignableFrom(second)) ||
            (first == Short.TYPE && Short.class.isAssignableFrom(second)) ||
            (first == Byte.TYPE && Byte.class.isAssignableFrom(second));
    }

    /**
     * Utility method that iterates over a collection of input
     * parameters to determine their types while logging
     * the class names to create a unique identifier for a
     * method signature.
     *
     * @param parameters - A list of supplied parameters.
     * @return An array of <tt>Class</tt> instances indicating the class of each corresponding parameter.
     *
     */
    public static Class[] paramTypes(List parameters)
    {
        Class[] paramTypes = new Class[parameters.size()];
        for (int i = 0; i < paramTypes.length; i++)
        {
            Object p = parameters.get(i);
            paramTypes[i] = p == null ? Object.class : p.getClass();
        }
        return paramTypes;
    }

    /**
     * Utility method to provide more detailed information in the event that a search
     * for a specific method failed for the service class.
     *
     * @param methodName         the name of the missing method
     * @param suppliedParamTypes the types of parameters supplied for the search
     * @param bestMatch          the best match found during the search
     */
    public static void methodNotFound(String methodName, Class[] suppliedParamTypes, Match bestMatch)
    {
        // Set default error message...
        // Cannot invoke method '{methodName}'.
        int errorCode = CANNOT_INVOKE_METHOD;
        Object[] errorParams = new Object[]{methodName};
        String errorDetailVariant = "0";
        // Method '{methodName}' not found.
        Object[] errorDetailParams = new Object[]{methodName};

        if (bestMatch.matchedMethodName != null)
        {
            // Cannot invoke method '{bestMatch.matchedMethodName}'.
            errorCode = CANNOT_INVOKE_METHOD;
            errorParams = new Object[]{bestMatch.matchedMethodName};

            int suppliedParamCount = suppliedParamTypes.length;
            int expectedParamCount = bestMatch.methodParamTypes != null ? bestMatch.methodParamTypes.length : 0;

            if (suppliedParamCount != expectedParamCount)
            {
                // {suppliedParamCount} arguments were sent but {expectedParamCount} were expected.
                errorDetailVariant = "1";
                errorDetailParams = new Object[]{new Integer(suppliedParamCount), new Integer(expectedParamCount)};

            }
            else
            {
                String suppliedTypes = bestMatch.listTypes(suppliedParamTypes);
                String convertedTypes = bestMatch.listConvertedTypes();
                String expectedTypes = bestMatch.listExpectedTypes();

                if (expectedTypes != null)
                {
                    if (suppliedTypes != null)
                    {
                        if (convertedTypes != null)
                        {
                            // The expected argument types are ({expectedTypes})
                            // but the supplied types were ({suppliedTypes})
                            // and converted to ({convertedTypes}).
                            errorDetailVariant = "2";
                            errorDetailParams = new Object[]{expectedTypes, suppliedTypes, convertedTypes};
                        }
                        else
                        {
                            // The expected argument types are ({expectedTypes})
                            // but the supplied types were ({suppliedTypes})
                            // with none successfully converted.
                            errorDetailVariant = "3";
                            errorDetailParams = new Object[]{expectedTypes, suppliedTypes};
                        }
                    }
                    else
                    {
                        // The expected argument types are ({expectedTypes})
                        // but no arguments were provided.
                        errorDetailVariant = "4";
                        errorDetailParams = new Object[]{expectedTypes};
                    }
                }
                else
                {
                    // No arguments were expected but the following types were supplied (suppliedTypes)
                    errorDetailVariant = "5";
                    errorDetailParams = new Object[]{suppliedTypes};
                }
            }
        }

        MessageException ex = new MessageException();
        ex.setMessage(errorCode, errorParams);
        ex.setCode(MessageException.CODE_SERVER_RESOURCE_UNAVAILABLE);
        if (errorDetailVariant != null)
            ex.setDetails(errorCode, errorDetailVariant, errorDetailParams);

        if (bestMatch.paramTypeConversionFailure != null)
            ex.setRootCause(bestMatch.paramTypeConversionFailure);

        throw ex;
    }

    /**
     * A utility class to help rank methods in the search
     * for a best match, given a name and collection of
     * input parameters.
     *
     */
    public static class Match
    {
        /**
         * Constructor.
         * @param name the name of the method to match
         */
        public Match(String name)
        {
            this.methodName = name;
        }

        /**
         * Returns true if desired and found method names match.
         * @return true if desired and found method names match
         */
        public boolean matchedExactlyByName()
        {
            return matchedMethodName != null? matchedMethodName.equals(methodName) : false;
        }

        /**
         * Returns true if desired and found method names match only when case is ignored.
         * @return true if desired and found method names match only when case is ignored
         */
        public boolean matchedLooselyByName()
        {
            return matchedMethodName != null?
                    (!matchedExactlyByName() && matchedMethodName.equalsIgnoreCase(methodName)) : false;
        }


        /**
         * Lists the classes in the signature of the method matched.
         * @return the classes in the signature of the method matched
         */
        public String listExpectedTypes()
        {
            return listTypes(methodParamTypes);
        }


        /**
         * Lists the classes corresponding to actual invocation parameters once they have been
         * converted as best they could to match the classes in the invoked method's signature.
         *
         * @return the classes corresponding to actual invocation parameters once they have been
         * converted as best they could to match the classes in the invoked method's signature
         */
        public String listConvertedTypes()
        {
            return listTypes(convertedSuppliedTypes);
        }

        /**
         * Creates a string representation of the class names in the array of types passed into
         * this method.
         *
         * @param types an array of types whose names are to be listed
         * @return a string representation of the class names in the array of types
         */
        public String listTypes(Class[] types)
        {
            if (types == null || types.length == 0)
                return null;

            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < types.length; i++)
            {
                if (i > 0)
                    sb.append(", ");

                Class c = types[i];

                if (c != null)
                {
                    if (c.isArray())
                    {
                        c = c.getComponentType();
                        sb.append(c.getName()).append("[]");
                    }
                    else
                    {
                        sb.append(c.getName());
                    }
                }
                else
                {
                    sb.append("null");
                }
            }

            return sb.toString();
        }

        final String methodName;
        String matchedMethodName;

        boolean matchedByNumberOfParams;
        int matchedParamCount;
        int exactMatchedParamCount;
        Class[] methodParamTypes;
        Class[] convertedSuppliedTypes;
        Exception paramTypeConversionFailure;
    }
}
