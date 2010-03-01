/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.samplesocialsiteapplication.web.listeners;

import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sun.socialsite.business.startup.Startup;
import com.sun.socialsite.business.startup.StartupException;
import com.sun.socialsite.config.Config;


/**
 * Responds to app init/destroy events and holds SocialSite instance.
 */
public class ContextListener implements ServletContextListener {

    private static Log log = LogFactory.getLog(ContextListener.class);

    // reference to ServletContext object
    private static ServletContext servletContext;


    public ContextListener() {
        super();
    }


    /**
     * Get the ServletContext.
     *
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }


    /**
     * Responds to context initialization event by processing context
     * parameters for easy access by the rest of the application.
     */
    public void contextInitialized(ServletContextEvent sce) {

        log.info("SocialSite Initializing ... ");

        // Keep a reference to ServletContext object
        servletContext = sce.getServletContext();

        // Set a "context.realpath" property, allowing others to find our filesystem basedir
        Config.setProperty("context.realpath", sce.getServletContext().getRealPath("/"));

        // Set a "context.contextpath" property, allowing others to find our webapp base
        try {
            URL baseUrl = new URL(Config.getProperty("socialsite.base.url"));
            Config.setProperty("context.contextpath", baseUrl.getPath());
            log.info("Config[context.contextpath]="+Config.getProperty("context.contextpath"));
        } catch (MalformedURLException ex) {
            String msg = String.format("Could not decode socialsite.base.url[%s]", Config.getProperty("socialsite.base.url"));
            log.error(msg, ex);
        }

        // Now prepare the core services of the app so we can bootstrap
        try {
            Startup.prepare();
        } catch (StartupException ex) {
            log.fatal("SocialSite startup failed during app preparation", ex);
            return;
        }

        log.info("SocialSite Initialization Complete");

    }


    /**
     * Responds to app-destroy.
     */
    public void contextDestroyed(ServletContextEvent sce) {
    }

}

