package io.github.crazy.lonewolf.coders.model;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.annotation.OmniJModel;

import java.util.Date;
@OmniJModel
public record CostDetails(String dept, Date startDate, Date endDate) {
}
