/**
 * Copyright © 2016 Open Text.  All Rights Reserved.
 */
package com.opentext.otag.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Container wide singleton type service. This class is crucial to the
 * restart process when we are running inside of Tomcat. Its
 * {@link #containerReady} function is used by the deployed services to
 * determine when it is safe to initiate HTTP communications with the
 * Gateway. Services and apps <strong>must</strong> not interrupt Tomcat's
 * startup within Servlet type classes. So we provide a handler and hook it
 * into a deployed services/apps startup sequence.
 * <p>
 * This class should only ever be found in a jar within Tomcats lib to ensure that
 * webapp Classloaders inherit the instance loaded by Tomcats parent loader. We rely
 * on this mechanism.
 *
 * @author Rhys Evans rhyse@opentext.com
 * @version 16.0.0
 */
public class TomcatLifecycleListener implements LifecycleListener {

    private static final Log LOG = LogFactory.getLog(TomcatLifecycleListener.class);

    private static final String LOG_MARKER = "***--- Tomcat Lifecycle Listener Event ---***";

    /**
     * Is the host container ready for the services to start?
     */
    private static boolean CONTAINER_STARTED = false;

    /**
     * Is the container ready to start processing HTTP requests?
     *
     * @return true if the container has completed its setup routine
     */
    public static boolean containerReady() {
        try {
            return CONTAINER_STARTED;
        } catch (Exception e) {
            LOG.error("Error encountered when inspecting containerReady", e);
            return false;
        }
    }

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        synchronized (this) {
            if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
                LOG.info(LOG_MARKER);
                LOG.info("Received the \"after_start\" lifecycle event from Tomcat, it is now safe to proceed");
                LOG.info(LOG_MARKER);
                CONTAINER_STARTED = true;
            }

            if (Lifecycle.AFTER_STOP_EVENT.equals(lifecycleEvent.getType())) {
                LOG.info(LOG_MARKER);
                LOG.info("Received the \"after_stop\" lifecycle event from Tomcat");
                LOG.info(LOG_MARKER);
                CONTAINER_STARTED = false;
            }
        }
    }

    /**
     * Manually indicate startup has completed. For testing purposes only.
     */
    public static void setStarted() {
        CONTAINER_STARTED = true;
    }

}
