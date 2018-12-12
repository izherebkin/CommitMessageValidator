package org.izherebkin.gerrit.plugins.validation;

import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.ProjectConfigEntry;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.izherebkin.gerrit.plugins.Module;

import java.util.regex.Pattern;

public class ValidationModule extends Module {

    private static final String REJECT_COMMIT_WITHOUT_JIRA_ISSUE_KEY_FORMAT = "rejectCommitWithoutJiraIssueKeyFormat";
    private static final String REJECT_COMMIT_WITH_MULTIPLE_JIRA_ISSUE_KEYS = "rejectCommitWithMultipleJiraIssueKeys";
    private static final String REJECT_COMMIT_IF_JIRA_ISSUE_KEY_FROM_PROJECTS = "rejectCommitIfJiraIssueKeyFromProjects";
    private static final String REJECT_COMMIT_IF_JIRA_ISSUE_NOT_FOUND = "rejectCommitIfJiraIssueNotFound";
    private static final String ACCEPT_COMMIT_FOR_JIRA_ISSUE_TYPES = "acceptCommitForJiraIssueTypes";

    public static final Pattern JIRA_ISSUE_KEY_FORMAT_PATTERN = Pattern.compile("^[A-Z]+-[0-9]+\\s[\\s\\S]+");
    public static final Pattern JIRA_ISSUE_KEY_PATTERN = Pattern.compile("[A-Z]+-[0-9]+");

    @Inject
    public ValidationModule(@PluginName String pluginName, PluginConfigFactory pluginCfgFactory, ProjectCache projectCache) {
        super(pluginName, pluginCfgFactory, projectCache);
    }

    @Override
    protected void configure() {
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(REJECT_COMMIT_WITHOUT_JIRA_ISSUE_KEY_FORMAT))
                .toInstance(
                        new ProjectConfigEntry(
                                "Reject a commit without the correct format of registration?",
                                false,
                                "Failure of a commit that does not contain the correct format for the message."
                        )
                );
        DynamicSet.bind(binder(), CommitValidationListener.class).to(JiraIssueKeyFormatCommitValidation.class).in(Singleton.class);

        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(REJECT_COMMIT_WITH_MULTIPLE_JIRA_ISSUE_KEYS))
                .toInstance(
                        new ProjectConfigEntry(
                                "Reject a commit with multiple JIRA issue identifiers?",
                                false,
                                "Failure of a commit that contains multiple JIRA issue identifiers in a message."
                        )
                );
        DynamicSet.bind(binder(), CommitValidationListener.class).to(MultipleJiraIssueKeysCommitValidation.class).in(Singleton.class);

        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(REJECT_COMMIT_IF_JIRA_ISSUE_KEY_FROM_PROJECTS))
                .toInstance(
                        new ProjectConfigEntry(
                                "List of prohibited JIRA project identifiers",
                                "",
                                false,
                                "The failure of a commit that contains in a message a JIRA issue identifier that is related to prohibited JIRA project identifiers. Delimiter - ';'"
                        )
                );
        DynamicSet.bind(binder(), CommitValidationListener.class).to(JiraIssueKeyFromProjectsCommitValidation.class).in(Singleton.class);

        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(REJECT_COMMIT_IF_JIRA_ISSUE_NOT_FOUND))
                .toInstance(
                        new ProjectConfigEntry(
                                "Reject a commit with an issue identifier that doesn't exist in JIRA?",
                                false,
                                "The failure of a commit that contains in a message an JIRA issue identifier that does not exist in JIRA ITS."
                        )
                );
        bind(ProjectConfigEntry.class)
                .annotatedWith(Exports.named(ACCEPT_COMMIT_FOR_JIRA_ISSUE_TYPES))
                .toInstance(
                        new ProjectConfigEntry(
                                "List of allowed JIRA issue types",
                                "",
                                false,
                                "Accept a commit that contains in a message a JIRA issue identifier that relates to the allowed JIRA issue types. Delimiter - ';'"
                        )
                );
        DynamicSet.bind(binder(), CommitValidationListener.class).to(JiraIssueCommitValidation.class).in(Singleton.class);
    }

    public boolean rejectCommitWithoutJiraIssueKeyFormat(String project) {
        ProjectState projectState = getProjectState(project);
        return projectState != null && pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getBoolean(REJECT_COMMIT_WITHOUT_JIRA_ISSUE_KEY_FORMAT, false);
    }

    public boolean rejectCommitWithMultipleJiraIssueKeys(String project) {
        ProjectState projectState = getProjectState(project);
        return projectState != null && pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getBoolean(REJECT_COMMIT_WITH_MULTIPLE_JIRA_ISSUE_KEYS, false);
    }

    public String rejectCommitIfJiraIssueKeyFromProjects(String project) {
        ProjectState projectState = getProjectState(project);
        if (projectState != null) {
            return pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getString(REJECT_COMMIT_IF_JIRA_ISSUE_KEY_FROM_PROJECTS, null);
        }
        return null;
    }

    public boolean rejectCommitIfJiraIssueNotFound(String project) {
        ProjectState projectState = getProjectState(project);
        return projectState != null && pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getBoolean(REJECT_COMMIT_IF_JIRA_ISSUE_NOT_FOUND, false);
    }

    public String acceptCommitForJiraIssueTypes(String project) {
        ProjectState projectState = getProjectState(project);
        if (projectState != null) {
            return pluginCfgFactory.getFromProjectConfig(projectState, pluginName).getString(ACCEPT_COMMIT_FOR_JIRA_ISSUE_TYPES, null);
        }
        return null;
    }
}
