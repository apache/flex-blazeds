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

import flex.messaging.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Map;
import javax.servlet.http.Cookie;

import blazeds.qa.remotingService.BindObject;

public class TestServices implements FlexSessionAttributeListener, FlexSessionListener{
    private String _keepit;
    public int testArgsOrder(int arg1,int arg2,int arg3,int arg4,int arg5,int arg6,int whichone) {

        switch (whichone) {
            case 1:
                return arg1;
            case 2:
                return arg2;
            case 3:
                return arg3;
            case 4:
                return arg4;
            case 5:
                return arg5;
            case 6:
                return arg6;
            default:
                return Integer.MIN_VALUE;

        }
    }

    public void testMixNumbers(int arg1,short arg2,float arg3,double arg4,Integer arg5)  {

    }

    public void testMixTypes(int arg1, boolean arg3, Boolean arg4, String arg5) {

    }

    public void keepIt(String it) {
        _keepit = it;
    }

    public String keepWhat() {
        return _keepit;
    }

    public String getUserSessionID()  {
       return FlexContext.getHttpRequest().getSession(true).getId();
    }
    public String getGatewayConfigFileName() {
        return FlexContext.getServletConfig().getInitParameter("gateway.configuration.file");
    }
    public void setCookie(String name, String value) {
        FlexContext.getHttpResponse().addCookie(new Cookie(name,value));
    }
    public void setCookies(HashMap map) {
        Iterator allKeys = map.keySet().iterator();
        String key=null;
        while (allKeys.hasNext()) {
            key = (String) allKeys.next();
            System.out.println(key + "," +map.get(key) );
            setCookie(key,(String) map.get(key));
        }
    }
    public HashMap getCookiesAsMap() {
        Cookie[] cookies = FlexContext.getHttpRequest().getCookies();
        HashMap map = new HashMap(10,0.7F);
        for (int i=0; i < cookies.length ;i++) {
            map.put(cookies[i].getName(),cookies[i].getValue());
        }
        return map;
    }
    public Cookie[] getCookies() {
        return FlexContext.getHttpRequest().getCookies();
    }
    public String getUserPrincipal() {
        return FlexContext.getHttpRequest().getUserPrincipal().toString();
    }
    public boolean isUserInRole(String role) {
        return FlexContext.getHttpRequest().isUserInRole(role);
    }

    public String slowEchoString(String s, long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        return s;
    }

    public void throwException(String value) throws Exception {
        throw new Exception(value);
    }

    public void throwSlowException(String value, long ms) throws Exception {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        throw new Exception(value);
    }


    public void setAttribute(String key,Object value) {
        FlexSession fsession = FlexContext.getFlexSession();
        fsession.setAttribute(key,value);
    }

    public void setAttributeBind(String key) {
        FlexSession fsession = FlexContext.getFlexSession();
        fsession.setAttribute(key, new BindObject());
    }

    public void removeAttribute(String key) {
        FlexContext.getFlexSession().removeAttribute(key);
    }

    public Object getAttribute(String key) {
        return FlexContext.getFlexSession().getAttribute(key);
    }

    public void monitorSessionChange() {
        FlexContext.getFlexSession().addSessionAttributeListener(this);
        FlexContext.getFlexSession().addSessionDestroyedListener(this);
    }

    public void stopMonitorSessionChange() {
        FlexContext.getFlexSession().removeSessionAttributeListener(this);
        FlexContext.getFlexSession().removeSessionDestroyedListener(this);
    }

    public void attributeAdded(FlexSessionBindingEvent event) {
        System.out.println("<attributeAdded>" + event.getName() + ":" + event.getValue());
    }

    public void attributeRemoved(FlexSessionBindingEvent event) {
        System.out.println("<attributeRemoved>" + event.getName() + ":" + event.getValue());
    }

    public void attributeReplaced(FlexSessionBindingEvent event) {
        System.out.println("<attributeReplaced>" + event.getName() + ":" + event.getValue());
    }

    public void sessionDestroyed(FlexSession session) {
        System.out.println(Calendar.getInstance().toString() + session.toString()+" : Session Destroyed");
    }

    public void sessionCreated(FlexSession session) {
        System.out.println(Calendar.getInstance().toString() + session.toString()+" : Session Created");
    }

    public Object getUserPrincipalFromFlexSession() {
        return FlexContext.getFlexSession().getUserPrincipal();
    }

    public void invalidateSession() {
        FlexContext.getFlexSession().invalidate();
    }
    
    public void invalidateSessionTwice() {
        FlexContext.getFlexSession().invalidate();
        FlexContext.getFlexSession().invalidate();
    }

    enum ParamType {booleantype, inttype, stringtype, booleanintstringtypes, intbooleanstringtypes, stringbooleaninttypes, bookbeaninttypes, integerx3types, floatx3types, mapx3types}

    public ParamType methodMatch(boolean b)
    {
        return ParamType.booleantype;
    }
    public ParamType methodMatch(int i)
    {
        return ParamType.inttype;
    }
    public ParamType methodMatch(String s)
    {
        return ParamType.stringtype;
    }

    public ParamType methodMatch(boolean b, int i, String s)
    {
        return ParamType.booleanintstringtypes;
    }

    public ParamType methodMatch(int i, boolean b, String s)
    {
        return ParamType.intbooleanstringtypes;
    }

    public ParamType methodMatch( String s, boolean b, int i)
    {
        return ParamType.stringbooleaninttypes;
    }

    public ParamType methodMatch( Book book, dev.echoservice.Bean b, int i)
    {
        return ParamType.bookbeaninttypes;
    }

    public ParamType methodMatch(Integer i1, Integer i2, Integer i3)
    {
        return ParamType.integerx3types;
    }

    public ParamType methodMatch(Map m1, Map m2, Map m3)
    {
        return ParamType.mapx3types;
    }
    public String testBlz462(Map obj)
    {
        return obj.getClass().getName();
    }
    public String testBlz462(Book obj)
    {
        return obj.getClass().getName();
    }
}
