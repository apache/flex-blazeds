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
package flex.messaging.security;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import oracle.security.jazn.realm.Realm;
import oracle.security.jazn.realm.RealmRole;
import oracle.security.jazn.realm.RealmUser;

/**
 * An Oracle specific implementation of java.security.Principal.
 */
public class OracleUser implements Principal {
    private LoginContext context;
    private Subject subject;

    public OracleUser(LoginContext context) throws LoginException {
        this.context = context;
        context.logout();
        context.login();
        this.subject = context.getSubject();
    }

    public void logout() throws LoginException {
        context.logout();
    }

    private Principal userPrincipal() {
        Set possibleUsers = subject.getPrincipals(RealmUser.class);
        return (Principal) possibleUsers.iterator().next();
    }

    public boolean isMemberOf(List roleNames) {
        boolean result = false;
        Set possibleUsers = subject.getPrincipals(RealmRole.class);
        Iterator itr = possibleUsers.iterator();
        while (itr.hasNext()) {
            RealmRole role = (RealmRole) itr.next();
            Realm realm = role.getRealm();
            String realmFullName = realm.getFullName();
            String roleSimpleName = role.getName();
            if ((realmFullName.length() > 0) &&
                    roleSimpleName.startsWith(realmFullName)) {
                // Format is "<realm full name>\<role name>"
                roleSimpleName = roleSimpleName.substring
                        (realmFullName.length() + 1);
            }

            if (roleNames.contains(roleSimpleName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean equals(Object object) {
        boolean result = false;
        if (object == this) {
            result = true;
        } else if (object instanceof OracleUser) {
            OracleUser other = (OracleUser) object;
            result = this.subject.equals(other.subject);
        }
        return result;
    }

    public String getName() {
        return userPrincipal().getName();
    }

    public int hashCode() {
        return this.subject.hashCode();
    }

    public String toString() {
        return this.subject.toString();
    }
}
