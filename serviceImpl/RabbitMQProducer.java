//package com.advancedBigDataIndexing.PlanService.serviceImpl;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.rabbitmq.client.*;
//import com.advancedBigDataIndexing.PlanService.model.Plan;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class RabbitMQProducer {
//
//    private final ObjectMapper objectMapper;
//    private final ConnectionFactory connectionFactory;
//
//    public RabbitMQProducer(ObjectMapper objectMapper, ConnectionFactory connectionFactory) {
//        this.objectMapper = objectMapper;
//        this.connectionFactory = connectionFactory;
//    }
//
//    public void sendMessage(Plan plan, String action) {
//        try (Connection connection = connectionFactory.newConnection();
//             Channel channel = connection.createChannel()) {
//
//            // Declare the queue
//            channel.queueDeclare("plan-queue", false, false, false, null);
//
//            // Create the message with action and data
//            String message = objectMapper.writeValueAsString(
//                    Map.of(
//                            "action", action,
//                            "data", plan
//                    )
//            );
//
//            // Send the message to RabbitMQ
//            channel.basicPublish("", "plan-queue", null, message.getBytes());
//
//            // Log the sent message
//            System.out.println("Sent message: " + message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}




package com.advancedBigDataIndexing.PlanService.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.advancedBigDataIndexing.PlanService.model.Plan;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitMQProducer {

    private final ObjectMapper objectMapper;
    private final ConnectionFactory connectionFactory;

    public RabbitMQProducer(ObjectMapper objectMapper, ConnectionFactory connectionFactory) {
        this.objectMapper = objectMapper;
        this.connectionFactory = connectionFactory;
    }

    public void sendParentMessage(Plan plan, String action) {
        sendMessage(plan, action, null);
    }

    public void sendChildMessage(Object childObject, String action, String parentId) {
        sendMessage(childObject, action, parentId);
    }

    private void sendMessage(Object data, String action, String parentId) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare the queue
            channel.queueDeclare("plan-queue", false, false, false, null);

            // Create the message with action and data
            Map<String, Object> messagePayload = Map.of(
                    "action", action,
                    "data", data,
                    "parentId", parentId
            );

            String message = objectMapper.writeValueAsString(messagePayload);

            // Send the message to RabbitMQ
            channel.basicPublish("", "plan-queue", null, message.getBytes());

            // Log the sent message
            System.out.println("Sent message: " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
