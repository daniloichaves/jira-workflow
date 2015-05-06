package com.corefiling.jira.plugins.workflowdata;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class StatusData
{
    @JsonProperty
    private String category;

    @JsonProperty
    private String name;

    @JsonProperty
    private String stepName;

    @JsonProperty
    private List<TransitionData> transitions = Lists.newArrayList();

    public StatusData()
    {
    }

    public String getId()
    {
        return category;
    }

    public StatusData setCategory(String category)
    {
        this.category = category;
        return this;
    }

    public String getName()
    {
        return name;
    }

    public StatusData setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getStepName()
    {
        return stepName;
    }

    public StatusData setStepName(String stepName)
    {
        this.stepName = stepName;
        return this;
    }

    public List<TransitionData> getTransitions()
    {
        return transitions;
    }

    public StatusData setTransitions(List<TransitionData> transitions)
    {
        this.transitions = transitions;
        return this;
    }
}
