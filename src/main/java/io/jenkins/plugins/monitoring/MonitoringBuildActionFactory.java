package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link TransientActionFactory} to add an action to specific {@link Run}.
 *
 * @author Simon Symhoven
 */
@Extension
public class MonitoringBuildActionFactory extends TransientActionFactory<Run> {


    @Override
    public Class<Run> type() {
        return Run.class;
    }

    /**
     * Add the action to the selected {@link Run} if its parent {@link hudson.model.Job} is a Pull Request.
     *
     * @param run
     *          the run to add the action to.
     * @return
     *          {@link Collections} of {@link MonitoringBuildAction} if the parent {@link hudson.model.Job} of the
     *          run is a Pull Request, else a empty collection.
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Run run) {
        if (run.getParent().getPronoun().equals("Pull Request")) {
            return Collections.singletonList(new MonitoringBuildAction(run));
        }

        return Collections.emptyList();
    }
}
