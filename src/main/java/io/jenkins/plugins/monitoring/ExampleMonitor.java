package io.jenkins.plugins.monitoring;

import hudson.Extension;

/**
 * An example Monitor View.
 */
@Extension
public class ExampleMonitor implements MonitorView {
    @Override
    public String getName() {
        return "ExampleMonitor";
    }

    @Override
    public Class<?> getClazz() {
        return ExampleMonitor.class;
    }

    @Override
    public String getIcon() {
        return MonitoringMultibranchProjectAction.getIconBig();
    }
}
