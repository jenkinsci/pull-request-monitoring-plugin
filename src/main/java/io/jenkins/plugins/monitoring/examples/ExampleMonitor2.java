package io.jenkins.plugins.monitoring.examples;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.monitoring.MonitorFactory;
import io.jenkins.plugins.monitoring.MonitorView;

import java.util.*;

/**
 * An example Monitor View.
 */
public class ExampleMonitor2 implements MonitorView {
    private final Run<?, ?> build;
    private final String id;

    /**
     * Create a new {@link ExampleMonitor2}.
     *
     * @param run
     *          the {@link Run}
     * @param viewId
     *          the id.
     */
    public ExampleMonitor2(Run<?, ?> run, String viewId) {
        this.build = run;
        this.id = viewId;
    }

    @Override
    public String getTitle() {
        return "First Monitor " + this.build.getDisplayName();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getChartId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a new {@link PieChartModel}.
     *
     * @return
     *          the model for pie chart.
     */
    public String getModel() {
        PieChartModel model = new PieChartModel("Title");
        Random r = new Random();
        model.add(new PieData("Segment 1", r.nextInt(100)), Palette.RED);
        model.add(new PieData("Segment 2",  r.nextInt(100)), Palette.GREEN);
        model.add(new PieData("Segment 3",  r.nextInt(100)), Palette.YELLOW);

        return new JacksonFacade().toJson(model);
    }

    /**
     * Creates a new {@link ExampleMonitor2Factory}.
     */
    @Extension
    public static class ExampleMonitor2Factory implements MonitorFactory {
        @Override
        public Collection<MonitorView> getMonitorViews(Run<?, ?> build) {
            List<MonitorView> monitors = new ArrayList<>();
            monitors.add(new ExampleMonitor2(build, "io.jenkins.plugins.monitoring.examples.ExampleMonitor2.View1"));
            monitors.add(new ExampleMonitor2(build, "io.jenkins.plugins.monitoring.examples.ExampleMonitor2.View2"));
            return monitors;
        }
    }
}
