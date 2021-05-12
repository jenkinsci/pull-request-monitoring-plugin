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
     *          the icon url.
     */
    String getIconUrl();

    /**
     * Defines the relative link to a detail portlet of showed plugin.
     *
     * @return
     *          the link to the detail portlet, or {@code Optional.empty()},
     *          if no link should be added to portlet.
     */
    Optional<String> getDetailViewUrl();
}
