<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<html xmlns="http://www.w3.org/1999/xhtml">
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<% long startTime = System.currentTimeMillis(); %>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title><s:property value="getProp('samplesocialsiteapplication.site.name')"/>: <s:property value="pageTitle" /></title>
    <link rel="stylesheet" type="text/css" href='<s:url value="/app-ui/themes/css/socialsite.css" />' />
    <!--
      * "Integral" design by Free CSS Templates - http://www.freecsstemplates.org
      * Released for free under a Creative Commons Attribution 2.5 License
    -->
    <link rel="stylesheet" type="text/css" href='<s:url value="/app-ui/themes/integral/default.css" />' />
    <link rel="stylesheet" type="text/css" href='<s:url value="/app-ui/themes/integral/socialsite_customization.css" />' />
</head>
<body class="yui-skin-sam view">


<div id="header">
       <!-- HEADER CONTENT BEGIN -->
       <tiles:insertAttribute name="menu" />
       <!-- HEADER CONTENT END -->
</div>


<div id="content">

	<div id="colOne">
           <!-- SIDEBAR CONTENT BEGIN -->
           <tiles:insertAttribute name="sidebar" />
           <br />
           <br />
           <!-- SIDEBAR CONTENT END -->
	</div>

	<div id="colTwo">
            <!-- MAIN CONTENT BEGIN -->


            <%-- Error Messages --%>
            <s:if test="!actionErrors.isEmpty || !fieldErrors.isEmpty">
                <div id="errors" class="errors">
                    <ul>
                        <s:iterator id="actionError" value="actionErrors">
                            <li><s:property value="#actionError" /></li>
                        </s:iterator>
                        <s:iterator id="fieldErrorName" value="fieldErrors.keySet()">
                            <s:iterator id="fieldErrorValue" value="fieldErrors[#fieldErrorName]">
                                <li><s:property value="#fieldErrorValue" /></li>
                            </s:iterator>
                        </s:iterator>
                    </ul>
                </div>
            </s:if>

            <%-- Messages --%>
            <s:if test="!actionMessages.isEmpty">
                <div id="success" class="success">
                    <ul>
                        <s:iterator id="actionMessage" value="actionMessages">
                            <li><s:property value="#actionMessage" /></li>
                        </s:iterator>
                    </ul>
                </div>
            </s:if>

            <tiles:insertAttribute name="content" />
            <br />
            <br />

           <!-- MAIN CONTENT END -->
	</div>

</div>
<div id="footer">
    <!-- FOOTER CONTENT BEGIN -->
    
    <p style="text-align:center">
    &copy;2008&nbsp;Sun Microsystems Inc.&nbsp;|&nbsp;
    <a href="http://www.sun.com/">About Sun</a>&nbsp;|&nbsp;
    <a href="http://www.sun.com/suntrademarks/">Trademarks</a>&nbsp;|&nbsp;
    <a href="http://www.sun.com/privacy/">Privacy Policy</a>&nbsp;|&nbsp;
    <a href="mailto:socialsoftware-support@sun.com">Feedback</a>&nbsp;|&nbsp;
    <a href="mailto:socialsoftware-support@sun.com">Report Bugs</a>
    </p>
    <s:text name="Footer.extraText"><span style="display:none;"></span></s:text>
 
    <!-- FOOTER CONTENT END -->
</div>
<!-- Page Generation Took: <%=(System.currentTimeMillis()-startTime)%>ms -->
</body>
</html>
