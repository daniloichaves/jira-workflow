/*
 * Taken from https://bitbucket.org/atlassian/jira-testkit
 * which is licenced under Apache 2.0
 */
package com.corefiling.jira.plugins.workflowdata;

import com.atlassian.jira.workflow.WorkflowScheme;

public interface WorkflowSchemeDataFactory
{
    WorkflowSchemeData toData(WorkflowScheme scheme);
}
