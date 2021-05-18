package io.jenkins.plugins.monitoring;

import hudson.ExtensionPoint;
import hudson.model.Run;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface defines the portlet to be shown in the monitoring dashboard.
 */
public interface MonitorPortlet {

    /**
     * Defines the the to be shown.
     *
     * @return
     *          the title.
     */
    String getTitle();

    /**
     * Defines the id for the portlet.
     *
     * @return
     *          the id.
     */
    default String getId() {
        return getClass().getName();
    }

    /**
     * Defines the preferred width of the portlet.
     *
     * @return
     *          the width in pixels.
     */
    int getPreferredWidth();

    /**
     * Defines the preferred height of the portlet.
     *
     * @return
     *          the height in pixels.
     */
    int getPreferredHeight();

    /**
     * Defines the icon to show in the dropdown list of available portlets.
     *
     * @return
     *          the app relative icon url depending on ${resURL} (/static/cache-id).
     */
    String getIconUrl();

    /**
     * Defines the relative url to a detail view of the portlet.
     *
     * @return
     *          the relative link to the detail view depending on ${rootURL} (/jenkins),
     *          or {@code Optional.empty()}, if no link should be added to portlet.
     */
    Optional<String> getDetailViewUrl();
}
