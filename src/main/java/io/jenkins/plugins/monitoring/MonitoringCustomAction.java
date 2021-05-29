package io.jenkins.plugins.monitoring;

import hudson.model.InvisibleAction;
import hudson.model.Run;

/**
 * This action is added to {@link Run}, if configuration is set in the Jenkinsfile. Therefore the {@link Monitor} of this
 * action {@link MonitoringCustomAction} overwrites the one from the {@link MonitoringDefaultAction} added by the
 * {@link MonitoringDefaultActionFactory}.
 *
 * @author Simon Symhoven
 */
public class MonitoringCustomAction extends InvisibleAction {
    private final Monitor monitor;

    /**
     * Creates a new instance of {@link MonitoringCustomAction}.
     *
     * @param monitor
     *          the {@link Monitor} to be add.
     */
    public MonitoringCustomAction(final Monitor monitor) {
        super();
        this.monitor = monitor;
    }

    public Monitor getMonitor() {
        return monitor;
    }
}
