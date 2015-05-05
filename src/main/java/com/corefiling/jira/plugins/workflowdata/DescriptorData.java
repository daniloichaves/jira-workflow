package com.corefiling.jira.plugins.workflowdata;

import com.google.common.collect.Maps;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class DescriptorData
{
    @JsonProperty
    private Map<String, String> parameters = Maps.newHashMap();

    @JsonProperty
    private String description;

    public DescriptorData()
    {
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public String getDescription()
    {
        return description;
    }

    public DescriptorData setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public DescriptorData setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
        return this;
    }
}
