package io.jenkins.plugins.monitoring.examples;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.PercentagePieChart;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.monitoring.MonitorFactory;
import io.jenkins.plugins.monitoring.MonitorView;

import java.util.Collection;
import java.util.Collections;

/**
 * Another example Monitor View.
 */
public class Second implements MonitorView {
    private final Run<?, ?> build;

    /**
     * Create a new {@link First}.
     *
     * @param run
     *          the {@link Run}
     */
    public Second(Run<?, ?> run) {
        this.build = run;
    }

    @Override
    public String getTitle() {
        return "Second Monitor " + build.getDisplayName();
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getIcon() {
        return "/plugin/pull-request-monitoring/monitors/second.png";
    }

    /**
     * Generates a new {@link PercentagePieChart}.
     *
     * @param percentage
     *          the percentage for pie chart.
     *
     * @return
     *          the model for pie chart.
     */
    public String getProgressModel(final int percentage) {
        return new JacksonFacade().toJson(new PercentagePieChart().create(percentage));
    }

    /**
     * Creates a new {@link SecondFactory}.
     */
    @Extension
    public static class SecondFactory implements MonitorFactory {
        @Override
        public Collection<MonitorView> getMonitorViews(Run<?, ?> build) {
            return Collections.singleton(new Second(build));
        }
    }
}
