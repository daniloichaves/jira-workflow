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
    private int id;

    @JsonProperty
    private List<DescriptorData> functions = Lists.newArrayList();

    @JsonProperty
    private List<DescriptorData> conditions = Lists.newArrayList();

    @JsonProperty
    private List<String> screens = Lists.newArrayList();

    @JsonProperty
    private String toStatus;

    @JsonProperty
    private String toCategory;

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

    public int getId()
    {
        return id;
    }

    public TransitionData setId(int id)
    {
        this.id = id;
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

    public List<String> getScreens()
    {
        return screens;
    }

    public TransitionData setScreens(List<String> screens)
    {
        this.screens = screens;
        return this;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public TransitionData setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
        return this;
    }

    public String getToCategory()
    {
        return toCategory;
    }

    public TransitionData setToCategory(String toCategory)
    {
        this.toCategory = toCategory;
        return this;
    }
}
