package com.advancedBigDataIndexing.PlanService.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.List;

@RedisHash("Plan")
@Data
@NoArgsConstructor
public class Plan implements Serializable {
    @Id
    private String objectId;
    private PlanCostShares planCostShares;
    private List<LinkedPlanService> linkedPlanServices;
    private String _org;
    private String objectType;
    private String planStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String creationDate;
}