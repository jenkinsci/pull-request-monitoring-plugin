package io.jenkins.plugins.monitoring;

import edu.hm.hafner.analysis.Severity;
import edu.hm.hafner.echarts.JacksonFacade;
import edu.hm.hafner.echarts.Palette;
import edu.hm.hafner.echarts.PieChartModel;
import edu.hm.hafner.echarts.PieData;
import hudson.Extension;
import hudson.model.Run;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import org.json.JSONObject;
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
    @JavaScriptMethod
    public String getId() {
        return result.getId();
    }

    @Override
    public int getPreferredWidth() {
        return 350;
    }

    @Override
    public int getPreferredHeight() {
        return 350;
    }

    @Override
    public String getIconUrl() {
        return action.getIconFileName();
    }

    @Override
    public Optional<String> getDetailViewUrl() {
        return Optional.of("../" + action.getUrlName());
    }

    @Override
    public String getTitle() {
        return action.getLabelProvider().getName();
    }

    /**
     * Get the json data for the sunburst diagram.
     *
     * @return
     *          the data as json string.
     */
    public String getSunburstModel() {
        /*        JSONObject sunburstData = new JSONObject();
        sunburstData.put("fixed", result.getFixedIssues().getSize());
        sunburstData.put("outstanding", result.getOutstandingIssues().getSize());

        JSONObject newIssues = new JSONObject();
        newIssues.put("total", result.getNewIssues().getSize());
        newIssues.put("low", result.getNewIssues().getSizeOf(Severity.WARNING_LOW));
        newIssues.put("normal", result.getNewIssues().getSizeOf(Severity.WARNING_NORMAL));
        newIssues.put("high", result.getNewIssues().getSizeOf(Severity.WARNING_HIGH));
        newIssues.put("error", result.getNewIssues().getSizeOf(Severity.ERROR));

        sunburstData.put("new", newIssues);*/
        JSONObject sunburstData = new JSONObject();
        sunburstData.put("fixed", 5);
        sunburstData.put("outstanding", 2);

        JSONObject newIssues = new JSONObject();
        newIssues.put("total", 3);
        newIssues.put("low", 1);
        newIssues.put("normal", 0);
        newIssues.put("high", 1);
        newIssues.put("error", 1);

        sunburstData.put("new", newIssues);
        return sunburstData.toString();
    }

    /**
     * Checks if {@link AnalysisResult} has issues to display in sunburst diagram.
     *
     * @return
     *          true if result has issues, else false.
     */

    public boolean hasIssues() {
        return result.getFixedIssues().getSize() != 0
                || result.getNewIssues().getSize() != 0
                || result.getOutstandingIssues().getSize() != 0;
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
            return "Warnings Next Generation";
        }
    }
}
