package org.izherebkin.gerrit.plugins.validation;

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

public class JiraIssueKeyFormatCommitValidation extends AbstractCommitValidation {

    private static final Logger log = LoggerFactory.getLogger(JiraIssueKeyFormatCommitValidation.class);

    @Inject
    private ValidationModule validationModule;

    protected boolean isActive(String project) {
        return validationModule.enabled(project) && validationModule.rejectCommitWithoutJiraIssueKeyFormat(project);
    }

    protected List<CommitValidationMessage> validateCommit(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException {
        try {
            String commitMessage = commitReceivedEvent.commit.getFullMessage();
            if (StringUtils.isEmpty(commitMessage) || !ValidationModule.JIRA_ISSUE_KEY_FORMAT_PATTERN.matcher(commitMessage).matches()) {
                StringBuilder message = new StringBuilder();
                message.append("Commit ");
                message.append(commitReceivedEvent.commit.abbreviate(7).name());
                message.append(" does not match the correct format; ");
                message.append("Hint: the correct commit message format is the JIRA issue identifier, any whitespace character, the JIRA issue summary.");
                CommitValidationMessage commitValidationMessage = new CommitValidationMessage(message.toString(), true);
                throw new CommitValidationException("The commit message does not match the correct format", Collections.singletonList(commitValidationMessage));
            }
        } catch (CommitValidationException cve) {
            throw cve;
        } catch (Throwable th) {
            log.error(ExceptionUtils.getRootCauseMessage(th), th);
        }
        return Collections.emptyList();
    }
}
