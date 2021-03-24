package io.jenkins.plugins.monitoring;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

@Extension
public class MonitoringBuildActionFactory extends TransientActionFactory<Run> {

    @Override
    public Class<Run> type() {
        return Run.class;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Collection<? extends Action> createFor(@Nonnull Run run) {
        return Collections.singletonList(new MonitoringBuildAction(run));
    }
}
