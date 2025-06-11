////package com.advancedBigDataIndexing.PlanService.serviceImpl;
////
////import com.advancedBigDataIndexing.PlanService.model.Plan;
////import com.fasterxml.jackson.core.JsonProcessingException;
////import com.fasterxml.jackson.databind.JsonNode;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import co.elastic.clients.elasticsearch.ElasticsearchClient;
////import co.elastic.clients.elasticsearch.core.DeleteRequest;
////import co.elastic.clients.elasticsearch.core.UpdateResponse;
////import co.elastic.clients.elasticsearch.core.IndexRequest;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.amqp.rabbit.annotation.RabbitListener;
////import org.springframework.stereotype.Service;
////
////@Service
////public class RabbitMQConsumer {
////
////    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
////    private final ObjectMapper objectMapper;
////    private final ElasticsearchClient elasticsearchClient;
////
////    public RabbitMQConsumer(ObjectMapper objectMapper, ElasticsearchClient elasticsearchClient) {
////        this.objectMapper = objectMapper;
////        this.elasticsearchClient = elasticsearchClient;
////    }
////
////    @RabbitListener(queues = "plan-queue")
////    public void consumeMessage(String message) {
////        try {
////            logger.info("Received message: {}", message);
////
////            // Parse the incoming message to JsonNode
////            JsonNode jsonNode = objectMapper.readTree(message);
////
////            // Check if the "action" field is present in the message
////            if (!jsonNode.has("action")) {
////                logger.warn("Received message without an action field: {}", message);
////                return;
////            }
////
////            String action = jsonNode.get("action").asText();
////            logger.info("Processing action: {}", action);
////
////            // Switch based on the action type (index, delete, update)
////            switch (action.toLowerCase()) {
////                case "index":
////                    handleIndexMessage(jsonNode);
////                    break;
////                case "delete":
////                    handleDeleteMessage(jsonNode);
////                    break;
////                case "update":
////                    handleUpdateMessage(jsonNode);
////                    break;
////                default:
////                    logger.warn("Unknown action received: {}", action);
////            }
////        } catch (JsonProcessingException e) {
////            logger.error("Invalid JSON format in message: {}", message, e);
////        } catch (Exception e) {
////            logger.error("Failed to process message from RabbitMQ", e);
////        }
////    }
////
////    private void handleIndexMessage(JsonNode jsonNode) {
////        try {
////            if (!jsonNode.has("data")) {
////                logger.warn("Indexing message missing 'data' field: {}", jsonNode);
////                return;
////            }
////
////            // Convert the 'data' field to Plan object
////            Plan plan = objectMapper.treeToValue(jsonNode.get("data"), Plan.class);
////            logger.info("Processing indexing request for Plan: {}", plan);
////
////            // Index the Plan into Elasticsearch
////            elasticsearchClient.index(i -> i
////                    .index("plan-index")
////                    .id(plan.getObjectId())
////                    .document(plan)
////            );
////
////            logger.info("Successfully indexed plan with ID: {}", plan.getObjectId());
////        } catch (Exception e) {
////            logger.error("Failed to index Plan object", e);
////        }
////    }
////
////    private void handleDeleteMessage(JsonNode jsonNode) {
////        try {
////            if (!jsonNode.has("data")) {
////                logger.warn("Deletion message missing 'data' field: {}", jsonNode);
////                return;
////            }
////
////            String planId = jsonNode.get("data").asText();
////            if (planId == null || planId.trim().isEmpty()) {
////                logger.warn("Received empty Plan ID for deletion.");
////                return;
////            }
////
////            logger.info("Processing delete request for Plan ID: {}", planId);
////
////            // Perform the delete operation in Elasticsearch
////            var deleteResponse = elasticsearchClient.delete(d -> d
////                    .index("plan-index")
////                    .id(planId)
////            );
////
////            if (deleteResponse.result().toString().equals("deleted")) {
////                logger.info("Successfully deleted plan with ID: {}", planId);
////            } else {
////                logger.warn("Failed to delete plan with ID: {}", planId);
////            }
////        } catch (Exception e) {
////            logger.error("Failed to delete Plan object", e);
////        }
////    }
////
////    private void handleUpdateMessage(JsonNode jsonNode) {
////        try {
////            if (!jsonNode.has("data")) {
////                logger.warn("Update message missing 'data' field: {}", jsonNode);
////                return;
////            }
////
////            Plan plan = objectMapper.treeToValue(jsonNode.get("data"), Plan.class);
////            if (plan.getObjectId() == null || plan.getObjectId().isEmpty()) {
////                logger.warn("Received Plan object without an ID for update.");
////                return;
////            }
////
////            logger.info("Processing update request for Plan: {}", plan);
////
////            // Perform the update operation in Elasticsearch
////            UpdateResponse<Plan> response = elasticsearchClient.update(u -> u
////                            .index("plan-index")
////                            .id(plan.getObjectId())
////                            .doc(plan),
////                    Plan.class
////            );
////
////            logger.info("Successfully updated plan with ID: {}, Result: {}", plan.getObjectId(), response.result());
////        } catch (Exception e) {
////            logger.error("Failed to update Plan object", e);
////        }
////    }
////}
////
////package com.advancedBigDataIndexing.PlanService.serviceImpl;
////
////import co.elastic.clients.elasticsearch.core.DeleteRequest;
////import co.elastic.clients.elasticsearch.core.IndexRequest;
////import com.advancedBigDataIndexing.PlanService.model.Plan;
////import com.fasterxml.jackson.core.JsonProcessingException;
////import com.fasterxml.jackson.databind.JsonNode;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import co.elastic.clients.elasticsearch.ElasticsearchClient;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.amqp.rabbit.annotation.RabbitListener;
////import org.springframework.stereotype.Service;
////
////@Service
////public class RabbitMQConsumer {
////
////    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
////    private final ObjectMapper objectMapper;
////    private final ElasticsearchClient elasticsearchClient;
////
////    public RabbitMQConsumer(ObjectMapper objectMapper, ElasticsearchClient elasticsearchClient) {
////        this.objectMapper = objectMapper;
////        this.elasticsearchClient = elasticsearchClient;
////    }
////
////    @RabbitListener(queues = "plan-queue")
////    public void consumeMessage(String message) {
////        try {
////            logger.info("Received message: {}", message);
////
////            // Parse the incoming message to JsonNode
////            JsonNode jsonNode = objectMapper.readTree(message);
////
////            // Check if the "action" field is present in the message
////            if (!jsonNode.has("action")) {
////                logger.warn("Received message without an action field: {}", message);
////                return;
////            }
////
////            String action = jsonNode.get("action").asText();
////            logger.info("Processing action: {}", action);
////
////            // Determine parentId if available
////            String parentId = jsonNode.has("parentId") ? jsonNode.get("parentId").asText() : null;
////
////            // Switch based on the action type (index, delete, update)
////            switch (action.toLowerCase()) {
////                case "index":
////                    handleIndexMessage(jsonNode.get("data"), parentId);
////                    break;
////                case "delete":
////                    handleDeleteMessage(jsonNode.get("data"));
////                    break;
////                case "update":
////                    handleUpdateMessage(jsonNode.get("data"));
////                    break;
////                default:
////                    logger.warn("Unknown action received: {}", action);
////            }
////        } catch (JsonProcessingException e) {
////            logger.error("Invalid JSON format in message: {}", message, e);
////        } catch (Exception e) {
////            logger.error("Failed to process message from RabbitMQ", e);
////        }
////    }
////
////    private void handleIndexMessage(JsonNode dataNode, String parentId) {
////        try {
////            if (dataNode == null) {
////                logger.warn("Indexing message missing 'data' field.");
////                return;
////            }
////
////            String joinFieldType = (parentId == null) ? "plan" : (parentId.contains("cost_share") ? "cost_share" : "linked_service");
////            String id = parentId == null ? dataNode.get("objectId").asText() : dataNode.get("id").asText();
////
////            var indexRequest = new IndexRequest.Builder<>()
////                    .index("plan-index")
////                    .id(id)
////                    .document(dataNode)
////                    .routing(parentId)
////                    .build();
////
////            elasticsearchClient.index(indexRequest);
////            logger.info("Successfully indexed document with ID: {}", id);
////        } catch (Exception e) {
////            logger.error("Failed to index document", e);
////        }
////    }
////
////    private void handleDeleteMessage(JsonNode dataNode) {
////        try {
////            if (dataNode == null) {
////                logger.warn("Deletion message missing 'data' field.");
////                return;
////            }
////
////            String documentId = dataNode.asText();
////            var deleteRequest = new DeleteRequest.Builder()
////                    .index("plan-index")
////                    .id(documentId)
////                    .build();
////
////            elasticsearchClient.delete(deleteRequest);
////            logger.info("Successfully deleted document with ID: {}", documentId);
////        } catch (Exception e) {
////            logger.error("Failed to delete document", e);
////        }
////    }
////
////    private void handleUpdateMessage(JsonNode dataNode) {
////        try {
////            if (dataNode == null) {
////                logger.warn("Update message missing 'data' field.");
////                return;
////            }
////
////            Plan plan = objectMapper.treeToValue(dataNode, Plan.class);
////            if (plan.getObjectId() == null || plan.getObjectId().isEmpty()) {
////                logger.warn("Received Plan object without an ID for update.");
////                return;
////            }
////
////            logger.info("Processing update request for Plan: {}", plan);
////
////            elasticsearchClient.update(u -> u
////                            .index("plan-index")
////                            .id(plan.getObjectId())
////                            .doc(plan),
////                    Plan.class
////            );
////
////            logger.info("Successfully updated plan with ID: {}", plan.getObjectId());
////        } catch (Exception e) {
////            logger.error("Failed to update Plan object", e);
////        }
////    }
////}
//
//package com.advancedBigDataIndexing.PlanService.serviceImpl;
//
//import co.elastic.clients.elasticsearch.core.DeleteRequest;
//import co.elastic.clients.elasticsearch.core.IndexRequest;
//import co.elastic.clients.elasticsearch.core.UpdateRequest;
//import com.advancedBigDataIndexing.PlanService.model.Plan;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class RabbitMQConsumer {
//
//    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
//    private final ObjectMapper objectMapper;
//    private final ElasticsearchClient elasticsearchClient;
//
//    public RabbitMQConsumer(ObjectMapper objectMapper, ElasticsearchClient elasticsearchClient) {
//        this.objectMapper = objectMapper;
//        this.elasticsearchClient = elasticsearchClient;
//    }
//
//    @RabbitListener(queues = "plan-queue")
//    public void consumeMessage(String message) {
//        try {
//            logger.info("Received message: {}", message);
//
//            // Parse the incoming message to JsonNode
//            JsonNode jsonNode = objectMapper.readTree(message);
//
//            // Check if the "action" field is present in the message
//            if (!jsonNode.has("action")) {
//                logger.warn("Received message without an action field: {}", message);
//                return;
//            }
//
//            String action = jsonNode.get("action").asText();
//            logger.info("Processing action: {}", action);
//
//            // Determine parentId if available
//            String parentId = jsonNode.has("parentId") ? jsonNode.get("parentId").asText() : null;
//
//            // Switch based on the action type (index, delete, update)
//            switch (action.toLowerCase()) {
//                case "index":
//                    handleIndexMessage(jsonNode.get("data"), parentId);
//                    break;
//                case "delete":
//                    handleDeleteMessage(jsonNode.get("data"));
//                    break;
//                case "update":
//                    handleUpdateMessage(jsonNode.get("data"));
//                    break;
//                default:
//                    logger.warn("Unknown action received: {}", action);
//            }
//        } catch (JsonProcessingException e) {
//            logger.error("Invalid JSON format in message: {}", message, e);
//        } catch (Exception e) {
//            logger.error("Failed to process message from RabbitMQ", e);
//        }
//    }
//
//private void handleIndexMessage(JsonNode dataNode, String parentId) {
//    try {
//        if (dataNode == null) {
//            logger.warn("Indexing message missing 'data' field.");
//            return;
//        }
//
//        String id = dataNode.get("objectId").asText();
//        ObjectNode joinField = objectMapper.createObjectNode();
//
//        // Determine whether it's a parent or child
//        if (parentId == null) {
//            joinField.put("name", "plan"); // Parent document
//        } else {
//            joinField.put("name", "linked_service"); // Child document
//            joinField.put("parent", parentId); // Link to parent
//        }
//
//        // Add the join_field to the document
//        ((ObjectNode) dataNode).set("join_field", joinField);
//
//        // Create an Elasticsearch index request
//        var indexRequest = new IndexRequest.Builder<>()
//                .index("plan-index")
//                .id(id)
//                .document(dataNode)
//                .routing(parentId) // Routing for child documents
//                .build();
//
//        // Index the document
//        elasticsearchClient.index(indexRequest);
//        logger.info("Successfully indexed document with ID: {}", id);
//    } catch (Exception e) {
//        logger.error("Failed to index document", e);
//    }
//}
//
//
//    private ObjectNode createJoinField(String parentId) {
//        if (parentId == null) {
//            // Parent document; no join field required
//            ObjectNode joinField = objectMapper.createObjectNode();
//            joinField.put("name", "plan");
//            return joinField;
//        }
//
//        // Child document; define the relationship
//        ObjectNode joinField = objectMapper.createObjectNode();
//        if (parentId.contains("cost_share")) {
//            joinField.put("name", "cost_share");
//        } else if (parentId.contains("linked_service")) {
//            joinField.put("name", "linked_service");
//        }
//        joinField.put("parent", parentId);
//        return joinField;
//    }
//
//
//    private void handleDeleteMessage(JsonNode dataNode) {
//        try {
//            if (dataNode == null) {
//                logger.warn("Deletion message missing 'data' field.");
//                return;
//            }
//
//            String documentId = dataNode.asText();
//            var deleteRequest = new DeleteRequest.Builder()
//                    .index("plan-index")
//                    .id(documentId)
//                    .build();
//
//            // Delete the document from Elasticsearch
//            elasticsearchClient.delete(deleteRequest);
//            logger.info("Successfully deleted document with ID: {}", documentId);
//        } catch (Exception e) {
//            logger.error("Failed to delete document", e);
//        }
//    }
//
//    private void handleUpdateMessage(JsonNode dataNode) {
//        try {
//            if (dataNode == null) {
//                logger.warn("Update message missing 'data' field.");
//                return;
//            }
//
//            // Deserialize the incoming JSON into a Plan object
//            Plan plan = objectMapper.treeToValue(dataNode, Plan.class);
//
//            // Check if the plan object has an ID
//            if (plan.getObjectId() == null || plan.getObjectId().isEmpty()) {
//                logger.warn("Received Plan object without an ID for update.");
//                return;
//            }
//
//            logger.info("Processing update request for Plan: {}", plan);
//
//            // Prepare the update request using UpdateRequest.Builder<Plan, Plan>
//            var updateRequest = new UpdateRequest.Builder<Plan, Plan>()
//                    .index("plan-index")
//                    .id(plan.getObjectId())  // The ID of the document to update
//                    .doc(plan)  // The document to update (new data to replace)
//                    .build();
//
//            // Execute the update request
//            elasticsearchClient.update(updateRequest, Plan.class);
//
//            logger.info("Successfully updated plan with ID: {}", plan.getObjectId());
//        } catch (Exception e) {
//            logger.error("Failed to update Plan object", e);
//        }
//    }}

package com.advancedBigDataIndexing.PlanService.serviceImpl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.*;
import com.advancedBigDataIndexing.PlanService.model.Plan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final ObjectMapper objectMapper;
    private final ElasticsearchClient elasticsearchClient;

    public RabbitMQConsumer(ObjectMapper objectMapper, ElasticsearchClient elasticsearchClient) {
        this.objectMapper = objectMapper;
        this.elasticsearchClient = elasticsearchClient;
    }

    @RabbitListener(queues = "plan-queue")
    public void consumeMessage(String message) {
        try {
            logger.info("Received message: {}", message);

            // Parse the incoming message to JsonNode
            JsonNode jsonNode = objectMapper.readTree(message);

            // Check if the "action" field is present in the message
            if (!jsonNode.has("action")) {
                logger.warn("Received message without an action field: {}", message);
                return;
            }

            String action = jsonNode.get("action").asText();
            logger.info("Processing action: {}", action);

            // Extract parentId if available
            String parentId = jsonNode.has("parentId") ? jsonNode.get("parentId").asText() : null;

            // Handle action based on type
            switch (action.toLowerCase()) {
                case "index":
                    handleIndexMessage(jsonNode.get("data"), parentId);
                    break;
                case "delete":
                    handleDeleteMessage(jsonNode.get("data"));
                    break;
                case "update":
                    handleUpdateMessage(jsonNode.get("data"));
                    break;
                default:
                    logger.warn("Unknown action received: {}", action);
            }
        } catch (JsonProcessingException e) {
            logger.error("Invalid JSON format in message: {}", message, e);
        } catch (Exception e) {
            logger.error("Failed to process message from RabbitMQ", e);
        }
    }

    private void handleIndexMessage(JsonNode dataNode, String parentId) {
        try {
            if (dataNode == null || !dataNode.has("objectId")) {
                logger.warn("Indexing message missing 'data' or 'objectId' field.");
                return;
            }

            String objectId = dataNode.get("objectId").asText();

            // Handle parent document
            if (parentId == null) {
                // Create a deep copy to avoid modifying the original JSON
                ObjectNode parentDocument = (ObjectNode) dataNode.deepCopy();

                // Remove the linkedPlanServices field from the parent document
                parentDocument.remove("linkedPlanServices");

                // Add join_field for the parent document
                ObjectNode joinField = objectMapper.createObjectNode();
                joinField.put("name", "plan");
                parentDocument.set("join_field", joinField);

                // Index the parent document
                var indexRequest = new IndexRequest.Builder<>()
                        .index("plan-index")
                        .id(objectId)
                        .document(parentDocument)
                        .build();

                elasticsearchClient.index(indexRequest);
                logger.info("Indexed parent document with ID: {}", objectId);

                // Process child documents in linkedPlanServices
                if (dataNode.has("linkedPlanServices")) {
                    ArrayNode linkedPlanServices = (ArrayNode) dataNode.get("linkedPlanServices");
                    for (JsonNode linkedServiceNode : linkedPlanServices) {
                        indexChildDocument(linkedServiceNode, objectId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to index document", e);
        }
    }


    private void indexChildDocument(JsonNode childNode, String parentId) {
        try {
            if (!childNode.has("objectId")) {
                logger.warn("Child document missing 'objectId' field.");
                return;
            }

            String childId = childNode.get("objectId").asText();
            ObjectNode documentNode = (ObjectNode) childNode.deepCopy();
            ObjectNode joinField = objectMapper.createObjectNode();
            joinField.put("name", "linked_service");
            joinField.put("parent", parentId);
            documentNode.set("join_field", joinField);

            var indexRequest = new IndexRequest.Builder<>()
                    .index("plan-index")
                    .id(childId)
                    .document(documentNode)
                    .routing(parentId) // Associate child with parent
                    .build();

            elasticsearchClient.index(indexRequest);
            logger.info("Indexed child document with ID: {} and parent ID: {}", childId, parentId);
        } catch (Exception e) {
            logger.error("Failed to index child document", e);
        }
    }

    private void handleDeleteMessage(JsonNode dataNode) {
        try {
            if (dataNode == null) {
                logger.warn("Deletion message missing 'data' field.");
                return;
            }

            // Extract the document ID (assumed to be the parent ID)
            String documentId = dataNode.asText();

            // Delete all child documents
            deleteChildDocuments(documentId);

            // Delete the parent document
            var deleteRequest = new DeleteRequest.Builder()
                    .index("plan-index")
                    .id(documentId)
                    .build();

            elasticsearchClient.delete(deleteRequest);
            logger.info("Successfully deleted parent document with ID: {}", documentId);

            try {
                // Create a match_all query
                Query matchAllQuery = new Query.Builder()
                        .matchAll(m -> m) // Match all documents
                        .build();

                // Build the DeleteByQueryRequest
                DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                        .index("plan-index") // Specify the index name
                        .query(matchAllQuery) // Set the match_all query
                        .refresh(true) // Ensure the changes are immediately visible
                        .build();

                // Execute the delete_by_query request
                var response = elasticsearchClient.deleteByQuery(deleteByQueryRequest);

                // Log the number of documents deleted
            } catch (Exception e) {
            }
        } catch (Exception e) {
            logger.error("Failed to delete document", e);
        }
    }

//    private void deleteChildDocuments(String parentId) {
//        try {
//            Query query = new Query.Builder()
//                    .bool(new BoolQuery.Builder()
//                            .must(new Query.Builder()
//                                    .term(new TermQuery.Builder()
//                                            .field("join_field.parent")
//                                            .value(parentId)
//                                            .build())
//                                    .build())
//                            .must(new Query.Builder()
//                                    .term(new TermQuery.Builder()
//                                            .field("join_field.name")
//                                            .value("linked_service")
//                                            .build())
//                                    .build())
//                            .build())
//                    .build();// Query Elasticsearch to find all child documents linked to the parent
//            var searchRequest = new SearchRequest.Builder()
//                    .index("plan-index")
//                    .query(query
//                    )
//                    .size(1000) // Adjust size for the expected number of children
//                    .build();
//
//            var searchResponse = elasticsearchClient.search(searchRequest, ObjectNode.class);
//            logger.info("Search response for children of parent ID {}: {}", parentId, searchResponse);
//
//            // Extract child document IDs
//            var childDocs = searchResponse.hits().hits();
//            for (var childDoc : childDocs) {
//                String childId = childDoc.id();
//
//                // Delete each child document
//                var deleteRequest = new DeleteRequest.Builder()
//                        .index("plan-index")
//                        .id(childId)
//                        .build();
//
//                elasticsearchClient.delete(deleteRequest);
//                logger.info("Successfully deleted child document with ID: {}", childId);
//            }
//
//            logger.info("Successfully deleted all child documents for parent ID: {}", parentId);
//        } catch (Exception e) {
//            logger.error("Failed to delete child documents for parent ID: {}", parentId, e);
//        }
//    }

    private void deleteChildDocuments(String parentId) {
        try {
            // Build the query to find all child documents linked to the parent
            Query query = new Query.Builder()
                    .bool(b -> b
                            .must(m -> m
                                    .term(t -> t
                                            .field("join_field.parent")
                                            .value(parentId)
                                    )
                            )
                            .must(m -> m
                                    .term(t -> t
                                            .field("join_field.name")
                                            .value("linked_service")
                                    )
                            )
                    )
                    .build();

            // Create the DeleteByQueryRequest
            var deleteByQueryRequest = new DeleteByQueryRequest.Builder()
                    .index("plan-index")
                    .query(query)
                    .refresh(true) // Ensures immediate reflection of deletion
                    .build();

            // Execute the delete_by_query operation
            var response = elasticsearchClient.deleteByQuery(deleteByQueryRequest);

            logger.info("Deleted {} child documents for parent ID: {}", response.deleted(), parentId);
        } catch (Exception e) {
            logger.error("Failed to delete child documents for parent ID: {}", parentId, e);
        }
    }

//    private void handleUpdateMessage(JsonNode dataNode) {
//        try {
//            if (dataNode == null) {
//                logger.warn("Update message missing 'data' field.");
//                return;
//            }
//
//            // Deserialize the incoming JSON into a Plan object
//            Plan plan = objectMapper.treeToValue(dataNode, Plan.class);
//
//            if (plan.getObjectId() == null || plan.getObjectId().isEmpty()) {
//                logger.warn("Received Plan object without an ID for update.");
//                return;
//            }
//
//            logger.info("Processing update request for Plan: {}", plan);
//
//            // Build the update request
//            var updateRequest = new UpdateRequest.Builder<Plan, Plan>()
//                    .index("plan-index")
//                    .id(plan.getObjectId())
//                    .doc(plan)
//                    .build();
//
//            // Execute update operation
//            elasticsearchClient.update(updateRequest, Plan.class);
//            logger.info("Successfully updated plan with ID: {}", plan.getObjectId());
//        } catch (Exception e) {
//            logger.error("Failed to update Plan object", e);
//        }
//    }
//}


    private void handleUpdateMessage(JsonNode dataNode) {
        try {
            if (dataNode == null) {
                logger.warn("Update message missing 'data' field.");
                return;
            }

            // Process the parent plan document
            if (dataNode.has("objectId") && !dataNode.has("join_field")) {
                handleParentUpdate(dataNode);
            }

            // Process linked services if present
            if (dataNode.has("linkedPlanServices")) {
                ArrayNode linkedServices = (ArrayNode) dataNode.get("linkedPlanServices");
                String parentId = dataNode.get("objectId").asText();

                for (JsonNode linkedServiceNode : linkedServices) {
                    handleChildUpdate(linkedServiceNode, parentId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process update message", e);
        }
    }

    private void handleParentUpdate(JsonNode dataNode) {
        try {
            // Deserialize the incoming JSON into a Plan object
            Plan plan = objectMapper.treeToValue(dataNode, Plan.class);

            if (plan.getObjectId() == null || plan.getObjectId().isEmpty()) {
                logger.warn("Received Plan object without an ID for update.");
                return;
            }

            logger.info("Processing update request for Plan: {}", plan);

            // Build the update request
            var updateRequest = new UpdateRequest.Builder<Plan, Plan>()
                    .index("plan-index")
                    .id(plan.getObjectId())
                    .doc(plan)
                    .build();

            // Execute update operation
            elasticsearchClient.update(updateRequest, Plan.class);
            logger.info("Successfully updated plan with ID: {}", plan.getObjectId());
        } catch (Exception e) {
            logger.error("Failed to update Plan object", e);
        }
    }

    private void handleChildUpdate(JsonNode childNode, String parentId) {
        try {
            if (!childNode.has("objectId")) {
                logger.warn("Child document missing 'objectId' field.");
                return;
            }

            String childId = childNode.get("objectId").asText();

            logger.info("Processing update request for Child: {}, Parent: {}", childId, parentId);

            // Check if the child document exists
            boolean childExists = elasticsearchClient.exists(
                    req -> req.index("plan-index").id(childId)
            ).value();

            if (childExists) {
                // Update the existing child document
                logger.info("Child document with ID: {} exists. Updating it.", childId);

                ObjectNode documentNode = (ObjectNode) childNode.deepCopy();

                var updateRequest = new UpdateRequest.Builder<ObjectNode, ObjectNode>()
                        .index("plan-index")
                        .id(childId)
                        .doc(documentNode)
                        .routing(parentId) // Ensure routing is preserved for parent-child relationship
                        .build();

                elasticsearchClient.update(updateRequest, ObjectNode.class);
                logger.info("Successfully updated child document with ID: {}", childId);
            } else {
                // Index as a new child document
                logger.info("Child document with ID: {} does not exist. Indexing it as new.", childId);

                ObjectNode documentNode = (ObjectNode) childNode.deepCopy();
                ObjectNode joinField = objectMapper.createObjectNode();
                joinField.put("name", "linked_service");
                joinField.put("parent", parentId);
                documentNode.set("join_field", joinField);

                var indexRequest = new IndexRequest.Builder<ObjectNode>()
                        .index("plan-index")
                        .id(childId)
                        .document(documentNode)
                        .routing(parentId) // Associate child with parent
                        .build();

                elasticsearchClient.index(indexRequest);
                logger.info("Successfully indexed new child document with ID: {}", childId);
            }
        } catch (Exception e) {
            logger.error("Failed to process Child document update or index", e);
        }
    }
}