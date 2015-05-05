package com.corefiling.jira.plugins.workflowdata;

import org.codehaus.jackson.annotate.JsonProperty;

public class FunctionData
{
    @JsonProperty
    private String className;

    public FunctionData()
    {
    }

    public String getClassName()
    {
        return className;
    }

    public FunctionData setClassName(String className)
    {
        this.className = className;
        return this;
    }
}
