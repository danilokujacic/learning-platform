package com.kujacic.users.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {
    @Bean
    public Queue coursesQueue() {
        return QueueBuilder.durable("courses-queue")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-dead-letter-exchange", "dlx")
                .build();
    }

    @Bean
    public Queue courseCertificatesQueue() {
        return QueueBuilder.durable("course-certificates-queue")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-dead-letter-exchange", "dlx")
                .build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setReturnsCallback(returned -> {
            log.error("Message returned! Exchange: {}, RoutingKey: {}, ReplyCode: {}, ReplyText: {}",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText());
        });

        return template;
    }


    @Bean
    public TopicExchange courseExchange() {
        return new TopicExchange("course-exchange", true, false);
    }

    @Bean
    public Binding courseLevelPassed() {
        return BindingBuilder
                .bind(coursesQueue())
                .to(courseExchange())
                .with("course-level.passed");
    }

    @Bean Binding courseCertificateIssued() {
        return BindingBuilder.bind((courseCertificatesQueue())).to(courseExchange()).with("course-certificate.issued");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}