package com.example.avaliacao.consumer;

import com.example.avaliacao.configuration.RabbitMQConfig;
import com.example.avaliacao.entity.Pedido;
import com.example.avaliacao.entity.StatusPedido;
import com.example.avaliacao.exceptions.ExcessaoProcessamento;
import com.example.avaliacao.services.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Random;

@Component
public class PedidoConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PedidoConsumer.class);
    private final Random random = new Random();

    private final RabbitTemplate rabbitTemplate;
    private final StatusService statusService;

    public PedidoConsumer(RabbitTemplate rabbitTemplate, StatusService statusService) {
        this.rabbitTemplate = rabbitTemplate;
        this.statusService = statusService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENTRADA)
    public void processar(Pedido pedido) {
        logger.info("Iniciando processamento do pedido {}", pedido.getId());
        statusService.atualizarStatus(pedido.getId(), "processando");

        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 segundos

            if (random.nextDouble() < 0.2) {
                throw new ExcessaoProcessamento("Falha simulada no pedido " + pedido.getId());
            }

            StatusPedido status = new StatusPedido(pedido.getId(), "SUCESSO", LocalDateTime.now(), null);
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_STATUS_SUCESSO, status);
            statusService.atualizarStatus(pedido.getId(), "sucesso");
            logger.info("Pedido {} processado com sucesso", pedido.getId());

        } catch (ExcessaoProcessamento e) {
            logger.error("Falha ao processar pedido {}: {}", pedido.getId(), e.getMessage());

            statusService.registrarFalha(pedido.getId(), e.getMessage());

            StatusPedido status = new StatusPedido(pedido.getId(), "FALHA", null, e.getMessage());
            rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_STATUS_FALHA, status);

            throw new AmqpRejectAndDontRequeueException(e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
