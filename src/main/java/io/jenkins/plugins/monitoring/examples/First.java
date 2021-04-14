package io.jenkins.plugins.monitoring.examples;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;
import hudson.Extension;
import io.jenkins.plugins.monitoring.MonitorView;
import io.jenkins.plugins.monitoring.MonitoringMultibranchProjectAction;

/**
 * An example Monitor View.
 */
@Extension
public class First implements MonitorView {
    @Override
    public String getTitle() {
        return "First Monitor";
    }

    @Override
    public Class<?> getClazz() {
        return First.class;
    }

    @Override
    public String getIcon() {
        return MonitoringMultibranchProjectAction.getIconBig();
    }

    /**
     * Generates a new {@link PieChartModel}.
     *
     * @return
     *          the model for pie chart.
     */
    public String getModel() {
        PieChartModel model = new PieChartModel("Title");

        model.add(new PieData("Segment 1 name", 10), Palette.RED);
        model.add(new PieData("Segment 2 name", 15), Palette.GREEN);
        model.add(new PieData("Segment 3 name", 20), Palette.YELLOW);

        return new JacksonFacade().toJson(model);
    }
}
