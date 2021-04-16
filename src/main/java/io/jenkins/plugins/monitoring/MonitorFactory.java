package io.jenkins.plugins.monitoring;

import hudson.ExtensionPoint;
import hudson.model.Run;

import java.util.Collection;

/**
 * Factory for {@link MonitorView}.
 */
public interface MonitorFactory extends ExtensionPoint {

    /**
     * Get a collection of {@link MonitorView} to display.
     *
     * @param build
     *              the reference {@link Run}.
     *
     * @return
     *              a collection of {@link MonitorView}.
     */
    Collection<MonitorView> getMonitorViews(Run<?, ?> build);

}
