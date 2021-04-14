package io.jenkins.plugins.monitoring;

import hudson.ExtensionPoint;


/**
 * This interface defines the view to be shown in the monitoring dashboard.
 */
public interface MonitorView extends ExtensionPoint {

    /**
     * Defines the the to be shown.
     * @return
     *          the title.
     */
    String getTitle();

    /**
     * Defines the class, which implements the {@link MonitorView}.
     * @return
     *          the class.
     */
    Class<?> getClazz();

    /**
     * Defines the icon to be displayed in the dashboard.
     * @return
     *          the icon filename.
     */
    String getIcon();

}
