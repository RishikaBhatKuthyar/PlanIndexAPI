package com.advancedBigDataIndexing.PlanService.serviceImpl;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.util.ObjectBuilder;
import com.advancedBigDataIndexing.PlanService.exception.JsonSchemaValidationException;
import com.advancedBigDataIndexing.PlanService.model.Plan;
import com.advancedBigDataIndexing.PlanService.repository.PlanRepository;
import com.advancedBigDataIndexing.PlanService.service.PlanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private static final Logger logger = LoggerFactory.getLogger(PlanServiceImpl.class);

    private final PlanRepository planRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ElasticsearchClient elasticsearchClient; // Add this


    @Override
    public void createPlan(Plan plan, String etag) throws JsonProcessingException {
        // Save plan to DB
        planRepository.save(plan);

        // Store the ETag in Redis
        redisTemplate.opsForValue().set(plan.getObjectId() + ":etag", etag);

        // Create the message with "action" and "data"
        String message = objectMapper.writeValueAsString(
                Map.of(
                        "action", "index",  // Specify the action type
                        "data", plan        // Include the Plan object
                )
        );

        // Publish the message to RabbitMQ
        rabbitTemplate.convertAndSend("plan-exchange", "plan-queue", message);

        logger.info("Published plan with action 'index' to RabbitMQ with objectId: {}", plan.getObjectId());
    }



    @Override
    public Optional<Plan> getPlanById(String objectId) {
        return planRepository.findById(objectId);
    }

    // Updated method for handling full update of a Plan
    public void updatePlan(String objectId, Plan updatedPlan, String etag) throws JsonSchemaValidationException {
        Optional<Plan> existingPlanOpt = planRepository.findById(objectId);
        if (existingPlanOpt.isEmpty()) {
            throw new JsonSchemaValidationException("Plan not found");
        }

        updatedPlan.setObjectId(objectId);
        planRepository.save(updatedPlan); // Save the updated plan to repository
        redisTemplate.opsForValue().set(objectId + ":etag", etag); // Cache ETag in Redis
        logger.info("Updated Plan with ID: {}", objectId);
    }

    public void patchPlan(String objectId, Plan patchData, String etag) throws JsonSchemaValidationException {
        Plan existingPlan = planRepository.findById(objectId).orElseThrow(() -> new JsonSchemaValidationException("Plan not found"));

        // Apply the patch data only to the non-null fields
        if (patchData.getPlanCostShares() != null) {
            existingPlan.setPlanCostShares(patchData.getPlanCostShares());
        }
        if (patchData.getLinkedPlanServices() != null) {
            existingPlan.setLinkedPlanServices(patchData.getLinkedPlanServices());
        }
        if (patchData.get_org() != null) {
            existingPlan.set_org(patchData.get_org());
        }
        if (patchData.getObjectType() != null) {
            existingPlan.setObjectType(patchData.getObjectType());
        }
        if (patchData.getPlanStatus() != null) {
            existingPlan.setPlanStatus(patchData.getPlanStatus());
        }
        if (patchData.getCreationDate() != null) {
            existingPlan.setCreationDate(patchData.getCreationDate());
        }

        planRepository.save(existingPlan); // Save the partially updated plan
        redisTemplate.opsForValue().set(objectId + ":etag", etag); // Update ETag in Redis
        try {
            // Publish a delete message to RabbitMQ
            String updateMessage = "{\"action\":\"update\",\"data\":\"" +existingPlan + "\"}";

            // Send the message to the 'plan-queue'
            rabbitTemplate.convertAndSend("plan-queue", updateMessage);

            logger.info("Sent update message for Plan with ID: {} to RabbitMQ", objectId);
        } catch (Exception e) {
            logger.error("Failed to send update message to RabbitMQ for Plan ID: {}", objectId, e);
        }
        logger.info("Patched Plan with ID: {}", objectId);
    }



    @Override
    public void deletePlanById(String objectId) {
        // Delete the plan from the repository (e.g., database)
        planRepository.deleteById(objectId);

        // Remove the cached ETag from Redis
        redisTemplate.delete(objectId + ":etag");

        try {
            // Publish a delete message to RabbitMQ
            String deleteMessage = "{\"action\":\"delete\",\"data\":\"" + objectId + "\"}";

            // Send the message to the 'plan-queue'
            rabbitTemplate.convertAndSend("plan-queue", deleteMessage);

            logger.info("Sent delete message for Plan with ID: {} to RabbitMQ", objectId);
        } catch (Exception e) {
            logger.error("Failed to send delete message to RabbitMQ for Plan ID: {}", objectId, e);
        }

        logger.info("Deleted Plan with ID: {} from repository and Redis", objectId);
    }




    @Override
    public String getEtagForPlan(String objectId) {
        return redisTemplate.opsForValue().get(objectId + ":etag");
    }
    @Override
    public boolean validatePlanAgainstSchema(String jsonData) throws JsonSchemaValidationException {
        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance();
            JsonSchema schema = factory.getSchema(getClass().getClassLoader().getResourceAsStream("schema/plan-schema.json"));
            JsonNode node = objectMapper.readTree(jsonData);
            Set<ValidationMessage> errors = schema.validate(node);
            if (!errors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (ValidationMessage error : errors) {
                    errorMessage.append(error.getMessage()).append("; ");
                }
                throw new JsonSchemaValidationException("JSON Schema Validation Error: " + errorMessage);
            }
            return true;
        } catch (Exception e) {
            throw new JsonSchemaValidationException("Schema validation failed.", e);
        }
    }

    @Override
    public void validatePlanSchema(Plan updatedPlan) throws JsonSchemaValidationException {
        try {
            String jsonData = objectMapper.writeValueAsString(updatedPlan);
            validatePlanAgainstSchema(jsonData);
        } catch (Exception e) {
            throw new JsonSchemaValidationException("Schema validation failed for the updated plan.", e);
        }
    }

    @Override
    public void saveUpdatedPlan(String objectId, Plan updatedPlan) {
        updatedPlan.setObjectId(objectId);
        planRepository.save(updatedPlan);
        try {
            String updatedPlanJson = objectMapper.writeValueAsString(updatedPlan);
            String newEtag = "\"" + DigestUtils.md5DigestAsHex(updatedPlanJson.getBytes()) + "\"";
            redisTemplate.opsForValue().set(objectId + ":etag", newEtag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isDuplicate(Plan plan) {
        Boolean exists = redisTemplate.hasKey("Plan:" + plan.getObjectId());
        logger.info("Checking for duplicate plan with objectId: " + plan.getObjectId() + ", Found: " + exists);
        return Boolean.TRUE.equals(exists);
    }

//    @Override
//    public Object searchByChildField(String childFieldValue) {
//        // Use Elasticsearch client to perform a has_child query or has_parent query.
//        // Construct a query something like:
//        //
//        // {
//        //   "query": {
//        //     "has_child": {
//        //       "type": "child",
//        //       "query": {
//        //         "match": { "child_data_field": childFieldValue }
//        //       },
//        //       "inner_hits": {}
//        //     }
//        //   }
//        // }
//        //
//        // Execute the search and return the parsed results.
//        //
//        // For now, return a placeholder or an empty list:
//        return new ArrayList<>();
//    }
//@Override
//public Object searchByChildField(String childFieldValue) {
//    try {
//        // Perform a match query on a nested field, for example: linkedPlanServices.linkedService.name
//        // Adjust the field name as needed if your indexing strategy differs.
//        var response = elasticsearchClient.search(s -> s
//                        .index("plan_index") // the index name where your Plan documents are stored
//                        .query(q -> q
//                                .match(m -> m
//                                        .field("linkedPlanServices.linkedService.name")
//                                        .query(childFieldValue)
//                                )
//                        ),
//                Plan.class // We know the document structure matches the Plan class
//        );
//
//        // Extract hits
//        List<Plan> results = new ArrayList<>();
//        response.hits().hits().forEach(hit -> results.add(hit.source()));
//
//        return results;
//    } catch (Exception e) {
//        logger.error("Error performing Elasticsearch query", e);
//        return Collections.emptyList();
//    }
//}


    @Override
    public Object searchByChildField(String childFieldValue) {
        try {
            var response = elasticsearchClient.search(s -> s
                            .index("plan-index")
                            .query(q -> q
                                    .hasChild(h -> h
                                            .type("linked_service")
                                            .query(mq -> mq
                                                    .match(m -> m
                                                            .field("serviceName")
                                                            .query(childFieldValue)
                                                    )
                                            )
                                    )
                            ),
                    Plan.class
            );


            return response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error performing Elasticsearch query", e);
            return Collections.emptyList();
        }
    }
}