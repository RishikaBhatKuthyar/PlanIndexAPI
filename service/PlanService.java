package com.advancedBigDataIndexing.PlanService.service;

import com.advancedBigDataIndexing.PlanService.model.Plan;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PlanService {
    void createPlan(Plan plan, String etag) throws JsonProcessingException;
    Optional<Plan> getPlanById(String objectId);

    void updatePlan(String objectId, Plan updatedPlan, String etag);
    void patchPlan(String objectId, Plan patchData, String etag);
    void deletePlanById(String objectId);
    String getEtagForPlan(String objectId);
    boolean validatePlanAgainstSchema(String jsonData);

    void validatePlanSchema(Plan updatedPlan);

    void saveUpdatedPlan(String objectId, Plan updatedPlan);

    boolean isDuplicate(Plan plan);

    Object searchByChildField(String childFieldValue);


}