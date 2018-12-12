package org.izherebkin.gerrit.plugins.validation;

import com.google.common.base.Splitter;
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.inject.Inject;
import org.izherebkin.gerrit.plugins.rest.JiraClientBuilder;
import net.rcarz.jiraclient.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class JiraIssueCommitValidation extends AbstractCommitValidation {

    private static final Logger log = LoggerFactory.getLogger(JiraIssueCommitValidation.class);

    private static final Splitter SPLITTER = Splitter.on(";").trimResults().omitEmptyStrings();

    @Inject
    private ValidationModule validationModule;

    protected boolean isActive(String project) {
        return validationModule.enabled(project) &&
                (validationModule.rejectCommitIfJiraIssueNotFound(project) || StringUtils.isNotEmpty(validationModule.acceptCommitForJiraIssueTypes(project))) &&
                StringUtils.isNotEmpty(validationModule.jiraUrl(project)) &&
                StringUtils.isNotEmpty(validationModule.jiraUsername(project)) &&
                StringUtils.isNotEmpty(validationModule.jiraPassword(project));
    }

    protected List<CommitValidationMessage> validateCommit(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException {
        try {
            String commitMessage = commitReceivedEvent.commit.getFullMessage();
            if (StringUtils.isNotEmpty(commitMessage)) {
                Matcher matcher = ValidationModule.JIRA_ISSUE_KEY_PATTERN.matcher(commitMessage);
                String projectName = commitReceivedEvent.project.getName();
                JiraClient jiraClient = JiraClientBuilder
                        .params(validationModule.jiraUrl(projectName), validationModule.jiraUsername(projectName),validationModule.jiraPassword(projectName))
                        .notVerifySSLCert()
                        .build();
                String acceptIssueTypes = validationModule.acceptCommitForJiraIssueTypes(projectName);
                List<String> acceptIssueTypesList = Collections.emptyList();
                if (StringUtils.isNotEmpty(acceptIssueTypes)) {
                    acceptIssueTypesList = SPLITTER.splitToList(acceptIssueTypes);
                }
                while (matcher.find()) {
                    String issueKey = matcher.group();
                    Issue issue = jiraClient.getIssue(issueKey);
                    String issueTypeName = issue.getIssueType().getName();
                    if (!acceptIssueTypesList.isEmpty() && !acceptIssueTypesList.contains(issueTypeName)) {
                        StringBuilder message = new StringBuilder();
                        message.append("Commit ");
                        message.append(commitReceivedEvent.commit.abbreviate(7).name());
                        message.append(" contains the JIRA issue identifier whose type is not allowed; ");
                        message.append("Hint: a commit message must contain the JIRA issue identifier of the allowed JIRA issue types - ");
                        message.append(acceptIssueTypes);
                        CommitValidationMessage commitValidationMessage = new CommitValidationMessage(message.toString(), true);
                        throw new CommitValidationException("The commit message contains the JIRA issue identifier whose type is not allowed", Collections.singletonList(commitValidationMessage));
                    }
                }
            }
        } catch (CommitValidationException cve) {
            throw cve;
        } catch (Throwable th) {
            if (th instanceof JiraException && validationModule.rejectCommitIfJiraIssueNotFound(commitReceivedEvent.project.getName())) {
                if (th.getCause() instanceof RestException && ((RestException) th.getCause()).getHttpStatusCode() == 404) {
                    StringBuilder message = new StringBuilder();
                    message.append("Commit ");
                    message.append(commitReceivedEvent.commit.abbreviate(7).name());
                    message.append(" contains the JIRA issue identifier that does not exist in JIRA ITS at ");
                    message.append(validationModule.jiraUrl(commitReceivedEvent.project.getName()));
                    message.append("; ");
                    message.append("Hint: a commit message must contain the JIRA issue identifier existing in JIRA ITS.");
                    CommitValidationMessage commitValidationMessage = new CommitValidationMessage(message.toString(), true);
                    throw new CommitValidationException("The commit message contains the JIRA issue identifier that does not exist in JIRA ITS", Collections.singletonList(commitValidationMessage));
                }
            }
            log.error(ExceptionUtils.getRootCauseMessage(th), th);
        }
        return Collections.emptyList();
    }
}
