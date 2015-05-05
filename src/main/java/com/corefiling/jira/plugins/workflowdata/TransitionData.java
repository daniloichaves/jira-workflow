package com.corefiling.jira.plugins.workflowdata;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class TransitionData
{
    @JsonProperty
    private String name;

    @JsonProperty
    private List<DescriptorData> functions = Lists.newArrayList();

    @JsonProperty
    private List<DescriptorData> conditions = Lists.newArrayList();

    public TransitionData()
    {
    }

    public String getName()
    {
        return name;
    }

    public TransitionData setName(String name)
    {
        this.name = name;
        return this;
    }

    public List<DescriptorData> getFunctions()
    {
        return functions;
    }

    public List<DescriptorData> getConditions()
    {
        return conditions;
    }

    public TransitionData setFunctions(List<DescriptorData> functions)
    {
        this.functions = functions;
        return this;
    }

    public TransitionData setConditions(List<DescriptorData> conditions)
    {
        this.conditions = conditions;
        return this;
    }
}
