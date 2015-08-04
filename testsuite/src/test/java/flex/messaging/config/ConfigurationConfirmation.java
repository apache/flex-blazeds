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

package flex.messaging.config;

import flex.messaging.LocalizedException;

import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class ConfigurationConfirmation
{
    private IdentityHashMap knownObjects;

    public ConfigurationConfirmation()
    {
    }

    private void init()
    {
        knownObjects = new IdentityHashMap(64);
    }

    public abstract MessagingConfiguration getExpectedConfiguration();

    public abstract LocalizedException getExpectedException();

    public boolean isNegativeTest()
    {
        return false;
    }

    public boolean matches(MessagingConfiguration config)
    {
        init();

        try
        {
            boolean match = configMatch(config, getExpectedConfiguration());
            return match;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public boolean negativeMatches(LocalizedException ex)
    {

        init();

        String message1 = ex.getMessage();
        String message2 = getExpectedException().getMessage();

        if (message1 == null && message2 == null)
            return true;

        if ((message1 == null && message2 != null) || (message1 != null && message2 == null))
            return false;

        return message1.equalsIgnoreCase(message2);
    }


    public boolean configMatch(MessagingConfiguration c1, MessagingConfiguration c2)
    {
        if (c1 == null && c2 == null)
            return true;

        if (c1 == null || c2 == null)
            return fail("Services sections didn't match: {0} != {1}", new Object[] {c1, c2});

        // Services
        List s1 = c1.getAllServiceSettings();
        List s2 = c2.getAllServiceSettings();
        if (!servicesMatch(s1, s2))
            return false;

        // Security
        SecuritySettings sec1 = c1.getSecuritySettings();
        SecuritySettings sec2 = c2.getSecuritySettings();
        if (!securityMatch(sec1, sec2))
            return false;
        return true;
    }

    private boolean securityMatch(SecuritySettings sec1, SecuritySettings sec2)
    {
        Collection<SecurityConstraint> constraintSet1 = sec1.getConstraints();
        for (SecurityConstraint sc1 : constraintSet1)
        {
            SecurityConstraint sc2 = sec2.getConstraint(sc1.getId());
            if (!securityConstraintsMatch(sc1, sc2))
                return false;
        }
        return true;
    }


    protected boolean servicesMatch(List s1, List s2)
    {
        if (s1 == null && s2 == null)
            return true;

        if (s1 == null || s2 == null)
            return fail("Services sections didn't match: {0} != {1}", new Object[] {s1, s2});

        if (s1.size() != s2.size())
            return fail("Services sections didn't match by size: {0} != {1}", new Object[] {new Integer(s1.size()), new Integer(s2.size())});

        Iterator it = s1.iterator();
        while (it.hasNext())
        {
            ServiceSettings ss1 =(ServiceSettings) it.next();
            String key = ss1.getId();

            ServiceSettings ss2 = null;
            for (Iterator iter = s2.iterator(); iter.hasNext();)
            {
                ServiceSettings element = (ServiceSettings) iter.next();
                if (element.getId().equals(key))
                {
                    ss2 = element;
                    break;
                }
            }

            if (!serviceSettingsMatch(ss1, ss2))
                return false;
        }

        return true;
    }

    protected boolean serviceSettingsMatch(ServiceSettings ss1, ServiceSettings ss2)
    {
        if (ss1 == null && ss2 == null)
            return true;

        if (ss1 == null || ss2 == null)
            return fail("Service sections didn't match: {0} != {1}", new Object[] {ss1, ss2});

        if (!ss1.getId().equals(ss2.getId()))
            return fail("Service ids didn't match: {0} != {1}", new Object[] {ss1.getId(), ss2.getId()});

        if (!ss1.getClassName().equals(ss2.getClassName()))
            return fail("Service class names didn't match: {0} != {1}", new Object[] {ss1.getClassName(), ss2.getClassName()});
       
        // Adapters
        Map a1 = ss1.getAllAdapterSettings();
        Map a2 = ss2.getAllAdapterSettings();
        if (!adaptersMatch(a1, a2))
            return false;

        // Default Adapters
        AdapterSettings as1 = (AdapterSettings)ss1.getDefaultAdapter();
        AdapterSettings as2 = (AdapterSettings)ss2.getDefaultAdapter();
        if (!adapterDefinitionsMatch(as1, as2))
            return false;


        // Default Channels
        List dc1 = ss1.getDefaultChannels();
        List dc2 = ss2.getDefaultChannels();
        if (dc1.size() != dc2.size())
            return fail("Default channels sections didn't match by size: {0} != {1}", new Object[] {new Integer(dc1.size()), new Integer(dc2.size())});

        for (int i = 0; i < dc1.size(); i++)
        {
            ChannelSettings c1 = (ChannelSettings)dc1.get(i);
            ChannelSettings c2 = (ChannelSettings)dc2.get(i);
            if (!channelDefinitionsMatch(c1, c2))
                return false;
        }

        // Destinations
        Map d1 = ss1.getDestinationSettings();
        Map d2 = ss2.getDestinationSettings();
        if (!destinationsMatch(d1, d2))
            return false;

        return true;
    }

    protected boolean adaptersMatch(Map a1, Map a2)
    {
        if (a1 == null && a2 == null)
            return true;

        if (a1 == null || a2 == null)
            return fail("Adapters sections didn't match: {0} != {1}", new Object[] {a1, a2});

        if (a1.size() != a2.size())
            return fail("Adapters sections didn't match by size: {0} != {1}", new Object[] {new Integer(a1.size()), new Integer(a2.size())});

        Iterator it = a1.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String)it.next();

            AdapterSettings as1 = (AdapterSettings)a1.get(key);
            AdapterSettings as2 = (AdapterSettings)a2.get(key);
            if (!adapterDefinitionsMatch(as1, as2))
                return false;
        }

        return true;
    }

    protected boolean adapterDefinitionsMatch(AdapterSettings as1, AdapterSettings as2)
    {
        if (as1 == null && as2 == null)
            return true;

        if (as1 == null || as2 == null)
            return fail("Adapters didn't match: {0} != {1}", new Object[] {as1, as2});

        if (!as1.getId().equals(as2.getId()))
            return fail("Adapter ids didn't match: {0} != {1}", new Object[] {as1.getId(), as2.getId()});

        if (!as1.getClassName().equals(as2.getClassName()))
            return fail("Adapter class names didn't match: {0} != {1}", new Object[] {as1.getClassName(), as2.getClassName()});

        if (as1.isDefault() != as2.isDefault())
            return fail("Adapter default attributes didn't match: {0} != {1}", new Object[] {Boolean.valueOf(as1.isDefault()), Boolean.valueOf(as2.isDefault())});

        if (!propertiesMatch(as1.getProperties(), as2.getProperties()))
            return false;

        return true;
    }

    protected boolean channelDefinitionsMatch(ChannelSettings c1, ChannelSettings c2)
    {
        if (c1 == null && c2 == null)
            return true;

        if (c1 == null || c2 == null)
            return fail("Channels didn't match: {0} != {1}", new Object[] {c1, c2});

        if (!c1.getId().equals(c2.getId()))
            return fail("Channel ids didn't match: {0} != {1}", new Object[] {c1.getId(), c2.getId()});

        if (!c1.getEndpointType().equals(c1.getEndpointType()))
            return fail("Channel endpoint types didn't match: {0} != {1}", new Object[] {c1.getEndpointType(), c2.getEndpointType()});

        if (!c1.getUri().equals(c2.getUri()))
            return fail("Channel endpoint URIs didn't match: {0} != {1}", new Object[] {c1.getUri(), c2.getUri()});

        if (!propertiesMatch(c1.getProperties(), c2.getProperties()))
            return false;

        return true;
    }

    protected boolean destinationsMatch(Map d1, Map d2)
    {
        if (d1 == null && d2 == null)
            return true;

        if (d1 == null || d2 == null)
            return fail("Destinations sections didn't match: {0} != {1}", new Object[] {d1, d2});

        if (d1.size() != d2.size())
            return fail("Destinations sections didn't match by size: {0} != {1}", new Object[] {new Integer(d1.size()), new Integer(d2.size())});

        Iterator it = d1.keySet().iterator();
        while (it.hasNext())
        {
            String key = (String)it.next();

            DestinationSettings ds1 = (DestinationSettings)d1.get(key);
            DestinationSettings ds2 = (DestinationSettings)d2.get(key);
            if (!destinationDefinitionsMatch(ds1, ds2))
                return false;
        }

        return true;
    }

    protected boolean destinationDefinitionsMatch(DestinationSettings d1, DestinationSettings d2)
    {
        if (d1 == null && d2 == null)
            return true;

        if (d1 == null || d2 == null)
            return fail("Destinations didn't match: {0} != {1}", new Object[] {d1, d2});

        if (!d1.getId().equals(d2.getId()))
            return fail("Destination ids didn't match: {0} != {1}", new Object[] {d1.getId(), d2.getId()});

        // Properties
        if (!propertiesMatch(d1.getProperties(), d1.getProperties()))
            return false;

         // Channels
        List dc1 = d1.getChannelSettings();
        List dc2 = d2.getChannelSettings();
        if (dc1.size() != dc2.size())
            return fail("Destination channels sections didn't match by size: {0} != {1}", new Object[] {new Integer(dc1.size()), new Integer(dc2.size())});

        for (int i = 0; i < dc1.size(); i++)
        {
            ChannelSettings c1 = (ChannelSettings)dc1.get(i);
            ChannelSettings c2 = (ChannelSettings)dc2.get(i);
            if (!channelDefinitionsMatch(c1, c2))
                return false;
        }

        // Security
        SecurityConstraint sc1 = d1.getConstraint();
        SecurityConstraint sc2 = d2.getConstraint();
        if (!securityConstraintsMatch(sc1, sc2))
            return false;

        // Adapter
        AdapterSettings as1 = d1.getAdapterSettings();
        AdapterSettings as2 = d2.getAdapterSettings();
        if (!adapterDefinitionsMatch(as1, as2))
            return false;

        return true;
    }

    protected boolean securityConstraintsMatch(SecurityConstraint sc1, SecurityConstraint sc2)
    {
        if (sc1 == null && sc2 == null)
            return true;

        if (sc1 == null || sc2 == null)
            return fail("Security constraints didn't match: {0} != {1}", new Object[] {sc1, sc2});

        if (!sc1.getId().equals(sc2.getId()))
            return fail("Security constraint ids didn't match: {0} != {1}", new Object[] {sc1.getId(), sc2.getId()});

        if (!sc1.getMethod().equals(sc2.getMethod()))
            return fail("Security constraint methods didn't match: {0} != {1}", new Object[] {sc1.getMethod(), sc2.getMethod()});
        
        // Roles
       List r1 = sc1.getRoles();
       List r2 = sc2.getRoles();
       if (r1.size() != r2.size())
           return fail("Security constraint roles sections didn't match by size: {0} != {1}", new Object[] {new Integer(r1.size()), new Integer(r2.size())});

        for (int i = 0; i < r1.size(); i++)
        {
            String role1 = (String)r1.get(i);
            String role2 = (String)r2.get(i);
            if (!role1.equals(role2))
                return fail("Security constraint roles didn't match: {0} != {1}", new Object[] {role1, role2});
        }

        return true;
    }

    protected boolean propertiesMatch(Map p1, Map p2)
    {
        if (p1 == null && p2 == null)
            return true;

        if (p1 == null || p2 == null)
            return fail("Properties didn't match: {0} != {1}", new Object[] {p1, p2});

        if (!mapsMatch(p1, p2))
            return false;

        return true;
    }

    protected boolean valuesMatch(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return true;

        if (o1 instanceof Map)
        {
            return mapsMatch((Map)o1, (Map)o2);
        }
        else if (o1 instanceof List)
        {
            return listsMatch((List)o1, (List)o2);
        }
        else if (o1 != null)
        {
            if (hasComplexChildren(o1))
            {
                // We avoid checking a complex/custom types twice in
                // case of circular dependencies
                Object known = knownObjects.get(o1);
                if (known != null)
                {
                    return true;
                }
                else
                {
                    knownObjects.put(o1, o2);
                }
            }

            return o1.equals(o2);
        }

        return false;
    }

    protected boolean stringValuesMatch(String str1, String str2)
    {
        if (str1 == null && str2 == null)
            return true;

        if (str1 != null && !str1.equals(str2))
            return false;

        return true;
    }

    protected boolean mapsMatch(Map map1, Map map2)
    {
        if (map1.size() != map2.size())
            return false;

        // We avoid checking a Map twice in case
        // of circular dependencies
        Object known = knownObjects.get(map1);
        if (known != null)
        {
            return true;
        }
        else
        {
            knownObjects.put(map1, map2);
        }

        Iterator it = map1.keySet().iterator();
        while (it.hasNext())
        {
            Object next = it.next();
            if (next instanceof String)
            {
                String key = (String)next;
                Object val1 = map1.get(key);
                Object val2 = map2.get(key);

                if (!valuesMatch(val1, val2))
                    return false;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    protected boolean listsMatch(List list1, List list2)
    {
        if (list1.size() != list2.size())
            return false;

        // We avoid checking a List twice in case
        // of circular dependencies
        Object known = knownObjects.get(list1);
        if (known != null)
        {
            return true;
        }
        else
        {
            knownObjects.put(list1, list2);
        }

        for (int i = 0; i < list1.size(); i++)
        {
            Object o1 = list1.get(i);
            Object o2 = list2.get(i);

            if (!valuesMatch(o1, o2))
                return false;
        }

        return true;
    }

    private static boolean hasComplexChildren(Object o)
    {
        if (o instanceof String
                || o instanceof Boolean
                || o instanceof Number
                || o instanceof Character
                || o instanceof Date)
        {
            return false;
        }

        return true;
    }


    protected boolean fail(String message)
    {
        return fail(message, null);
    }

    protected boolean fail(String message, Object[] parameters)
    {
        if (!isNegativeTest())
        {
            if (parameters != null)
            {
                // replace all of the parameters in the msg string
                for (int i = 0; i < parameters.length; i++)
                {
                    String replacement = parameters[i] != null ? parameters[i].toString() : "null";

                    message = message.replaceAll("\\{" + i + "\\}", replacement);
                }
            }

            System.err.println(message);
        }

        return false;
    }
}
