<%@ page language="java" %>
<%@ page contentType="application/json" %>
<%@ page import="com.sun.socialsite.business.EmfProvider" %>
<%@ page import="com.sun.socialsite.userapi.User" %>
<%@ page import="com.sun.socialsite.userapi.UserManager" %>
<%@ page import="com.sun.socialsite.userapi.UserManagerImpl" %>
<%@ page import="javax.persistence.EntityManager" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.JSONObject" %>
<%!
  private static JSONObject getPersonJSON(String userId) throws Exception {
      if (StringUtils.isEmpty(userId)) {
          return null;
      } else {
          EntityManager em = EmfProvider.getEmf().createEntityManager();
          UserManager userManager = new UserManagerImpl(em);
          User user = userManager.getUserByUserId(userId);
          em.close();
          JSONObject json = new JSONObject();
          json.put("id", userId);
          if (user != null) {
              if (StringUtils.isNotEmpty(user.getFullName())) {
                  json.put("displayName", user.getFullName());
                  json.put("name", new JSONObject().put("unstructured", user.getFullName()));
              } else {
                  json.put("displayName", userId);
              }
              if (StringUtils.isNotEmpty(user.getEmailAddress())) {
                  json.append("emails", new JSONObject().put("value", user.getEmailAddress()).put("primary", true));
              }
          }
          return json;
      }
  }
%>
<%
  JSONObject viewerJson = getPersonJSON(request.getRemoteUser());
  JSONObject ownerJson = getPersonJSON(request.getParameter("owner"));
%>

{
  'timeout': <%=request.getSession().getMaxInactiveInterval()%>,
  'assertions': {
    'containerId': 'SampleSocialsiteApplication',
    <% if (viewerJson != null) { %>
      'viewer': <%=viewerJson.toString()%>,
    <% } %>
    <% if (ownerJson != null) { %>
      'owner': <%=ownerJson.toString()%>,
    <% } %>
  }
}
