/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.social.opensocial.service;

import com.sun.socialsite.web.rest.opensocial.SocialSiteToken;


/**
 * A fake SecurityToken implementation to help testing.
 */
public class FakeSocialSiteGadgetToken extends SocialSiteToken {

    private String appId;
    private long moduleId;
    private String ownerId;
    private String viewerId;
    private String groupHandle;
    private String updatedToken;
    private String trustedToken;
    private boolean isAnonymous;
    private boolean isForContainerPage;

    public FakeSocialSiteGadgetToken() {
        super("dummy");
    }

    @Override
    public boolean hasPermission(String permissionName) {
        return true;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the moduleId
     */
    public long getModuleId() {
        return moduleId;
    }

    /**
     * @param moduleId the moduleId to set
     */
    public void setModuleId(long moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return the viewerId
     */
    public String getViewerId() {
        return viewerId;
    }

    /**
     * @param viewerId the viewerId to set
     */
    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    /**
     * @return the groupHandle
     */
    public String getGroupHandle() {
        return groupHandle;
    }

    /**
     * @param groupHandle the groupHandle to set
     */
    public void setGroupHandle(String groupHandle) {
        this.groupHandle = groupHandle;
    }

    /**
     * @return the updatedToken
     */
    public String getUpdatedToken() {
        return updatedToken;
    }

    /**
     * @param updatedToken the updatedToken to set
     */
    public void setUpdatedToken(String updatedToken) {
        this.updatedToken = updatedToken;
    }

    /**
     * @return the trustedToken
     */
    public String getTrustedToken() {
        return trustedToken;
    }

    /**
     * @param trustedToken the trustedToken to set
     */
    public void setTrustedToken(String trustedToken) {
        this.trustedToken = trustedToken;
    }

    /**
     * @return the isAnonymous
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }

    /**
     * @param isAnonymous the isAnonymous to set
     */
    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    /**
     * @return the isForContainerPage
     */
    public boolean isForContainerPage() {
        return isForContainerPage;
    }

    /**
     * @param isForContainerPage the isForContainerPage to set
     */
    public void setForContainerPage(boolean isForContainerPage) {
        this.isForContainerPage = isForContainerPage;
    }

    public String getTrustedJson() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getContainer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAppUrl() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAuthenticationMode() {
        return null;  // TODO do it right?
    }

    public String getActiveUrl() {
        return null;  // TODO do it right?
    }
}
