package io.github.crazy.lonewolf.sampleapp.model;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJModel;

import java.util.Date;
@OmniJModel
        (operation = "costDetails")
public record CostDetails(String dept, Date startDate, Date endDate) {
}
