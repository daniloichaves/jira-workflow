package com.corefiling.jira.plugins.workflowdata;

import org.codehaus.jackson.annotate.JsonProperty;

public class DescriptorData
{
    @JsonProperty
    private String className;

    @JsonProperty
    private String description;

    public DescriptorData()
    {
    }

    public String getClassName()
    {
        return className;
    }

    public String getDescription()
    {
        return description;
    }

    public DescriptorData setClassName(String className)
    {
        this.className = className;
        return this;
    }

    public DescriptorData setDescription(String description)
    {
        this.description = description;
        return this;
    }
}
