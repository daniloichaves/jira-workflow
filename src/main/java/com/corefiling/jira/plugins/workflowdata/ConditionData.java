package com.corefiling.jira.plugins.workflowdata;

import org.codehaus.jackson.annotate.JsonProperty;

public class ConditionData
{
    @JsonProperty
    private String className;

    public ConditionData()
    {
    }

    public String getClassName()
    {
        return className;
    }

    public ConditionData setClassName(String className)
    {
        this.className = className;
        return this;
    }
}
