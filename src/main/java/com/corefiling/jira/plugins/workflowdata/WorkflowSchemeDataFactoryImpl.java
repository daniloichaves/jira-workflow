/*
 * Based on code in  https://bitbucket.org/atlassian/jira-testkit
 * which is licenced under Apache 2.0
 */
package com.corefiling.jira.plugins.workflowdata;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.web.bean.WorkflowDescriptorFormatBean;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.corefiling.jira.plugins.workflowdata.util.DescriptorUtil;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class WorkflowSchemeDataFactoryImpl implements WorkflowSchemeDataFactory
{
    private final IssueTypeManager issueTypeManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final DateTimeFormatter formatter;
    private final PluginAccessor pluginAccessor;
    private final FieldScreenManager fieldScreenManager;
    private final WorkflowDescriptorFormatBean workflowFormatter;

    WorkflowSchemeDataFactoryImpl(IssueTypeManager issueTypeManager, WorkflowSchemeManager workflowSchemeManager,
            PluginAccessor pluginAccessor, FieldScreenManager fieldScreenManager, DateTimeFormatter formatter)
    {
        this.issueTypeManager = issueTypeManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.pluginAccessor = pluginAccessor;
        this.fieldScreenManager = fieldScreenManager;
        this.formatter = formatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).withLocale(Locale.ENGLISH);
        this.workflowFormatter = new WorkflowDescriptorFormatBean(pluginAccessor);
    }

    private List<DescriptorData> getFunctions(final JiraWorkflow workflow, final ActionDescriptor transition)
    {
        List<DescriptorData> functions = Lists.newArrayList();
        for (FunctionDescriptor func : workflow.getPostFunctionsForTransition(transition))
        {
            DescriptorData function = new DescriptorData();
            String className = (String) func.getArgs().get("class.name");
            if (className != null)
            {
                workflowFormatter.setPluginType("workflow-function");
                function.setDescription(workflowFormatter.formatDescriptor(func).getDescription());
                HashMap<String, String> parameters = new HashMap<String, String>();
                for (Map.Entry<Object, Object> entry : (Set<Map.Entry>)func.getArgs().entrySet())
                {
                    parameters.put((String) entry.getKey(), (String) entry.getValue());
                }
                function.setParameters(parameters);
            }
            else
            {
                function.setDescription("UNKNOWN");
            }
            functions.add(function);
        }
        return functions;
    }

    private List<DescriptorData> getConditions(final JiraWorkflow workflow, final ActionDescriptor transition)
    {
        List<DescriptorData> conditions = Lists.newArrayList();
        for (ConditionDescriptor condition : DescriptorUtil.getConditionsForTransition(transition))
        {
            DescriptorData conditionData = new DescriptorData();
            String className = (String) condition.getArgs().get("class.name");
            if (className != null)
            {
                workflowFormatter.setPluginType("workflow-condition");
                conditionData.setDescription(workflowFormatter.formatDescriptor(condition).getDescription());
                HashMap<String, String> parameters = new HashMap<String, String>();
                for (Map.Entry<Object, Object> entry : (Set<Map.Entry>)condition.getArgs().entrySet())
                {
                    parameters.put((String) entry.getKey(), (String) entry.getValue());
                }

                conditionData.setParameters(parameters);
            }
            else
            {
                conditionData.setDescription("UNKNOWN");
            }
            conditions.add(conditionData);
        }
        return conditions;
    }

    private List<String> getScreens(final ActionDescriptor transition, final Map<Integer, List<FieldScreen>> screensForAction)
    {
        List<String> screens = Lists.newArrayList();
        if (screensForAction.get(transition.getId()) != null)
        {
          for (FieldScreen fieldScreen : screensForAction.get(transition.getId()))
          {
              screens.add(fieldScreen.getName());
          }
        }
        return screens;
    }

    private Status getTargetStatus(final JiraWorkflow workflow, final ActionDescriptor transition)
    {
        StepDescriptor targetStep = workflow.getDescriptor().getStep(transition.getUnconditionalResult().getStep());
        return workflow.getLinkedStatusObject(targetStep);
    }

    private List<TransitionData> getTransitions(final JiraWorkflow workflow, final StepDescriptor step, final Map<Integer, List<FieldScreen>> screensForAction)
    {
        List<TransitionData> transitions = Lists.newArrayList();
        for (Object transitionObject : workflow.getDescriptor().getStep(step.getId()).getActions())
        {
            ActionDescriptor transitionAction = (ActionDescriptor) transitionObject;
            Status targetStatus = getTargetStatus(workflow, transitionAction);
            transitions.add(new TransitionData().setName(transitionAction.getName())
                                                .setId(transitionAction.getId())
                                                .setToStatus(targetStatus.getName())
                                                .setToCategory(targetStatus.getStatusCategory().getName())
                                                .setScreens(getScreens(transitionAction, screensForAction))
                                                .setFunctions(getFunctions(workflow, transitionAction))
                                                .setConditions(getConditions(workflow, transitionAction)));
        }
        return transitions;
    }

    private List<StatusData> getStatuses(final JiraWorkflow workflow)
    {
        List<StatusData> statusData = Lists.newArrayList();
        Map<Integer, List<FieldScreen>> screensForAction = getScreensForAction(workflow);

        for (Status status : workflow.getLinkedStatusObjects())
        {
            StepDescriptor step = workflow.getLinkedStep(status);
            statusData.add(new StatusData().setName(status.getName())
                                           .setStepName(step.getName())
                                           .setCategory(status.getStatusCategory().getName())
                                           .setTransitions(getTransitions(workflow, step, screensForAction)));
        }
        return statusData;
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
        Set<String> workflowNames = Sets.newHashSet();
        for (JiraWorkflow workflow : ComponentAccessor.getWorkflowManager().getWorkflowsFromScheme(workflowSchemeManager.getSchemeObject(scheme.getId())))
        {
            if (workflowNames.contains(workflow.getName()))
            {
                continue;
            }
            workflowNames.add(workflow.getName());

            WorkflowData workflowData = new WorkflowData().setName(workflow.getName())
                                            .setActive(workflow.isActive())
                                            .setIsDefault(workflow.isDefault())
                                            .setStates(getStatuses(workflow));
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
                                       .setName(scheme.getName())
                                       .setDescription(scheme.getDescription())
                                       .setMappings(map)
                                       .setDefaultWorkflow(defaultWorkflow)
                                       .setDraft(scheme.isDraft())
                                       .setActive(workflowSchemeManager.isActive(scheme))
                                       .setWorkflows(workflows);
    }

    Function<WorkflowScheme, WorkflowSchemeData> fromSchemeToDataFunction()
    {
        return new Function<WorkflowScheme, WorkflowSchemeData>()
        {
            @Override
            public WorkflowSchemeData apply(WorkflowScheme scheme) {
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

    private Map<Integer, List<FieldScreen>> getScreensForAction(final JiraWorkflow workflow)
    {
        Map<Integer, List<FieldScreen>> screensForAction = Maps.newHashMap();
        for (FieldScreen screen : fieldScreenManager.getFieldScreens())
        {
            for (ActionDescriptor action : workflow.getActionsForScreen(screen))
            {
                if (action.hasId())
                {
                  int actionId = action.getId();
                  if (screensForAction.get(actionId) == null)
                  {
                      screensForAction.put(actionId, new ArrayList<FieldScreen>());
                  }
                  screensForAction.get(actionId).add(screen);
                }
            }
        }
        return screensForAction;
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

