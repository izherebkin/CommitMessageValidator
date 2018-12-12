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
import java.util.regex.Matcher;

public class MultipleJiraIssueKeysCommitValidation extends AbstractCommitValidation {

    private static final Logger log = LoggerFactory.getLogger(MultipleJiraIssueKeysCommitValidation.class);

    @Inject
    private ValidationModule validationModule;

    protected boolean isActive(String project) {
        return validationModule.enabled(project) && validationModule.rejectCommitWithMultipleJiraIssueKeys(project);
    }

    protected List<CommitValidationMessage> validateCommit(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException {
        try {
            String commitMessage = commitReceivedEvent.commit.getFullMessage();
            if (StringUtils.isNotEmpty(commitMessage)) {
                Matcher matcher = ValidationModule.JIRA_ISSUE_KEY_PATTERN.matcher(commitMessage);
                int matchCount = 0;
                while (matcher.find()) {
                    matchCount++;
                    if (matchCount > 1) {
                        StringBuilder message = new StringBuilder();
                        message.append("Commit ");
                        message.append(commitReceivedEvent.commit.abbreviate(7).name());
                        message.append(" contains several JIRA issue identifiers; ");
                        message.append("Hint: a commit message must contain only one JIRA issue identifier.");
                        CommitValidationMessage commitValidationMessage = new CommitValidationMessage(message.toString(), true);
                        throw new CommitValidationException("The commit message contains several JIRA issue identifiers", Collections.singletonList(commitValidationMessage));
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
