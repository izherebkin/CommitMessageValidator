package org.izherebkin.gerrit.plugins;

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.ProjectConfigEntry;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import org.izherebkin.gerrit.plugins.validation.ValidationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Module extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(Module.class);

    private static final String ENABLED = "enabled";
    private static final String JIRA_URL = "jiraUrl";
    private static final String JIRA_USERNAME = "jiraUsername";
    private static final String JIRA_PASSWORD = "jiraPassword";

    protected final String pluginName;
    protected final PluginConfigFactory pluginCfgFactory;
    private final ProjectCache projectCache;

    @Inject
    public Module(@PluginName String pluginName, PluginConfigFactory pluginCfgFactory, ProjectCache projectCache) {
        this.pluginName = pluginName;
        this.pluginCfgFactory = pluginCfgFactory;
        this.projectCache = projectCache;
    }

    @Override
    protected void configure() {
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(ENABLED))
                .toInstance(
                        new ProjectConfigEntry(
                                "Activate the plugin?",
                                false,
                                "Plugin activation. Parameter to enable or disable the plugin."
                        )
                );
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(JIRA_URL))
                .toInstance(
                        new ProjectConfigEntry(
                                "JIRA URL",
                                "",
                                false,
                                "JIRA ITS URL. Must start with http:// or https://."
                        )
                );
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(JIRA_USERNAME))
                .toInstance(
                        new ProjectConfigEntry(
                                "JIRA Username",
                                "",
                                false,
                                "JIRA ITS Username."
                        )
                );
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(JIRA_PASSWORD))
                .toInstance(
                        new ProjectConfigEntry(
                                "JIRA Password",
                                "",
                                false,
                                "JIRA ITS Password."
                        )
                );

        install(new ValidationModule(pluginName, pluginCfgFactory, projectCache));
    }


    public boolean enabled(String project) {
        ProjectState projectState = getProjectState(project);
        return projectState != null && pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getBoolean(ENABLED, false);
    }

    public String jiraUrl(String project) {
        ProjectState projectState = getProjectState(project);
        if (projectState != null) {
            return pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getString(JIRA_URL, null);
        }
        return null;
    }

    public String jiraUsername(String project) {
        ProjectState projectState = getProjectState(project);
        if (projectState != null) {
            return pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getString(JIRA_USERNAME, null);
        }
        return null;
    }

    public String jiraPassword(String project) {
        ProjectState projectState = getProjectState(project);
        if (projectState != null) {
            return pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getString(JIRA_PASSWORD, null);
        }
        return null;
    }

    protected final ProjectState getProjectState(String project) {
        ProjectState projectState = projectCache.get(new Project.NameKey(project));
        if (projectState == null) {
            log.error("Failed to check if " + pluginName + " is enabled for project " + project + ": Project " + project + " not found");
        }
        return projectState;
    }
}
