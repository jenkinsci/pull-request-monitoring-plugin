package io.jenkins.plugins.monitoring;

import hudson.ExtensionPoint;
import hudson.model.Run;

import java.util.Collection;

/**
 * <p>Defines the {@link ExtensionPoint} to register one or more new pull request monitoring portlets.</p>
 *
 * <p>The {@link #getDisplayName()} is shown in a dropdown list as optgroup. The children of the optgroup
 * are the registered portlets of {@link #getPortlets(Run)}. </p>
 *
 * <p>Since an empty dashboard is always added by default, it is possible that the method {@link #getPortlets(Run)} will
 * be called even though the current run may not be finished. It is therefore advisable to perform a null check on
 * the actions of the run required by your portlet and return an empty list if necessary.
 * (Example: <a href="https://bit.ly/2SDZzAg">code-coverage-api</a>)</p>
 *
 * @since 1.6.0
 * @author Simon Symhoven
 */
public abstract class MonitorPortletFactory implements ExtensionPoint {

    /**
     * Get a collection of {@link MonitorPortlet} to display.
     *
     * @param build
     *              the reference {@link Run}.
     *
     * @return
     *              a collection of {@link MonitorPortlet}.
     */
    public abstract Collection<MonitorPortlet> getPortlets(Run<?, ?> build);

    /**
     * Defines the name of the factory.
     *
     * @return
     *              the name to display for the factory.
     */
    public abstract String getDisplayName();

}
