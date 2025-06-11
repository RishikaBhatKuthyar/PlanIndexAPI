package com.advancedBigDataIndexing.PlanService.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanServiceCostShares {
    private Integer deductible;
    private String _org;
    private Integer copay;
    private String objectId;
    private String objectType;
}
