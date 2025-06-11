//package com.advancedBigDataIndexing.PlanService.controller;
//
//import com.advancedBigDataIndexing.PlanService.exception.JsonSchemaValidationException;
//import com.advancedBigDataIndexing.PlanService.model.Plan;
//import com.advancedBigDataIndexing.PlanService.service.PlanService;
//import com.advancedBigDataIndexing.PlanService.util.GoogleTokenVerifier;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.DigestUtils;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//@RestController
//@RequestMapping("/api/v1/plans")
//@RequiredArgsConstructor
//public class PlanController {
//    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);
//
//    private final PlanService planService;
//    private final ObjectMapper objectMapper;
//    private final GoogleTokenVerifier googleTokenVerifier;
//
//    private String extractToken(String tokenHeader) {
//        String token;
//        if (tokenHeader.startsWith("Bearer ")) {
//            token = tokenHeader.substring(7).trim();
//        } else {
//            token = tokenHeader.trim();
//        }
//        return token;
//    }
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createPlan(@RequestHeader(value = "Authorization", required = false) String tokenHeader, @RequestBody String jsonPlan) {
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            planService.validatePlanAgainstSchema(jsonPlan);
//            Plan plan = objectMapper.readValue(jsonPlan, Plan.class);
//
//            // Debug log before checking for duplicates
//            logger.info("Checking for duplicate plan with objectId: " + plan.getObjectId());
//            if (planService.isDuplicate(plan)) {
//                logger.info("Duplicate plan found with objectId: " + plan.getObjectId());
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate plan data");
//            }
//            String etag = UUID.randomUUID().toString();
//            planService.createPlan(plan, etag);
//            return ResponseEntity.status(HttpStatus.CREATED).eTag(etag).body(plan);
//
//        } catch (JsonSchemaValidationException e) {
//            logger.error("Plan validation failed", e);
//            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
//
//        } catch (Exception e) {
//            logger.error("An error occurred during token verification", e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/{objectId}")
//    public ResponseEntity<?> getPlan(
//            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
//            @PathVariable String objectId,
//            @RequestHeader(value = "If-None-Match", required = false) String requestEtag) {
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            Optional<Plan> plan = planService.getPlanById(objectId);
//            if (plan.isPresent()) {
//                String etag = planService.getEtagForPlan(objectId);
//                if (etag != null && etag.equals(requestEtag)) {
//                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
//                }
//                return ResponseEntity.ok()
//                        .eTag(etag)
//                        .body(plan.get());
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found.");
//            }
//        } catch (Exception e) {
//            logger.error("An error occurred during token verification", e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
//        }
//    }
//
//    @PutMapping("/{objectId}")
//    public ResponseEntity<?> updatePlan(
//            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
//            @PathVariable String objectId,
//            @RequestBody String jsonPlan) {
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            planService.validatePlanAgainstSchema(jsonPlan);
//            Plan updatedPlan = objectMapper.readValue(jsonPlan, Plan.class);
//            String etag = UUID.randomUUID().toString();
//            planService.updatePlan(objectId, updatedPlan, etag);
//            return ResponseEntity.ok().eTag(etag).body("Plan updated successfully.");
//        } catch (JsonSchemaValidationException e) {
//            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("An error occurred during token verification", e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
//        }
//    }
//
//    @PatchMapping("/{objectId}")
//    public ResponseEntity<?> patchPlan(
//            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
//            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
//            @PathVariable String objectId,
//            @RequestBody Map<String, Object> planPatch) {
//
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            if (ifMatchHeader == null || ifMatchHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Missing If-Match header");
//            }
//
//            Optional<Plan> existingPlanOptional = planService.getPlanById(objectId);
//            if (existingPlanOptional.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found.");
//            }
//
//            Plan existingPlan = existingPlanOptional.get();
//
//            String existingPlanJson = objectMapper.writeValueAsString(existingPlan);
//            String serverEtag = planService.getEtagForPlan(objectId);
//
//            logger.info("If-Match Header: " + ifMatchHeader);
//            logger.info("Generated Server ETag: " + serverEtag);
//
//            if (!ifMatchHeader.equals(serverEtag)) {
//                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("ETag mismatch. Resource has been modified.");
//            }
//
//            Map<String, Object> existingPlanMap = objectMapper.convertValue(existingPlan, Map.class);
//
//
//            planPatch.forEach((key, value) -> {
//                if (key.equals("linkedPlanServices") && value instanceof List) {
//                    List<Map<String, Object>> existingServices = (List<Map<String, Object>>) existingPlanMap.getOrDefault(key, new ArrayList<>());
//                    existingServices.addAll((List<Map<String, Object>>) value);
//                    existingPlanMap.put(key, existingServices);
//                } else {
//                    // For non-array keys, replace the values
//                    existingPlanMap.put(key, value);
//                }
//            });
//
//            Plan updatedPlan = objectMapper.convertValue(existingPlanMap, Plan.class);
//
//            planService.validatePlanSchema(updatedPlan);
//
//            planService.saveUpdatedPlan(objectId, updatedPlan);
//
//            String newEtag = planService.getEtagForPlan(objectId);
//
//            return ResponseEntity.ok().eTag(newEtag).body("Plan patched successfully.");
//
//        } catch (JsonSchemaValidationException e) {
//            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("An error occurred during patching", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error patching plan: " + e.getMessage());
//        }
//    }
//
//
//    @DeleteMapping("/{objectId}")
//    public ResponseEntity<?> deletePlan(
//            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
//            @PathVariable String objectId) {
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            Optional<Plan> planOptional = planService.getPlanById(objectId);
//            if (planOptional.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan with ID " + objectId + " not found.");
//            }
//            planService.deletePlanById(objectId);
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "Plan with ID " + objectId + " was successfully deleted.");
//            return ResponseEntity.status(HttpStatus.OK).body(response);
//        } catch (Exception e) {
//            logger.error("An error occurred during token verification", e);
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
//        }
//    }
//}






package com.advancedBigDataIndexing.PlanService.controller;

import com.advancedBigDataIndexing.PlanService.exception.JsonSchemaValidationException;
import com.advancedBigDataIndexing.PlanService.model.LinkedPlanService;
import com.advancedBigDataIndexing.PlanService.model.Plan;
import com.advancedBigDataIndexing.PlanService.service.PlanService;
import com.advancedBigDataIndexing.PlanService.util.GoogleTokenVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {
    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    private final PlanService planService;
    private final ObjectMapper objectMapper;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RabbitTemplate rabbitTemplate;

    private String extractToken(String tokenHeader) {
        String token;
        if (tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7).trim();
        } else {
            token = tokenHeader.trim();
        }
        return token;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@RequestHeader(value = "Authorization", required = false) String tokenHeader, @RequestBody String jsonPlan) {
        try {
            if (tokenHeader == null || tokenHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            }

            String token = extractToken(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            googleTokenVerifier.verifyToken(token);

            planService.validatePlanAgainstSchema(jsonPlan);
            Plan plan = objectMapper.readValue(jsonPlan, Plan.class);

            // Determine if parent or child based on parentId
            // If parentId is null -> parent; else child
            boolean isDuplicate = planService.isDuplicate(plan);
            logger.info("Checking for duplicate plan with objectId: " + plan.getObjectId() + ", Found: " + isDuplicate);

            if (isDuplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Duplicate plan data");
            }

            String etag = UUID.randomUUID().toString();
            planService.createPlan(plan, etag);
            return ResponseEntity.status(HttpStatus.CREATED).eTag(etag).body(plan);

        } catch (JsonSchemaValidationException e) {
            logger.error("Plan validation failed", e);
            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());

        } catch (Exception e) {
            logger.error("An error occurred during token verification", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<?> getPlan(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable String objectId,
            @RequestHeader(value = "If-None-Match", required = false) String requestEtag,
            @RequestParam(value = "childFieldValue", required = false) String childFieldValue) {
        try {
            if (tokenHeader == null || tokenHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            }

            String token = extractToken(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            googleTokenVerifier.verifyToken(token);

            // If childFieldValue is provided, perform a search in Elasticsearch instead of normal retrieval
            if (childFieldValue != null && !childFieldValue.isEmpty()) {
                Object searchResults = planService.searchByChildField(childFieldValue);
                return ResponseEntity.ok(searchResults);
            }

            // Normal plan retrieval by objectId from DB
            Optional<Plan> plan = planService.getPlanById(objectId);
            if (plan.isPresent()) {
                String etag = planService.getEtagForPlan(objectId);
                if (etag != null && etag.equals(requestEtag)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
                }
                return ResponseEntity.ok()
                        .eTag(etag)
                        .body(plan.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found.");
            }
        } catch (Exception e) {
            logger.error("An error occurred during token verification or retrieval", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @PutMapping("/{objectId}")
    public ResponseEntity<?> updatePlan(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable String objectId,
            @RequestBody String jsonPlan) {
        try {
            if (tokenHeader == null || tokenHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            }

            String token = extractToken(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            googleTokenVerifier.verifyToken(token);

            planService.validatePlanAgainstSchema(jsonPlan);
            Plan updatedPlan = objectMapper.readValue(jsonPlan, Plan.class);
            String etag = UUID.randomUUID().toString();
            planService.updatePlan(objectId, updatedPlan, etag);
            return ResponseEntity.ok().eTag(etag).body("Plan updated successfully.");
        } catch (JsonSchemaValidationException e) {
            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred during token verification", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

//    @PatchMapping("/{objectId}")
//    public ResponseEntity<?> patchPlan(
//            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
//            @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
//            @PathVariable String objectId,
//            @RequestBody Map<String, Object> planPatch) {
//
//        try {
//            if (tokenHeader == null || tokenHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
//            }
//
//            String token = extractToken(tokenHeader);
//            if (token == null || token.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
//            }
//
//            googleTokenVerifier.verifyToken(token);
//
//            if (ifMatchHeader == null || ifMatchHeader.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Missing If-Match header");
//            }
//
//            Optional<Plan> existingPlanOptional = planService.getPlanById(objectId);
//            if (existingPlanOptional.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found.");
//            }
//
//            Plan existingPlan = existingPlanOptional.get();
//
//            String existingPlanJson = objectMapper.writeValueAsString(existingPlan);
//            String serverEtag = planService.getEtagForPlan(objectId);
//
//            logger.info("If-Match Header: " + ifMatchHeader);
//            logger.info("Generated Server ETag: " + serverEtag);
//
//            if (!ifMatchHeader.equals(serverEtag)) {
//                return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("ETag mismatch. Resource has been modified.");
//            }
//
//            Map<String, Object> existingPlanMap = objectMapper.convertValue(existingPlan, Map.class);
//
//            planPatch.forEach((key, value) -> {
//                if (key.equals("linkedPlanServices") && value instanceof List) {
//                    List<Map<String, Object>> existingServices = (List<Map<String, Object>>) existingPlanMap.getOrDefault(key, new ArrayList<>());
//
//                    // Merge the new linkedPlanServices into the existing ones
//                    List<Map<String, Object>> newServices = (List<Map<String, Object>>) value;
//                    for (Map<String, Object> newService : newServices) {
//                        boolean exists = existingServices.stream()
//                                .anyMatch(existingService -> existingService.get("objectId").equals(newService.get("objectId")));
//                        if (!exists) {
//                            existingServices.add(newService);
//                        }
//                    }
//
//                    existingPlanMap.put(key, existingServices);
//                } else {
//                    // For non-array keys, replace the values
//                    existingPlanMap.put(key, value);
//                }
//            });
//
//            Plan updatedPlan = objectMapper.convertValue(existingPlanMap, Plan.class);
//
//            planService.validatePlanSchema(updatedPlan);
//
//            planService.saveUpdatedPlan(objectId, updatedPlan);
//
//            String newEtag = planService.getEtagForPlan(objectId);
//
//
//
//            return ResponseEntity.ok().eTag(newEtag).body("Plan patched successfully.");
//
//        } catch (JsonSchemaValidationException e) {
//            return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("An error occurred during patching", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error patching plan: " + e.getMessage());
//        }
//    }
//
@PatchMapping("/{objectId}")
public ResponseEntity<?> patchPlan(
        @RequestHeader(value = "Authorization", required = false) String tokenHeader,
        @RequestHeader(value = "If-Match", required = false) String ifMatchHeader,
        @PathVariable String objectId,
        @RequestBody Map<String, Object> planPatch) {

    try {
        if (tokenHeader == null || tokenHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
        }

        String token = extractToken(tokenHeader);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        googleTokenVerifier.verifyToken(token);

        if (ifMatchHeader == null || ifMatchHeader.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("Missing If-Match header");
        }

        Optional<Plan> existingPlanOptional = planService.getPlanById(objectId);
        if (existingPlanOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found.");
        }

        Plan existingPlan = existingPlanOptional.get();

        String existingPlanJson = objectMapper.writeValueAsString(existingPlan);
        String serverEtag = planService.getEtagForPlan(objectId);

        logger.info("If-Match Header: " + ifMatchHeader);
        logger.info("Generated Server ETag: " + serverEtag);

        if (!ifMatchHeader.equals(serverEtag)) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body("ETag mismatch. Resource has been modified.");
        }

        Map<String, Object> existingPlanMap = objectMapper.convertValue(existingPlan, Map.class);

        planPatch.forEach((key, value) -> {
            if (key.equals("linkedPlanServices") && value instanceof List) {
                List<Map<String, Object>> existingServices = (List<Map<String, Object>>) existingPlanMap.getOrDefault(key, new ArrayList<>());

                // Merge the new linkedPlanServices into the existing ones
                List<Map<String, Object>> newServices = (List<Map<String, Object>>) value;
                for (Map<String, Object> newService : newServices) {
                    boolean exists = existingServices.stream()
                            .anyMatch(existingService -> existingService.get("objectId").equals(newService.get("objectId")));
                    if (!exists) {
                        existingServices.add(newService);
                    }
                }

                existingPlanMap.put(key, existingServices);
            } else {
                // For non-array keys, replace the values
                existingPlanMap.put(key, value);
            }
        });

        Plan updatedPlan = objectMapper.convertValue(existingPlanMap, Plan.class);

        planService.validatePlanSchema(updatedPlan);

        planService.saveUpdatedPlan(objectId, updatedPlan);

        String newEtag = planService.getEtagForPlan(objectId);

        // Publish a message to RabbitMQ with the updated plan
        try {
            String updateMessage = "{\"action\":\"update\",\"data\":" + objectMapper.writeValueAsString(updatedPlan) + "}";

            // Send the message to the 'plan-queue'
            rabbitTemplate.convertAndSend("plan-queue", updateMessage);

            logger.info("Sent update message for Plan with ID: {} to RabbitMQ", objectId);
        } catch (Exception e) {
            logger.error("Failed to send update message to RabbitMQ for Plan ID: {}", objectId, e);
        }

        logger.info("Patched Plan with ID: {}", objectId);

        return ResponseEntity.ok().eTag(newEtag).body("Plan patched successfully.");

    } catch (JsonSchemaValidationException e) {
        return ResponseEntity.badRequest().body("Invalid plan data: " + e.getMessage());
    } catch (Exception e) {
        logger.error("An error occurred during patching", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error patching plan: " + e.getMessage());
    }
}

    @DeleteMapping("/{objectId}")
    public ResponseEntity<?> deletePlan(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable String objectId) {
        try {
            if (tokenHeader == null || tokenHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            }

            String token = extractToken(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            googleTokenVerifier.verifyToken(token);

            Optional<Plan> planOptional = planService.getPlanById(objectId);
            if (planOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan with ID " + objectId + " not found.");
            }
            planService.deletePlanById(objectId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Plan with ID " + objectId + " was successfully deleted.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.error("An error occurred during token verification", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchPlans(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @RequestParam(value = "childFieldValue", required = true) String childFieldValue) {
        try {
            if (tokenHeader == null || tokenHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            }

            String token = extractToken(tokenHeader);
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
            }

            googleTokenVerifier.verifyToken(token);

            // Perform the search using the service
            Object searchResults = planService.searchByChildField(childFieldValue);
            return ResponseEntity.ok(searchResults);
        } catch (Exception e) {
            logger.error("An error occurred during search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error performing search: " + e.getMessage());
        }
    }


}
