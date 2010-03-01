
package com.samplesocialsiteapplication.web.ui.core.struts2;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;


public class SessionContextInterceptor extends AbstractInterceptor {

    private static Log log = LogFactory.getLog(SessionContextInterceptor.class);

    public String intercept(ActionInvocation invocation) throws Exception {
       Action action = (Action)(invocation.getAction());
       if (action instanceof AbstractAction) {
           AbstractAction a = (AbstractAction)action;
           a.setViewerId(ServletActionContext.getRequest().getRemoteUser());
       }
       return invocation.invoke();
    }

}
