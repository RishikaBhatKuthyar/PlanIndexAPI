//package com.advancedBigDataIndexing.PlanService.config;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.Queue; // Use this import
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Bean
//    public Queue planQueue() {
//        return new Queue("plan-queue", false);
//    }
//
//    @Bean
//    public TopicExchange planExchange() {
//        return new TopicExchange("plan-exchange");
//    }
//
//    @Bean
//    public Binding planBinding(Queue planQueue, TopicExchange planExchange) {
//        return BindingBuilder.bind(planQueue).to(planExchange).with("plan-queue");
//    }
//}


package com.advancedBigDataIndexing.PlanService.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue; // Use this import
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Define the RabbitMQ Queue
    @Bean
    public Queue planQueue() {
        return new Queue("plan-queue", false); // false means non-durable
    }

    // Define the RabbitMQ Exchange (using TopicExchange)
    @Bean
    public TopicExchange planExchange() {
        return new TopicExchange("plan-exchange");
    }

    // Define the binding between Queue and Exchange
    @Bean
    public Binding planBinding(Queue planQueue, TopicExchange planExchange) {
        return BindingBuilder.bind(planQueue).to(planExchange).with("plan-queue");
    }

    // Define the RabbitMQ ConnectionFactory Bean
    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");  // Set your RabbitMQ host
        factory.setPort(5672);         // Set RabbitMQ port (default is 5672)
        factory.setUsername("guest");  // Set your RabbitMQ username
        factory.setPassword("guest");  // Set your RabbitMQ password
        return factory;
    }
}
