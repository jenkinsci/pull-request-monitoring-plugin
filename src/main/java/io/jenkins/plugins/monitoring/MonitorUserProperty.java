package io.jenkins.plugins.monitoring;

import edu.hm.hafner.echarts.JacksonFacade;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A {@link hudson.model.UserProperty} to store the json configuration per user as property.
 */
public class MonitorUserProperty extends hudson.model.UserProperty {

    private Collection<MonitorProperty> properties;

    public MonitorUserProperty() {

    }

    @DataBoundConstructor
    public MonitorUserProperty(MonitorProperty defaultProp) {
        this.properties = new ArrayList<>();
        this.properties.add(defaultProp);
    }

    public Collection<MonitorProperty> getProperties() {
        return properties;
    }

    public MonitorProperty getProperty(String id) {
        return this.getProperties().stream()
                .filter(monitorProperty -> monitorProperty.getId().equals(id))
                .findFirst().orElse(null);
    }

    public void setProperties(Collection<MonitorProperty> properties) {
        this.properties = properties;
    }

    public void createOrUpdate(String id, String config) {
        MonitorProperty property = this.getProperties().stream()
                .filter(monitorProperty -> monitorProperty.getId().equals(id))
                .findFirst().orElse(null);

        if (property == null) {
            this.getProperties().add(new MonitorProperty(id, config));
        } else {
            property.setConfig(config);
        }
    }

    public static class MonitorProperty {

        private final String id;
        private String config;

        public MonitorProperty(String id, String config) {
            this.id = id;
            this.config = config;
        }

        public String getId() {
            return id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

    }

    @Extension
    public static class MonitorPropertyDescriptor extends UserPropertyDescriptor {

        @Override
        public UserProperty newInstance(User user) {
            return new MonitorUserProperty();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Pull Request Monitoring";
        }

    }

}
