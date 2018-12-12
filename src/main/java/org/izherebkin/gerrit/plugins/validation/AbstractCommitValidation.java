package org.izherebkin.gerrit.plugins.validation;

import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidationMessage;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCommitValidation implements CommitValidationListener {

    @Override
    public final List<CommitValidationMessage> onCommitReceived(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException {
        if (isActive(commitReceivedEvent.project.getName())) {
            return validateCommit(commitReceivedEvent);
        }
        return Collections.emptyList();
    }

    protected abstract boolean isActive(String project);

    protected abstract List<CommitValidationMessage> validateCommit(CommitReceivedEvent commitReceivedEvent) throws CommitValidationException;
}
