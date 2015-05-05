package com.corefiling.jira.plugins.workflowdata;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class WorkflowData
{
    @JsonProperty
    private String name;

    @JsonProperty
    private boolean active;

    @JsonProperty
    private boolean isDefault;

    @JsonProperty
    private String lastModified;

    @JsonProperty
    private String lastModifiedBy;

    @JsonProperty
    private List<StatusData> states = Lists.newArrayList();

    public WorkflowData()
    {
    }

    public String getName()
    {
        return name;
    }

    public WorkflowData setName(String name)
    {
        this.name = name;
        return this;
    }

    public boolean getActive()
    {
        return active;
    }

    public WorkflowData setActive(boolean active)
    {
        this.active = active;
        return this;
    }

    public boolean getIsDefault()
    {
        return isDefault;
    }

    public WorkflowData setIsDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
        return this;
    }

    public String getLastModified()
    {
        return lastModified;
    }

    public WorkflowData setLastModified(String lastModified)
    {
        this.lastModified = lastModified;
        return this;
    }

    public String getLastModifiedBy()
    {
        return lastModifiedBy;
    }

    public WorkflowData setLastModifiedBy(String lastModifiedBy)
    {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    public List<StatusData> getStates()
    {
        return states;
    }

    public WorkflowData setStates(List<StatusData> states)
    {
        this.states = states;
        return this;
    }
}
