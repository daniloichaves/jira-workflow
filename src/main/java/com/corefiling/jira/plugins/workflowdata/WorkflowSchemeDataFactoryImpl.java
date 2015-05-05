/*
 * Based on code in  https://bitbucket.org/atlassian/jira-testkit
 * which is licenced under Apache 2.0
 */
package com.corefiling.jira.plugins.workflowdata;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.corefiling.jira.plugins.workflowdata.util.DescriptorUtil;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.loader.ValidatorDescriptor;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class WorkflowSchemeDataFactoryImpl implements WorkflowSchemeDataFactory
{
    private final IssueTypeManager issueTypeManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final DateTimeFormatter formatter;

    private static final Logger LOG = LoggerFactory.getLogger("atlassian.plugin");

    WorkflowSchemeDataFactoryImpl(IssueTypeManager issueTypeManager, WorkflowSchemeManager workflowSchemeManager,
            DateTimeFormatter formatter)
    {
        this.issueTypeManager = issueTypeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.formatter = formatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).withLocale(Locale.ENGLISH);
    }

    @Override
    public WorkflowSchemeData toData(WorkflowScheme scheme)
    {
        String defaultWorkflow = null;
        Map<String, String> map = Maps.newHashMap();
        for (Map.Entry<String, String> entry : scheme.getMappings().entrySet())
        {
            String key = entry.getKey();
            if (key != null)
            {
                key = issueTypeManager.getIssueType(key).getName();
                map.put(key, entry.getValue());
            }
            else
            {
                defaultWorkflow = entry.getValue();
            }
        }

        List<WorkflowData> workflows = Lists.newArrayList();
        try
        {
            LOG.info("finding workflows");
            for (JiraWorkflow workflow : ComponentAccessor.getWorkflowManager().getWorkflowsFromScheme(workflowSchemeManager.getScheme(scheme.getId())))
            {
                LOG.info("Found workflow: " + workflow.getName());
                workflows.add(new WorkflowData().setName(workflow.getName()));
            }
        }
        catch (GenericEntityException e)
        {
            LOG.warn("Failed to get Workflows: " + e.toString());
        }
        final WorkflowSchemeData data = new WorkflowSchemeData().setId(scheme.getId())
                .setName(scheme.getName()).setDescription(scheme.getDescription())
                .setMappings(map).setDefaultWorkflow(defaultWorkflow).setDraft(scheme.isDraft())
                .setActive(workflowSchemeManager.isActive(scheme)).setWorkflows(workflows);

        if (scheme instanceof DraftWorkflowScheme)
        {
            DraftWorkflowScheme draftWorkflowScheme = (DraftWorkflowScheme) scheme;
            data.setLastModified(formatter.format(draftWorkflowScheme.getLastModifiedDate()));
            data.setLastModifiedUser(draftWorkflowScheme.getLastModifiedUser().getName());
        }
        return data;
    }

    @Override
    public WorkflowSchemeData toData(Scheme scheme)
    {
        Map<String, String> mappings = Maps.newHashMap();
        Collection<SchemeEntity> entities = scheme.getEntities();
        String defaultWorkflow =null;
        for (SchemeEntity entity : entities)
        {
            String parameter = entity.getParameter();
            if (parameter == null || "0".equals(parameter))
            {
                defaultWorkflow = entity.getEntityTypeId().toString();
            }
            else
            {
                IssueType object = issueTypeManager.getIssueType(parameter);
                mappings.put(object.getName(), entity.getEntityTypeId().toString());
            }
        }
        if (defaultWorkflow == null)
        {
            defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
        }

        List<WorkflowData> workflows = Lists.newArrayList();
        LOG.info("finding workflows scheme");
        for (JiraWorkflow workflow : ComponentAccessor.getWorkflowManager().getWorkflowsFromScheme(scheme))
        {
            List<StatusData> statusData = Lists.newArrayList();
            for (Status s : workflow.getLinkedStatusObjects())
            {
                StepDescriptor stepDescriptor = workflow.getLinkedStep(s);
                StatusData statusDatum = new StatusData().setName(s.getName())
                                                        .setStepName(stepDescriptor.getName());
                List<TransitionData> transitions = Lists.newArrayList();
                for (ActionDescriptor a : workflow.getActionsWithResult(stepDescriptor))
                {
                    TransitionData transition = new TransitionData().setName(a.getName());
                    List<FunctionData> functions = Lists.newArrayList();
                    for (FunctionDescriptor func : workflow.getPostFunctionsForTransition(a))
                    {
                        FunctionData function = new FunctionData();
                        String className = (String) func.getArgs().get("class.name");
                        if (className != null)
                        {
                            function.setClassName(className);
                        }
                        else
                        {
                            function.setClassName("UNKNOWN");
                        }
                        functions.add(function);
                    }
                    transition.setFunctions(functions);
                    List<ConditionData> conditions = Lists.newArrayList();
                    for (ConditionDescriptor condition : DescriptorUtil.getConditionsForTransition(a))
                    {
                        ConditionData conditionData = new ConditionData();
                        String className = (String) condition.getArgs().get("class.name");
                        if (className != null)
                        {
                            conditionData.setClassName(className);
                        }
                        else
                        {
                            conditionData.setClassName("UNKNOWN");
                        }
                        conditions.add(conditionData);
                    }
                    transition.setConditions(conditions);
                    transitions.add(transition);
                }
                statusDatum.setTransitions(transitions);
                statusData.add(statusDatum);
            }

            LOG.info("Found workflow: " + workflow.getName());
            WorkflowData workflowData = new WorkflowData().setName(workflow.getName())
                                            .setActive(workflow.isActive())
                                            .setIsDefault(workflow.isDefault())
                                            .setStates(statusData);
            if (workflow.getUpdatedDate() != null)
            {
                workflowData.setLastModified(formatter.format(workflow.getUpdatedDate()));
            }
            if (workflow.getUpdateAuthor() != null)
            {
               workflowData.setLastModifiedBy(workflow.getUpdateAuthor().getEmailAddress());
            }
            workflows.add(workflowData);
        }

        return new WorkflowSchemeData().setId(scheme.getId())
                .setName(scheme.getName()).setDescription(scheme.getDescription())
                .setMappings(mappings).setDefaultWorkflow(defaultWorkflow).setDraft(false)
                .setActive(!workflowSchemeManager.getProjects(scheme).isEmpty()).setWorkflows(workflows);
    }

    Function<Scheme, WorkflowSchemeData> fromSchemeToDataFunction()
    {
        return new Function<Scheme, WorkflowSchemeData>()
        {
            @Override
            public WorkflowSchemeData apply(Scheme scheme) {
                return toData(scheme);
            }
        };
    }

    AssignableWorkflowScheme schemeFromData(WorkflowSchemeData data, AssignableWorkflowScheme.Builder current)
    {
        AssignableWorkflowScheme.Builder builder = current.setName(data.getName()).setDescription(data.getDescription());
        setMappings(data, current);
        return builder.build();
    }

    DraftWorkflowScheme draftFromData(WorkflowSchemeData data, DraftWorkflowScheme current)
    {
        DraftWorkflowScheme.Builder builder = current.builder().clearMappings();
        setMappings(data, builder);
        return builder.build();
    }

    private void setMappings(WorkflowSchemeData data, WorkflowScheme.Builder<?> builder)
    {
        builder.clearMappings();
        if (data.getDefaultWorkflow() != null)
        {
            builder.setDefaultWorkflow(data.getDefaultWorkflow());
        }

        for (Map.Entry<String, String> entry : data.getMappings().entrySet())
        {
            builder.setMapping(findIssueType(entry.getKey()), entry.getValue());
        }
    }

    private String findIssueType(String type)
    {
        IssueType obj = issueTypeManager.getIssueType(type);
        if (obj == null)
        {
            for (IssueType issueType : issueTypeManager.getIssueTypes())
            {
                if (issueType.getName().equals(type))
                {
                    obj = issueType;
                    break;
                }
            }
        }

        if (obj == null)
        {
            throw new IllegalArgumentException("Unable to find IssueType with id or name of '" + type + "'.");
        }
        return obj.getId();
    }
}

