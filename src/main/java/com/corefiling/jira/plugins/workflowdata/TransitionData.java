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
    private List<FunctionData> functions = Lists.newArrayList();

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

    public List<FunctionData> getFunctions()
    {
        return functions;
    }

    public TransitionData setFunctions(List<FunctionData> functions)
    {
        this.functions = functions;
        return this;
    }
}
