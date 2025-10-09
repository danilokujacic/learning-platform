package com.kujacic.courses.config;


import lombok.extern.slf4j.Slf4j;
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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("Message confirmed: {}", correlationData);
            } else {
                log.error("Message not confirmed: {}, cause: {}", correlationData, cause);
            }
        });

        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned.getMessage());
        });

        template.setMandatory(true);
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
