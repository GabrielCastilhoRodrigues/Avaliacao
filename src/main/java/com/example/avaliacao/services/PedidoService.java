package com.example.avaliacao.services;

import com.example.avaliacao.configuration.RabbitMQConfig;
import com.example.avaliacao.entity.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PedidoService {

    private static final Logger logger = LoggerFactory.getLogger(PedidoService.class);

    private final RabbitTemplate rabbitTemplate;
    private final StatusService statusService;

    public PedidoService(RabbitTemplate rabbitTemplate, StatusService statusService) {
        this.rabbitTemplate = rabbitTemplate;
        this.statusService = statusService;
    }

    public Pedido publicar(Pedido pedido) {
        if (pedido.getId() == null) {
            pedido.setId(UUID.randomUUID());
        }

        statusService.atualizarStatus(pedido.getId(), "recebido");
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_ENTRADA, pedido);
        logger.info("Pedido {} publicado na fila {}", pedido.getId(), RabbitMQConfig.QUEUE_ENTRADA);

        return pedido;
    }
}
