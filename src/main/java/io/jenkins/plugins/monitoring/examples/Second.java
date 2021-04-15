package io.jenkins.plugins.monitoring.examples;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.PercentagePieChart;
import hudson.Extension;
import io.jenkins.plugins.monitoring.MonitorView;
import io.jenkins.plugins.monitoring.MonitoringMultibranchProjectAction;

/**
 * Another example Monitor View.
 */
@Extension
public class Second implements MonitorView {
    @Override
    public String getTitle() {
        return "Second Monitor";
    }

    @Override
    public Class<?> getClazz() {
        return Second.class;
    }

    @Override
    public String getIcon() {
        return MonitoringMultibranchProjectAction.getIconBig();
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
}
