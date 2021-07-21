package io.jenkins.plugins.monitoring.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.model.Run;
import io.jenkins.plugins.monitoring.MonitorPortlet;
import io.jenkins.plugins.monitoring.MonitorPortletFactory;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A utility class for the portlets.
 */
public final class PortletUtils {
    private static final Logger LOGGER = Logger.getLogger(PortletUtils.class.getName());

    private PortletUtils() {
        // make checkstyle happy.
    }

    /**
     * Gets all {@link MonitorPortlet} for corresponding {@link MonitorPortletFactory}.
     *
     * @param build
     *          the reference build.
     *
     * @return
     *          all available {@link MonitorPortlet}.
     */
    public static List<? extends MonitorPortlet> getAvailablePortlets(final Run<?, ?> build) {
        return getFactories()
                .stream()
                .map(factory -> factory.getPortlets(build))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all portlet factories, type of {@link MonitorPortletFactory}.
     *
     * @return
     *          all factories as list.
     */
    public static List<? extends MonitorPortletFactory> getFactories() {
        return ExtensionList.lookup(MonitorPortletFactory.class);
    }

    /**
     * Gets all {@link MonitorPortlet} for one {@link MonitorPortletFactory}.
     *
     * @param build
     *         the build to get the portlets for.
     *
     * @param factory
     *         the factory to get the portlets for.
     *
     * @return
     *         the filtered portlets.
     */
    public static List<? extends MonitorPortlet> getAvailablePortletsForFactory(
            final Run<?, ?> build, final MonitorPortletFactory factory) {
        return getFactories()
                .stream()
                .filter(fac -> fac.equals(factory))
                .map(fac -> fac.getPortlets(build))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get all the default portlets as configuration.
     *
     * @param build
     *              the build to get the portlets for.
     *
     * @return
     *              the json array configuration as string.
     */
    public static String getDefaultPortletsAsConfiguration(Run<?, ?> build) {
        return new JSONArray(getAvailablePortlets(build)
                .stream()
                .filter(MonitorPortlet::isDefault)
                .map(portlet -> new JSONObject(String.format("{\"id\": \"%s\"}", portlet.getId())))
                .toArray())
                .toString();
    }

    /**
     * Validate the json configuration.
     *
     * @param configuration
     *              the configuration as json
     *
     * @return
     *              true, if the configuration is valid, else false.
     */
    public static boolean isValidConfiguration(@NonNull final String configuration) {
        try (InputStream schemaStream = PortletUtils.class.getResourceAsStream("/schema.json")) {
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaStream));
            JSONArray jsonSubject = new JSONArray(configuration);
            Schema schema = SchemaLoader.load(jsonSchema);
            schema.validate(jsonSubject);
            return true;
        }
        catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Invalid Configuration found: ", exception);
            return false;
        }
    }
}
