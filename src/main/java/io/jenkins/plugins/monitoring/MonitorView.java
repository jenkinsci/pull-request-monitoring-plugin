package io.jenkins.plugins.monitoring;

/**
 * This interface defines the view to be shown in the monitoring dashboard.
 */
public interface MonitorView {

    /**
     * Defines the the to be shown.
     * @return
     *          the title.
     */
    String getTitle();

    /**
     * Defines the id for the view.
     * @return
     *          the id.
     */
    String getId();


}
