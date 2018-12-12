package org.izherebkin.gerrit.plugins.validation;

import com.google.common.base.Splitter;
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class JiraIssueKeyFromProjectsCommitValidation extends AbstractCommitValidation {

    private static final Logger log = LoggerFactory.getLogger(JiraIssueKeyFromProjectsCommitValidation.class);

    private static final Splitter SPLITTER = Splitter.on(";").trimResults().omitEmptyStrings();

    @Inject
    private ValidationModule validationModule;

    protected boolean isActive(String project) {
        return validationModule.enabled(project) && StringUtils.isNotEmpty(validationModule.rejectCommitIfJiraIssueKeyFromProjects(project));
    }

    protected List<CommitValidationMessage> validateCommit(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException {
        try {
            String commitMessage = commitReceivedEvent.commit.getFullMessage();
            if (StringUtils.isNotEmpty(commitMessage)) {
                Matcher matcher = ValidationModule.JIRA_ISSUE_KEY_PATTERN.matcher(commitMessage);
                String rejectProjects = validationModule.rejectCommitIfJiraIssueKeyFromProjects(commitReceivedEvent.project.getName());
                List<String> rejectProjectsList = SPLITTER.splitToList(rejectProjects);
                while (matcher.find()) {
                    String issueKey = matcher.group();
                    for (String rejectProject : rejectProjectsList) {
                        if (issueKey.startsWith(rejectProject)) {
                            StringBuilder message = new StringBuilder();
                            message.append("Commit ");
                            message.append(commitReceivedEvent.commit.abbreviate(7).name());
                            message.append(" contains the JIRA issue identifier of prohibited JIRA project identifiers; ");
                            message.append("Hint: a commit message must not contain the JIRA issue identifier for prohibited JIRA project identifiers - ");
                            message.append(rejectProjects);
                            CommitValidationMessage commitValidationMessage = new CommitValidationMessage(message.toString(), true);
                            throw new CommitValidationException("The commit message contains the JIRA issue identifier of the prohibited JIRA project identifiers", Collections.singletonList(commitValidationMessage));
                        }
                    }
                }
            }
        } catch (CommitValidationException cve) {
            throw cve;
        } catch (Throwable th) {
            log.error(ExceptionUtils.getRootCauseMessage(th), th);
        }
        return Collections.emptyList();
    }
}
