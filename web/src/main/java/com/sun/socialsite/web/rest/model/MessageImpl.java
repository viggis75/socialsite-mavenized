package com.sun.socialsite.web.rest.model;

import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.Url;
import org.json.JSONObject;

import java.util.List;
import java.util.Date;

import com.sun.socialsite.pojos.RelationshipRequest;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.pojos.GroupRequest;
import com.sun.socialsite.pojos.MessageContent;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.SocialSiteException;

/**
 * Describe your class here
 *
 * @author $Author$
 *         <p/>
 *         $Id$
 */
public class MessageImpl implements Message {

    private String body ;
    private String bodyId = null;
    private String id;
    private Profile sender ;
    private Message.Status status ;
    private String title;
    private Message.Type type;
    private ExtendedType extendedType;

    private Integer level = 2;
    private String howknow = null;

    private String appId = null;
    private String appUrl = null;

    private List<String> collectionIds = null;
    private List<String> recipients = null;
    private List<String> replies = null;
    private String inReplyTo = null;
    private Date timeSent;
    private Date updated = null;

    private static final JSONObject EMPTY_JSON = new JSONObject();

    private AppManager appManager = Factory.getSocialSite().getAppManager();
    private ProfileManager profManager = Factory.getSocialSite().getProfileManager();

    /**
     * Utility constructor for code that doesn't
     * care about notifications.
     *
     * @param content A message content object representing a message
     *     sent from one person to another.
     */
    public MessageImpl(MessageContent content) {
        this(content, false);
    }

    /**
     * Constructor that takes a MessageContext object.
     *
     * @param content MessageContent object to be wrapped.
     * @param isGroupInvite If true, tells the class to set type
     *     to 'notification'
     */
    public MessageImpl(MessageContent content, boolean isGroupInvite) {
        if (isGroupInvite) {
            this.title = "Invitation to join group " + content.getGroup().getName();
            this.body = "";            
            this.type = Type.NOTIFICATION;
            this.extendedType = ExtendedType.GROUP_INVITE;
        } else {
            this.title = content.getSummary();
            this.body = content.getContent();
            this.type = Type.NOTIFICATION;
            this.extendedType = ExtendedType.PRIVATE_MESSAGE;
        }
        this.id = content.getId();
        this.sender = content.getProfile();
        this.timeSent = content.getUpdated();

        String contentStatus = content.getStatus();
        if (contentStatus.equals("new"))
            this.status = Message.Status.NEW;
        else if (contentStatus.equals("read"))
            this.status = Message.Status.FLAGGED;
        else if (contentStatus.equals("deleted"))
            this.status = Message.Status.DELETED;
        else
            this.status = Message.Status.NEW; // TODO is this the right thing to do?

        this.appId = content.getAppId();
        this.inReplyTo = content.getReplyToId();
    }

    /**
     * Constructor that takes a FriendshipRequest object.
     *
     * @param request The friendship request to wrap
     */
    public MessageImpl(RelationshipRequest request) {
        Profile fromUser = request.getProfileFrom();
        this.body = "";

        this.id = request.getId();
        this.sender = fromUser;

        this.title = "Relationship Request from " + fromUser.getName();
        this.type = Type.NOTIFICATION;
        this.extendedType = MessageImpl.ExtendedType.RELATIONSHIP_REQUEST;
        this.level = request.getLevelTo();
        this.howknow = request.getHowknow();
        this.timeSent = request.getCreated();

        String requestStatus = request.getStatus().toString();
        if (requestStatus.equals("new"))
            this.status = Message.Status.NEW;
        else if (requestStatus.equals("read"))
            this.status = Message.Status.FLAGGED;
        else if (requestStatus.equals("deleted"))
            this.status = Message.Status.DELETED;
        else
            this.status = Message.Status.NEW; // TODO is this the right thing to do?

    }

    /**
     * Constructor that takes a GroupRequest object.
     *
     * @param request The group request to wrap
     */
    public MessageImpl(GroupRequest request) {
        Profile fromUser = request.getProfileFrom();
        this.body = "";

        this.id = request.getId();
        this.sender = fromUser;

        this.title = "Group Membership Request from " + fromUser.getName() +
                " for group '" + request.getGroup().getName() + "'";
        this.type = Type.NOTIFICATION;
        this.extendedType = ExtendedType.GROUP_MEMBERSHIP_REQUEST;
        this.timeSent = request.getCreated();

        String requestStatus = request.getStatus().toString();
        if (requestStatus.equals("new"))
            this.status = Message.Status.NEW;
        else if (requestStatus.equals("read"))
            this.status = Message.Status.FLAGGED;
        else if (requestStatus.equals("deleted"))
            this.status = Message.Status.DELETED;
        else
            this.status = Message.Status.NEW; // TODO is this the right thing to do?

    }

    public String getAppUrl() {
        if (appUrl == null && appId != null ) {
            try {
                return appManager.getApp(appId).getURL().toString();
            }
            catch (SocialSiteException sse) {
                // this is a problem
                return null;
            }
        }
        else {
            return appUrl;
        }
    }

    public void setAppUrl(String url) {
        this.appUrl = url;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String newBody) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getBodyId() {
        return bodyId ; // dude, we just set body to "" everywhere, what the heck ID is it supposed to be
    }

    public void setBodyId(String bodyId) {
        this.bodyId = bodyId;
    }

    public List<String> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(List<String> collectionIds) {
        this.collectionIds = collectionIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String parentId) {
        this.inReplyTo = parentId;
    }

    public List<String> getRecipients() {
        return recipients; // TODO as far as I can tell this will always be null
    }

    public List<String> getReplies() {
        return replies;
    }

    public Status getStatus() {
        return status;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSenderId() {
        return sender.getId();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setSenderId(String senderId) {
        try {
            sender = profManager.getProfile(senderId);
        }
        catch (SocialSiteException e) {
            // hmm, I guess we just leave the sender to what it was
        }

    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getTitleId() {
        return null;
    }

    public void setTitleId(String titleId) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Type getType() {
        return type;
    }

    public void setType(Type newType) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public Date getUpdated() {
        if (updated == null ) {
            updated = timeSent;
        }
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public List<Url> getUrls() {
        return null;
    }

    public void setUrls(List<Url> urls) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String sanitizeHTML(String htmlStr) {
        return htmlStr;
    }

    public enum ExtendedType {

        /** An email. */
        EMAIL("EMAIL"),

        /** A short private message. */
        NOTIFICATION("NOTIFICATION"),

        /** A message to a specific user that can be seen only by that user. */
        PRIVATE_MESSAGE("PRIVATE_MESSAGE"),

        /** A message to a specific user that can be seen by more than that user. */
        PUBLIC_MESSAGE("PUBLIC_MESSAGE"),

        /** Relationship request from one user to another */
        RELATIONSHIP_REQUEST("RELATIONSHIP_REQUEST"),

        /** Invitation to join a group from one user to another */
        GROUP_INVITE("GROUP_INVITE"),

        /** Request for membership in a group, from group to a user */
        GROUP_MEMBERSHIP_REQUEST("GROUP_MEMBERSHIP_REQUEST");

        /**
         * The type of message.
         */
        private final String jsonString;

        /**
         * Create a message type based on a string token.
         * @param jsonString the type of message
         */
        private ExtendedType(String jsonString) {
            this.jsonString = jsonString;
        }

        /**
         * @return a string representation of the enum.
         */
        @Override
        public String toString() {
            return this.jsonString;
        }

    }

}
