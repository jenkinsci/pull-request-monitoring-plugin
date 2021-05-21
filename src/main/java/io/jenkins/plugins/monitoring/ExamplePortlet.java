package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * An example of {@link MonitorPortletFactory}.
 */
public class ExamplePortlet implements MonitorPortlet {
    private final String id;
    private final String title;

    /**
     * Creates a new {@link ExamplePortlet}.
     *
     * @param title
     *          the title of the portlet.
     *
     * @param id
     *          the id of the portlet.
     */
    public ExamplePortlet(String title, String id) {
        this.id = id;
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getId() {
        return id;
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
    public Optional<String> getIconUrl() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getDetailViewUrl() {
        return Optional.empty();
    }

    /**
     * An example of {@link MonitorPortletFactory}.
     */
    @Extension
    public static class ExampleMonitorFactory implements MonitorPortletFactory {

        @Override
        public Collection<MonitorPortlet> getPortlets(Run<?, ?> build) {
            List<MonitorPortlet> portlets = new ArrayList<>();
            portlets.add(new ExamplePortlet("Good First Portlet", "first-demo-portlet"));
            portlets.add(new ExamplePortlet("Another Portlet", "second-demo-portlet"));
            return portlets;
        }

        @Override
        public String getDisplayName() {
            return "Pull Request Monitoring (Demo)";
        }
    }
}
