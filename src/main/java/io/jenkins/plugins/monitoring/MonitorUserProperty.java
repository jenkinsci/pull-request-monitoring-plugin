package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link UserProperty} to store the json configuration per user as property.
 *
 * @author simonsymhoven
 */
public class MonitorUserProperty extends UserProperty {

    private final Collection<MonitorProperty> properties;

    /**
     * Creates a new {@link MonitorUserProperty}.
     */
    public MonitorUserProperty() {
        this.properties = new ArrayList<>();
    }

    public Collection<MonitorProperty> getProperties() {
        return properties;
    }

    /**
     * Get a {@link MonitorProperty} by its id.
     *
     * @param id
     *          the id of the {@link MonitorProperty} to get.
     *
     * @return
     *          the {@link MonitorProperty} or null if id does not exist on {@link MonitorUserProperty}.
     */
    public MonitorProperty getProperty(String id) {
        return this.getProperties().stream()
                .filter(monitorProperty -> monitorProperty.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * Updates an existing {@link MonitorProperty}.
     *
     * @param id
     *          the id of the {@link MonitorProperty} to update.
     *
     * @param config
     *          the config string to update.
     */
    public void update(String id, String config) {
        MonitorProperty property = this.getProperties().stream()
                .filter(monitorProperty -> monitorProperty.getId().equals(id))
                .findFirst().orElse(null);

        if (property == null) {
            this.getProperties().add(new MonitorProperty(id, config));
        }
        else {
            property.setConfig(config);
        }
    }

    /**
     * The property class to store. Each {@link MonitorProperty} has an id and a config (json string).
     */
    public static class MonitorProperty {

        private final String id;
        private String config;

        /**
         * Creates a {@link MonitorProperty}.
         *
         * @param id
         *          the id of the {@link MonitorProperty}.
         *
         * @param config
         *          the config of the {@link MonitorProperty}.
         */
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

    /**
     * A {@link UserPropertyDescriptor} for the {@link MonitorUserProperty}.
     */
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
