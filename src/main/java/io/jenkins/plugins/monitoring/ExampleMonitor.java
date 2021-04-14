package io.jenkins.plugins.monitoring;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;
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

    @Override
    public String getData() {
        PieChartModel model = new PieChartModel("Title");

        model.add(new PieData("Segment 1", 10), Palette.RED);
        model.add(new PieData("Segment 2", 15), Palette.GREEN);
        model.add(new PieData("Segment 3", 20), Palette.YELLOW);

        return new JacksonFacade().toJson(model);
    }
}
