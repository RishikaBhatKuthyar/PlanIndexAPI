package com.advancedBigDataIndexing.PlanService.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LinkedPlanService {
    private LinkedService linkedService;
    private PlanCostShares planserviceCostShares;
    private String _org;
    private String objectId;
    private String objectType;
}
