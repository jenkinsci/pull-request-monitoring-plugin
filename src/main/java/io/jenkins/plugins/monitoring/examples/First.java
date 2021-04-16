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
public class First implements MonitorView {
    private final Run<?, ?> build;
    private final String id;

    /**
     * Create a new {@link First}.
     *
     * @param run
     *          the {@link Run}
     * @param viewId
     *          the id.
     */
    public First(Run<?, ?> run, String viewId) {
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

    @Override
    public String getIcon() {
        return "/plugin/pull-request-monitoring/monitors/first.png";
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
        model.add(new PieData("Segment 1", Math.abs(r.nextInt())), Palette.RED);
        model.add(new PieData("Segment 2",  Math.abs(r.nextInt())), Palette.GREEN);
        model.add(new PieData("Segment 3",  Math.abs(r.nextInt())), Palette.YELLOW);

        return new JacksonFacade().toJson(model);
    }

    /**
     * Creates a new {@link FirstFactory}.
     */
    @Extension
    public static class FirstFactory implements MonitorFactory {
        @Override
        public Collection<MonitorView> getMonitorViews(Run<?, ?> build) {
            List<MonitorView> monitors = new ArrayList<>();
            monitors.add(new First(build, "io.jenkins.plugins.monitoring.examples.First.View1"));
            monitors.add(new First(build, "io.jenkins.plugins.monitoring.examples.First.View2"));
            return monitors;
        }
    }
}
