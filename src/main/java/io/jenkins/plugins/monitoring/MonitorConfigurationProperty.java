package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Saveable;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;

import java.io.IOException;
import java.util.*;

/**
 * A {@link UserProperty} to store the json configuration per user as property.
 *
 * @author Simon Symhoven
 */
public class MonitorConfigurationProperty extends UserProperty implements Saveable {

    private final Collection<MonitorConfiguration> configurations;

    /**
     * The id for the default configuration.
     */
    public static final String DEFAULT_ID = "default";

    /**
     * Creates a new {@link MonitorConfigurationProperty}.
     *
     * @param configurations
     *              the list of configurations to add to the {@link MonitorConfigurationProperty}.
     */
    public MonitorConfigurationProperty(List<MonitorConfiguration> configurations) {
        this.configurations = configurations;
    }

    public Collection<MonitorConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Get a {@link MonitorConfiguration} by its id.
     *
     * @param id
     *          the id of the {@link MonitorConfiguration} to get.
     *
     * @return
     *          the {@link MonitorConfiguration} or default if id does not exist on {@link MonitorConfigurationProperty}.
     */
    public MonitorConfiguration getConfiguration(String id) {
        return getConfigurations().stream()
                .filter(monitorConfiguration -> monitorConfiguration.getId().equals(id))
                .findFirst()
                .orElse(getConfigurations()
                        .stream()
                        .filter(monitorConfiguration -> monitorConfiguration.getId().equals(DEFAULT_ID))
                        .findFirst().get());
    }

    /**
     * Updates an existing {@link MonitorConfiguration}.
     *
     * @param id
     *          the id of the {@link MonitorConfiguration} to update.
     *
     * @param config
     *          the config string to update.
     */
    public void createOrUpdateConfiguration(String id, String config) {
        MonitorConfiguration property = getConfigurations().stream()
                .filter(monitorConfiguration -> monitorConfiguration.getId().equals(id))
                .findFirst().orElse(null);

        if (property == null) {
            getConfigurations().add(new MonitorConfiguration(id, config));
        }
        else {
            property.setConfig(config);
        }

        try {
            save();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a configuration from configurations list.
     *
     * @param id
     *              the id of configuration to remove.
     *
     */
    public void removeConfiguration(String id) {
        getConfigurations().remove(getConfiguration(id));

        try {
            save();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the {@link MonitorConfigurationProperty} for current user.
     *
     * @return
     *          the {@link MonitorConfigurationProperty} as {@link Optional}.
     */
    public static Optional<MonitorConfigurationProperty> forCurrentUser() {
        final User current = User.current();
        return current == null ? Optional.empty() : Optional.of(current.getProperty(MonitorConfigurationProperty.class));
    }

    @Override
    public void save() throws IOException {
        user.save();
    }

    /**
     * The property class to store. Each {@link MonitorConfiguration} has an id and a config (json string).
     */
    public static class MonitorConfiguration {

        private final String id;
        private String config;

        /**
         * Creates a {@link MonitorConfiguration}.
         *
         * @param id
         *          the id of the {@link MonitorConfiguration}.
         *
         * @param config
         *          the config of the {@link MonitorConfiguration}.
         */
        public MonitorConfiguration(String id, String config) {
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
     * A {@link UserPropertyDescriptor} for the {@link MonitorConfigurationProperty}.
     */
    @Extension
    public static class MonitorPropertyDescriptor extends UserPropertyDescriptor {

        @Override
        public UserProperty newInstance(User user) {
            return new MonitorConfigurationProperty(new ArrayList<>());
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Pull Request Monitoring";
        }

    }

}
