/*
 * Based on code in https://bitbucket.org/atlassian/jira-testkit
 * which is licenced under Apache 2.0
 */
package com.corefiling.jira.plugins.workflowdata;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Iterables;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Used to expose data on configured workflows and schemes.
 */
@Produces ({MediaType.APPLICATION_JSON})
@Path ("/workflowschemes")
public class WorkflowSchemeResource
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext context;
    private final WorkflowSchemeDataFactoryImpl dataFactory;

    public WorkflowSchemeResource(WorkflowSchemeManager workflowSchemeManager,
                                   ProjectManager projectManager,
                                   JiraAuthenticationContext context,
                                   WorkflowSchemeDataFactoryImpl dataFactory)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectManager = projectManager;
        this.context = context;
        this.dataFactory = dataFactory;
    }

    @GET
    @AnonymousAllowed
    public Response getWorkflowScheme(@QueryParam("schemeName") String schemeName,
           @QueryParam ("projectKey") String projectKey, @QueryParam ("projectName") String projectName,
           @QueryParam ("draft") boolean getDraft)
    {
        schemeName = stripToNull(schemeName);
        if (schemeName != null)
        {
            AssignableWorkflowScheme workflowSchemeObj = workflowSchemeManager.getWorkflowSchemeObj(schemeName);
            if (workflowSchemeObj != null)
            {
                return ok(workflowSchemeObj);
            }
            else
            {
                return fourOhfour();
            }
        }

        projectKey = stripToNull(projectKey);
        if (projectKey == null)
        {
            projectName = stripToNull(projectName);
            if (projectName == null)
            {
                return getAllSchemes();
            }
            else
            {
                return schemeForProject(projectManager.getProjectObjByName(projectName), getDraft);
            }
        }
        else
        {
            return schemeForProject(projectManager.getProjectObjByKey(projectKey), getDraft);
        }
    }

    @Path("{id}")
    @GET
    @AnonymousAllowed
    public Response getWorkflowScheme(@PathParam("id") long id)
    {
        final AssignableWorkflowScheme workflowSchemeObj = workflowSchemeManager.getWorkflowSchemeObj(id);
        if (workflowSchemeObj == null)
        {
            return fourOhfour();
        }
        else
        {
            return Response.ok(dataFactory.toData(workflowSchemeObj)).cacheControl(CacheControl.never()).build();
        }
    }

    private static Response fourOhfour()
    {
        return Response.status(Response.Status.NOT_FOUND).cacheControl(CacheControl.never()).build();
    }

    private Response schemeForProject(Project project, boolean getDraft)
    {
        if (project == null)
        {
            return fourOhfour();
        }

        final AssignableWorkflowScheme projectScheme = workflowSchemeManager.getWorkflowSchemeObj(project);
        final WorkflowScheme scheme = getDraft ? workflowSchemeManager.getDraftForParent(projectScheme) : projectScheme;

        if (scheme == null)
        {
            return fourOhfour();
        }

        return ok(scheme);
    }

    private Response ok(WorkflowScheme scheme)
    {
        return Response.ok(dataFactory.toData(scheme))
                .cacheControl(CacheControl.never()).build();
    }

    private Response getAllSchemes()
    {
        Iterable<WorkflowSchemeData> schemeObjects = Iterables.transform(workflowSchemeManager.getAssignableSchemes(), dataFactory.fromSchemeToDataFunction());
        return Response.ok(schemeObjects).cacheControl(CacheControl.never()).build();
    }
}
