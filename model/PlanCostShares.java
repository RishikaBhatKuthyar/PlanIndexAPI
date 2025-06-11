package com.advancedBigDataIndexing.PlanService.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanCostShares {
    private String objectId;
    private String objectType;
    private String _org;
    private int deductible;
    private int copay;
}
