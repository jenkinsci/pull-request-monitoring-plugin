package io.jenkins.plugins.monitoring;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.TransientActionFactory;

import java.util.Collection;
import java.util.Collections;

@Extension
public class MonitoringProjectActionFactory extends TransientActionFactory<Project> {

    @Override
    public Class<Project> type() {
        return Project.class;
    }

    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull Project project) {
        return Collections.singletonList(new MonitoringProjectAction(project));
    }
}
