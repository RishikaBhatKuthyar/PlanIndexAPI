package com.advancedBigDataIndexing.PlanService.repository;

import com.advancedBigDataIndexing.PlanService.model.Plan;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends CrudRepository<Plan, String> {


    boolean existsByObjectId(String objectId);
}
