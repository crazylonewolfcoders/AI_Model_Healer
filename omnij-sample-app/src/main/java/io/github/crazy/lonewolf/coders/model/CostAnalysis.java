package io.github.crazy.lonewolf.coders.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJModel;

import java.util.Date;

@OmniJModel
public record CostAnalysis (
        String id, String name, boolean cost, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy") Date date, @JsonProperty(required = true) double costitem, float tripleItem
, CostDetails costDetails
) {
    @Override
    public String toString(){
        return id+ " Name:"+ name;
    }
}
