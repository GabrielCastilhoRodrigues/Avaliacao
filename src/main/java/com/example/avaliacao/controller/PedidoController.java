package com.example.avaliacao.controller;

import com.example.avaliacao.configuration.RabbitMQConfig;
import com.example.avaliacao.dto.RetornoPedidoDTO;
import com.example.avaliacao.entity.Pedido;
import com.example.avaliacao.record.StatusInfo;
import com.example.avaliacao.services.PedidoService;
import com.example.avaliacao.services.StatusService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final StatusService statusService;

    public PedidoController(PedidoService pedidoService, StatusService statusService) {
        this.pedidoService = pedidoService;
        this.statusService = statusService;
    }

    @PostMapping("/")
    public ResponseEntity<RetornoPedidoDTO> criaPedido(@Valid @RequestBody Pedido pedido) {
        Pedido publicado = pedidoService.publicar(pedido);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new RetornoPedidoDTO("recebido", publicado.getId()));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<StatusInfo> consultarStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(statusService.buscarStatus(id));
    }
}
