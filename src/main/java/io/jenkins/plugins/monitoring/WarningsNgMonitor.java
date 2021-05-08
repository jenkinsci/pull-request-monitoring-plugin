package io.jenkins.plugins.monitoring;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WarningsNgMonitor implements MonitorPortlet {

    private final ResultAction action;
    private final AnalysisResult result;

    public WarningsNgMonitor(ResultAction action) {
        this.action = action;
        this.result = action.getResult();
    }

    @Override
    public String getTitle() {
        return action.getLabelProvider().getName();
    }

    public String getChartModel() {

        PieChartModel model = new PieChartModel(getTitle());

        model.add(new PieData("New", result.getNewSize()), Palette.RED);
        model.add(new PieData("Fixed", result.getFixedSize()), Palette.GREEN);
        model.add(new PieData("Outstanding", result.getOutstandingIssues().getSize()), Palette.YELLOW);

        return new JacksonFacade().toJson(model);
    }

    @Override
    @JavaScriptMethod
    public String getId() {
        return result.getId();
    }

    @Override
    public int getPreferredWidth() {
        return 250;
    }

    @Override
    public int getPreferredHeight() {
        return 250;
    }

    @Override
    public String getIconUrl() {
        return action.getIconFileName();
    }

    @Override
    public Optional<String> getDetailViewUrl() {
        return Optional.of("../" + action.getUrlName());
    }

    @Extension
    public static class WarningsNgMonitorFactory implements MonitorPortletFactory {

        @Override
        public Collection<MonitorPortlet> getPortlets(Run<?, ?> build) {
            List<ResultAction> action = build.getActions(ResultAction.class);

            return action.stream()
                    .map(WarningsNgMonitor::new)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        @Override
        public String getDisplayName() {
            return "Warnings Ng";
        }
    }
}
