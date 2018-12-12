# Commit Message Validator
Commit Message Validator is a commit message validator plugin for Gerrit Code Review.

The plugin allows you to configure the validation of the commit message at the stage of the push procedure in the GIT. If the commit fails validation, a message will be returned indicating the reason for the failure. In the Gerrit Code Review, respectively, the refused commit will not fall.

## Build
Build a plugin using Maven:

    mvn clean package assembly:single -Dhttps.protocols=TLSv1.2

After a successful build, the jar-file with dependencies will be located in the `target` directory.

## Installation
Plugin installation is as easy as dropping the plugin jar into the `$gerrit_root/plugins/` folder. It may take [a few minutes](https://gerrit-review.googlesource.com/Documentation/config-gerrit.html#plugins.checkFrequency) until the server picks up new and updated plugins.

## Configuration
The configuration of the plugin is done on project level in the `project.config` file of the project. Project owners can do the configuration in the Gerrit web UI from project info screen.

      [plugin "commit-message-validator"]
        enabled = false
        jiraUrl = jiraUrl
        jiraUsername = jiraUsername
        jiraPassword = jiraPassword
        rejectCommitWithoutJiraIssueKeyFormat = false
        rejectCommitWithMultipleJiraIssueKeys = false
        rejectCommitIfJiraIssueKeyFromProjects = PRJ1;PRJ2
        rejectCommitIfJiraIssueNotFound = false;
        acceptCommitForJiraIssueTypes = Component improvement;Bug

**None of the parameters are inherited by child projects.**

plugin.commit-message-validator.enabled : Activate the plugin?

    Plugin activation. Parameter to enable or disable the plugin.
    The default value is false.

plugin.commit-message-validator.jiraUrl : JIRA URL.

    JIRA ITS URL. Must start with http:// or https://.
    The default value is empty.

plugin.commit-message-validator.jiraUsername : JIRA Username.

    JIRA ITS Username.
    The default value is empty.

plugin.commit-message-validator.jiraPassword : JIRA Password.

    JIRA ITS Password.
    The default value is empty.

plugin.commit-message-validator.rejectCommitWithoutJiraIssueKeyFormat : Reject a commit without the correct format of registration?

    Failure of a commit that does not contain the correct format for the message.
    The default value is false.

plugin.commit-message-validator.rejectCommitWithMultipleJiraIssueKeys : Reject a commit with multiple JIRA issue identifiers?

    Failure of a commit that contains multiple JIRA issue identifiers in a message.
    The default value is false.

plugin.commit-message-validator.rejectCommitIfJiraIssueKeyFromProjects : List of prohibited JIRA project identifiers.

    The failure of a commit that contains in a message a JIRA issue identifier that is related to prohibited JIRA project identifiers. Delimiter is `;`.
    The default value is empty.

plugin.commit-message-validator.rejectCommitIfJiraIssueNotFound : Reject a commit with an issue identifier that doesn't exist in JIRA?

    The failure of a commit that contains in a message an JIRA issue identifier that does not exist in JIRA ITS.
    The default value is false.

plugin.commit-message-validator.acceptCommitForJiraIssueTypes : List of allowed JIRA issue types

    
    Accept a commit that contains in a message a JIRA issue identifier that relates to the allowed JIRA issue types. Delimiter is `;`.
    The default value is empty. This means the check will not be executed.

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```