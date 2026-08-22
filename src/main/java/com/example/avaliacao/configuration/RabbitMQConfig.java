package com.example.avaliacao.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_ENTRADA = "pedidos.entrada.gabriel-castilho";
    public static final String QUEUE_DLQ = "pedidos.entrada.gabriel-castilho.dlq";
    public static final String QUEUE_STATUS_SUCESSO = "pedidos.status.sucesso.gabriel-castilho";
    public static final String QUEUE_STATUS_FALHA = "pedidos.status.falha.gabriel-castilho";

    private static final String DLX_EXCHANGE = "pedidos.entrada.gabriel-castilho.dlx";

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue filaDLQ() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding bindingDLQ() {
        return BindingBuilder.bind(filaDLQ()).to(dlxExchange()).with(QUEUE_DLQ);
    }

    @Bean
    public Queue filaEntrada() {
        return QueueBuilder.durable(QUEUE_ENTRADA)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue filaStatusSucesso() {
        return QueueBuilder.durable(QUEUE_STATUS_SUCESSO).build();
    }

    @Bean
    public Queue filaStatusFalha() {
        return QueueBuilder.durable(QUEUE_STATUS_FALHA).build();
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}