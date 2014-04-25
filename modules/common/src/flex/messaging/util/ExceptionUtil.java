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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @exclude
 */
public class ExceptionUtil
{
    /**
     * List of no-arg methods that are known to return a wrapped throwable.
     **/
    public static String[] unwrapMethods = { "getRootCause", "getTargetException",
                                             "getTargetError", "getException",
                                             "getCausedByException", "getLinkedException" };

    /**
     * Get the wrapped Exception object from the Throwable object.
     * @param t the Throwable object
     * @return Throwable the wrapped exception object if any
     */
    public static Throwable wrappedException(Throwable t)
    {
        // Handle these statically since they are core to Java
        return (t instanceof InvocationTargetException)? 
                ((InvocationTargetException)t).getTargetException() : getRootCauseWithReflection(t);
    }

    /**
     * Get to the base exception (if any).
     * @param t the Throwable object
     * @return the base Exception object
     */
    public static Throwable baseException(Throwable t) 
    {
        Throwable wrapped = wrappedException(t);
        return wrapped != null? baseException(wrapped) : t;
    }

    /**
     * Return the stack trace in a String.
     * @param t the Throwable object
     * @return String the String presentation of the Throwable object
     */
    public static String toString(Throwable t) 
    {
        StringWriter strWrt = new StringWriter();
        t.printStackTrace(new PrintWriter(strWrt));

        return strWrt.toString();
    }

    /**
     * Return the stack trace up to the first line that starts with prefix.
     *
     * <p>Example: ExceptionUtil.getStackTraceUpTo(exception, "jrunx.");</p>
     * @param t the Throwable object
     * @param prefix the prefix message that we are looking for
     * @return String the String of stack trace lines till the prefix message is located
     */
    public static String getStackTraceUpTo(Throwable t, String prefix) 
    {
        StringTokenizer tokens = new StringTokenizer(toString(t), "\n\r");
        StringBuffer trace = new StringBuffer();
        boolean done = false;

        String lookingFor = "at " + prefix;
        while (!done && tokens.hasMoreElements())
        {
            String token = tokens.nextToken();
            if (token.indexOf(lookingFor) == -1)
                trace.append(token);
            else
                done = true;
            trace.append(StringUtils.NEWLINE);
        }

        return trace.toString();
    }

    /**
     * return the top n lines of this stack trace.
     *
     * <p>Example: ExceptionUtil.getStackTraceLines(exception, 10);</p>
     * @param t the Throwable object
     * @param numLines number of lines we should trace down
     * @return String the String of stack trace lines
     */
    public static String getStackTraceLines(Throwable t, int numLines) 
    {
        StringTokenizer tokens = new StringTokenizer(toString(t), "\n\r");

        StringBuffer trace = new StringBuffer();

        for (int i=0; i<numLines; i++)
        {
            String token = tokens.nextToken();
            trace.append(token);
            trace.append(StringUtils.NEWLINE);
        }

        return trace.toString();
    }

    /**
     * Return the "nth" method call from the stack trace of "t", where 0 is
     * the top.
     * @param t the Throwable object
     * @param nth the line number of the message should we skip
     * @return String the callAt String
     */
    public static String getCallAt(Throwable t, int nth) 
    {
        StringTokenizer tokens = new StringTokenizer(toString(t), "\n\r");
        try 
        {
            // Skip the first line - the exception message
            for(int i = 0; i <= nth; ++i)
                tokens.nextToken();

            // get the method name from the next token
            String token = tokens.nextToken();
            int index1 = token.indexOf(' ');
            int index2 = token.indexOf('(');
            StringBuffer call = new StringBuffer();
            call.append(token.substring(index1 < 0 ? 0 : index1 + 1, index2 < 0 ? call.length() : index2));

            int index3 = token.indexOf(':', index2 < 0 ? 0 : index2);
            if(index3 >= 0) 
            {
                int index4 = token.indexOf(')', index3);
                call.append(token.substring(index3, index4 < 0 ? token.length() : index4));
            }
            return call.toString();
        }
        catch(NoSuchElementException e) {}

        return "unknown";
    }


    /**
     * Utility method for converting an exception into a string. This
     * method unwinds all wrapped exceptions
     * @param t The throwable exception
     * @return The printable exception
     */
    public static String exceptionToString(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);

        //print out the exception stack.
        printExceptionStack(t, out, 0);
        return sw.toString();
    }
    
    /**
     * Utility method for converting an exception and all chained root causes into a
     * string. Unlike <code>exceptionToString(Throwable)</code> which prints the chain
     * from most nested root cause down to the top-level exception, this method prints 
     * from the top-level exception down to the most nested root cause.
     * 
     * @param t The throwable exception.
     * @return The printable exception.
     */
    public static String exceptionFollowedByRootCausesToString(Throwable t)
    {
        StringBuffer output = new StringBuffer();
        Throwable root = t;
        while (root != null)
        {
            output.append((root == t) ? ((root instanceof Exception) ? "  Exception: " : "  Error: ") : "  Root cause: ");
            output.append(ExceptionUtil.toString(root));
            // Do not recurse if the root cause has already been printed; this will have happened if the root cause has
            // been assigned to the current Throwable via initCause() or as a constructor argument.
            Throwable cause = root.getCause();
            root = ExceptionUtil.wrappedException(root);
            if (cause == root)
                break;
        }
        return output.toString();
    }

    /**
     * Recursively prints out a stack of wrapped exceptions.
     */
    protected static void printExceptionStack(Throwable th, PrintWriter out, int depth){
        //only print the stack depth if the depth is greater than 0
        boolean printStackDepth = depth>0;

        Throwable wrappedException = ExceptionUtil.wrappedException(th);
        if (wrappedException != null)
        {
            printStackDepth = true;
            printExceptionStack(wrappedException, out, depth + 1);
        }

        if(printStackDepth){
            out.write("[" + depth + "]");
        }

        th.printStackTrace(out);
    }

    private static Throwable getRootCauseWithReflection(Throwable t)
    {
        for(int i = 0; i < unwrapMethods.length; i++)
        {
            Method m = null;

            try
            {
                m = t.getClass().getMethod(unwrapMethods[i], (Class[])null);
                return (Throwable) m.invoke(t, (Object[])null);
            }
            catch(Exception nsme)
            {
                // ignore
            }
        }

        return null;
    }
}
