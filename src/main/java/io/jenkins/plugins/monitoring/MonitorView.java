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
     * Defines the relative link to a detail view of showed plugin.
     *
     * @return
     *          the link to the detail view, or {@code Optional.empty()},
     *          if no link should be added to view.
     */
    Optional<String> getDetailViewUrl();

}
