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

package com.sun.socialsite.web.rest.core;

import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.ProfileDefinition;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ResponseItem;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.service.SocialRequestItem;

import javax.servlet.http.HttpServletResponse;


/**
 * <p>Handles requests to GET and PUT SocialSite Profile data and metadata.</p>
 *
 * <p>Supports these URIs and HTTP methods:</p>
 *    /profiles - GET JSON representation of profile definition metadata.<br />
 */
@Service(name = "profiledef", path="/{userId}/{personId}")
public class ProfileDefinitionHandler { // extends RestrictedDataRequestHandler {

    private static Log log = LogFactory.getLog(ProfileDefinitionHandler.class);

    private static final String PROFILEDEF_PATH = "/profiledef/{userId}/{personId}";


    @Operation(httpMethods="GET")
    public Future<?> get(SocialRequestItem request) {
        log.trace("BEGIN");
        RestrictedDataRequestHandler.authorizeRequest(request);

        //request.applyUrlTemplate(PROFILEDEF_PATH);
        ResponseItem res = null;
        try {
            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            ProfileDefinition pdef = profileManager.getProfileDefinition();
            return ImmediateFuture.newInstance(pdef.toJSON());
        } catch (Exception ex) {
            String msg = "ERROR fetching profile definition";
            res = new ResponseItem(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
            log.error(msg, ex);
        }
        log.trace("END");
        return ImmediateFuture.newInstance(res);
    }
}
