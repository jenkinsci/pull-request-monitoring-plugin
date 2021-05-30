package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.Action;
import hudson.model.Job;
import io.jenkins.plugins.monitoring.util.PortletService;
import io.jenkins.plugins.monitoring.util.PullRequestFinder;
import jenkins.model.TransientActionFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link TransientActionFactory} to add an action to specific {@link Run}.
 *
 * @author Simon Symhoven
 */
@Extension
public class MonitoringDefaultActionFactory extends TransientActionFactory<Run> {
    @Override
    public Class<Run> type() {
        return Run.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull final Run run) {

        if (run.isBuilding()) {
            return Collections.emptyList();
        }

        final Job<?, ?> job = run.getParent();

        if (PullRequestFinder.isPullRequest(job)) {
            Monitor monitor = new Monitor(PortletService.getDefaultPortletsAsConfiguration(run));
            return Collections.singletonList(new MonitoringDefaultAction(run, monitor));
        }

        return Collections.emptyList();
    }
}
