package io.jenkins.plugins.monitoring;

import hudson.ExtensionPoint;
import hudson.model.Run;

import java.util.Collection;

/**
 * Factory for {@link MonitorPortlet}.
 */
public interface MonitorPortletFactory extends ExtensionPoint {

    /**
     * Get a collection of {@link MonitorPortlet} to display.
     *
     * @param build
     *              the reference {@link Run}.
     *
     * @return
     *              a collection of {@link MonitorPortlet}.
     */
    Collection<MonitorPortlet> getPortlets(Run<?, ?> build);

    /**
     * Defines the name of the factory.
     *
     * @return
     *              the name to display for the factory.
     */
    String getDisplayName();
}