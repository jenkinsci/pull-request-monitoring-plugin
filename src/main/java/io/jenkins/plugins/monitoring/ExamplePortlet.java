package io.jenkins.plugins.monitoring;

import java.util.Optional;

public class ExamplePortlet implements MonitorPortlet {
    @Override
    public String getTitle() {
        return "Example Portlet";
    }

    @Override
    public int getPreferredWidth() {
        return 300;
    }

    @Override
    public int getPreferredHeight() {
        return 200;
    }

    @Override
    public String getIconUrl() {
        return "</path-to-icon/icon.png>";
    }

    @Override
    public Optional<String> getDetailViewUrl() {
        return Optional.of("<link-to-detail-view>");
    }
}
