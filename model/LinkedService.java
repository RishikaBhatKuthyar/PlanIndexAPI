package com.advancedBigDataIndexing.PlanService.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LinkedService {
    private String objectId;
    private String objectType;
    private String _org;
    private String name;
}
