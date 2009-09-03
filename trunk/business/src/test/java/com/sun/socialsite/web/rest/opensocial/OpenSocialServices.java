/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://socialsite.dev.java.net/legal/CDDL+GPL.html
 * or legal/LICENSE.txt.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at legal/LICENSE.txt.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided by Sun
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.socialsite.web.rest.opensocial;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.opensocial.spi.UserId;

/**
 * Combine all services into one class so we can run Shindig tests against them.
 */
public class OpenSocialServices implements PersonService, ActivityService, AppDataService {
    private PersonService personService;
    private ActivityService activityService;
    private AppDataService appDataService;

    @Inject
    public OpenSocialServices(
            PersonService personService,
            ActivityService activityService,
            AppDataService appDataService) {
        this.personService = personService;
        this.activityService = activityService;
        this.appDataService = appDataService;
    }

    public Future<RestfulCollection<Person>> getPeople(Set<UserId> arg0, GroupId arg1, CollectionOptions arg2, Set<String> arg3, SecurityToken arg4) throws SocialSpiException {
        return personService.getPeople(arg0, arg1, arg2, arg3, arg4);
    }

    public Future<Person> getPerson(UserId arg0, Set<String> arg1, SecurityToken arg2) throws SocialSpiException {
        return personService.getPerson(arg0, arg1, arg2);
    }

    public Future<Activity> getActivity(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, String arg4, SecurityToken arg5) throws SocialSpiException {
        return activityService.getActivity(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public Future<Void> deleteActivities(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, SecurityToken arg4) throws SocialSpiException {
        return activityService.deleteActivities(arg0, arg1, arg2, arg3, arg4);
    }

    public Future<Void> createActivity(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, Activity arg4, SecurityToken arg5) throws SocialSpiException {
        return activityService.createActivity(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public Future<DataCollection> getPersonData(Set<UserId> arg0, GroupId arg1, String arg2, Set<String> arg3, SecurityToken arg4) throws SocialSpiException {
        return appDataService.getPersonData(arg0, arg1, arg2, arg3, arg4);
    }

    public Future<Void> deletePersonData(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, SecurityToken arg4) throws SocialSpiException {
        return appDataService.deletePersonData(arg0, arg1, arg2, arg3, arg4);
    }

    public Future<Void> updatePersonData(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, Map<String, String> arg4, SecurityToken arg5) throws SocialSpiException {
        return appDataService.updatePersonData(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public Future<RestfulCollection<Activity>> getActivities(Set<UserId> arg0, GroupId arg1, String arg2, Set<String> arg3, CollectionOptions arg4, SecurityToken arg5) throws SocialSpiException {
        return activityService.getActivities(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public Future<RestfulCollection<Activity>> getActivities(UserId arg0, GroupId arg1, String arg2, Set<String> arg3, CollectionOptions arg4, Set<String> arg5, SecurityToken arg6) throws SocialSpiException {
        return activityService.getActivities(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
    }

}