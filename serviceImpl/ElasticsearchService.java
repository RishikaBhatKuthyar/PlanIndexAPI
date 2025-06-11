package com.advancedBigDataIndexing.PlanService.serviceImpl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.advancedBigDataIndexing.PlanService.model.LinkedService;
import com.advancedBigDataIndexing.PlanService.model.Plan;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;

@Service
public class ElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "plan-index";
    private final ObjectMapper objectMapper; // Add ObjectMapper here

    @Autowired
    public ElasticsearchService(ElasticsearchClient elasticsearchClient, ObjectMapper objectMapper) {
        this.elasticsearchClient = elasticsearchClient;
        this.objectMapper = objectMapper;
    }

    public void initializeIndex() throws IOException {
        String indexName = "plan-index";

        // Check if the index already exists
        boolean indexExists = elasticsearchClient.indices()
                .exists(e -> e.index(indexName))
                .value();

        if (!indexExists) {
            // Define the index mapping with join_field
            String indexMapping = """
        {
          "mappings": {
            "properties": {
              "join_field": {
                "type": "join",
                "relations": {
                  "plan": "linked_service"
                }
              },
              "objectId": {
                "type": "keyword"
              },
              "planStatus": {
                "type": "keyword"
              },
              "linkedService.name": {
                "type": "text"
              },
              "creationDate": {
                "type": "date",
                "format": "dd-MM-yyyy"
              }
            }
          }
        }
        """;

            // Create the index with the defined mapping
            CreateIndexRequest request = new CreateIndexRequest.Builder()
                    .index(indexName)
                    .withJson(new StringReader(indexMapping))
                    .build();

            var response = elasticsearchClient.indices().create(request);

            if (response.acknowledged()) {
                logger.info("Successfully created index '{}' with join_field.", indexName);
            } else {
                logger.error("Failed to create index '{}'.", indexName);
            }
        } else {
            logger.info("Index '{}' already exists.", indexName);
        }
    }


    /**
     * Method to delete and recreate the Elasticsearch index.
     */
    public void recreateIndex() throws IOException {
        // Check if the index exists
        boolean indexExists = elasticsearchClient.indices()
                .exists(e -> e.index(INDEX_NAME))
                .value();

        // Delete the index if it exists
        if (indexExists) {
            elasticsearchClient.indices().delete(d -> d.index(INDEX_NAME));
            logger.info("Deleted the existing index '{}'.", INDEX_NAME);
        }

        // Recreate the index
        initializeIndex();
        logger.info("Recreated the index '{}'.", INDEX_NAME);
    }

    /**
     * Method to index a Plan document.
     */
    public void indexPlanDocument(String documentId, Plan planDocument) throws IOException {
        logger.info("Indexing parent document with ID: {}", documentId);

        IndexRequest<Plan> indexRequest = new IndexRequest.Builder<Plan>()
                .index("plan-index")
                .id(documentId)
                .document(planDocument)
                .build();

        elasticsearchClient.index(indexRequest);
    }

    /**
     * Method to index a child document linked to a parent.
     */
    public void indexChildDocument(String parentId, String childId, LinkedService childDocument) throws IOException {
        logger.info("Indexing child document with ID: {} and parent ID: {}", childId, parentId);

        ObjectNode documentNode = objectMapper.valueToTree(childDocument);
        ObjectNode joinField = objectMapper.createObjectNode();
        joinField.put("name", "linked_service");
        joinField.put("parent", parentId);

        documentNode.set("join_field", joinField);

        IndexRequest<ObjectNode> indexRequest = new IndexRequest.Builder<ObjectNode>()
                .index("plan-index")
                .id(childId)
                .document(documentNode)
                .routing(parentId)  // Ensures the child is linked to the parent
                .build();

        elasticsearchClient.index(indexRequest);
    }

}
