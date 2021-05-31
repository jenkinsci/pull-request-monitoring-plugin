package io.jenkins.plugins.monitoring;

import hudson.model.InvisibleAction;
import hudson.model.Run;

/**
 * This action is added to {@link Run}, if configuration is set in the Jenkinsfile. Therefore the portlets of this
 * action {@link MonitoringCustomAction} overwrites the one from the {@link MonitoringDefaultAction} added by the
 * {@link MonitoringDefaultActionFactory}.
 *
 * @author Simon Symhoven
 */
public class MonitoringCustomAction extends InvisibleAction {
    private final String portlets;

    /**
     * Creates a new instance of {@link MonitoringCustomAction}.
     *
     * @param portlets
     *          the portlets as json array string to be add.
     */
    public MonitoringCustomAction(final String portlets) {
        super();
        this.portlets = portlets;
    }

    public String getPortlets() {
        return portlets;
    }
}
