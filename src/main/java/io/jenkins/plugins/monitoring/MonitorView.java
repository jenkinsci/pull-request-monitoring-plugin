package io.jenkins.plugins.monitoring;

import java.util.Optional;

/**
 * This interface defines the view to be shown in the monitoring dashboard.
 */
public interface MonitorView {

    /**
     * Defines the the to be shown.
     *
     * @return
     *          the title.
     */
    String getTitle();

    /**
     * Defines the id for the view.
     *
     * @return
     *          the id.
     */
    default String getId() {
        return getClass().getName();
    }

    /**
     * Defines the preferred width of the view.
     *
     * @return
     *          the width in pixels.
     */
    int getPreferredWidth();

    /**
     * Defines the preferred height of the view.
     *
     * @return
     *          the height in pixels.
     */
    int getPreferredHeight();

    /**
     * Defines the icon to show in the dropdown list of available plugins.
     *
     * @return
     *          the icon url.
     */
    String getIconUrl();

    /**
     * Defines the relative link to a detail view of showed plugin.
     *
     * @return
     *          the link to the detail view, or {@code Optional.empty()},
     *          if no link should be added to view.
     */
    Optional<String> getDetailViewUrl();

}
