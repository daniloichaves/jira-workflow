package com.corefiling.jira.plugins.workflowdata.util;

import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class DescriptorUtil
{
    public static Collection<ConditionDescriptor> getConditionsForTransition(final ActionDescriptor actionDescriptor)
    {
        RestrictionDescriptor restriction = actionDescriptor.getRestriction();
        if (restriction != null)
        {
            ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
            if (conditionsDescriptor != null)
            {
                List<ConditionDescriptor> conditions = new ArrayList<ConditionDescriptor>();
                conditions.addAll(conditionsDescriptor.getConditions());
                return conditions;
            }
        }
        return Collections.<ConditionDescriptor>emptyList();
    }
}
