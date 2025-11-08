package com.kujacic.courses.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
@Slf4j
public class RabbitConfig {
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
    public Queue courseCertificatesQueue() {
        return QueueBuilder.durable("certificate-request-queue")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-dead-letter-exchange", "dlx")
                .build();
    }

    @Bean
    public TopicExchange usersExchange() {
        return new TopicExchange("users-exchange", true, false);
    }

    @Bean
    public Binding courseLevelPassed() {
        return BindingBuilder
                .bind(courseCertificatesQueue())
                .to(usersExchange())
                .with("course-certificate.requested");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
