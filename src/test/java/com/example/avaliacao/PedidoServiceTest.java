package com.example.avaliacao;

import com.example.avaliacao.configuration.RabbitMQConfig;
import com.example.avaliacao.entity.Pedido;
import com.example.avaliacao.services.PedidoService;
import com.example.avaliacao.services.StatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StatusService statusService;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    @DisplayName("Deve publicar o pedido na fila de entrada")
    void devePublicarPedidoNaFilaDeEntrada() {
        Pedido pedido = new Pedido();
        pedido.setProduto("Notebook");
        pedido.setQuantidade(2);

        pedidoService.publicar(pedido);

        verify(rabbitTemplate, times(1))
                .convertAndSend(eq(RabbitMQConfig.QUEUE_ENTRADA), any(Pedido.class));
    }

    @Test
    @DisplayName("Deve gerar id quando nao informado")
    void deveGerarIdQuandoNaoInformado() {
        Pedido pedido = new Pedido();
        pedido.setProduto("Mouse");
        pedido.setQuantidade(1);
        pedido.setDataCriacao(LocalDateTime.now());

        Pedido resultado = pedidoService.publicar(pedido);

        assertThat(resultado.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve preservar a dataCriacao informada pelo cliente")
    void devePreservarDataCriacao() {
        LocalDateTime data = LocalDateTime.of(2026, 8, 22, 10, 30);
        Pedido pedido = new Pedido();
        pedido.setProduto("Monitor");
        pedido.setQuantidade(1);
        pedido.setDataCriacao(data);

        Pedido resultado = pedidoService.publicar(pedido);

        assertThat(resultado.getDataCriacao()).isEqualTo(data);
    }

    @Test
    @DisplayName("Deve preservar o id enviado pelo cliente")
    void devePreservarIdDoCliente() {
        UUID idCliente = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(idCliente);
        pedido.setProduto("Teclado");
        pedido.setQuantidade(3);

        pedidoService.publicar(pedido);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.QUEUE_ENTRADA), captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(idCliente);
    }

    @Test
    @DisplayName("Deve registrar o status inicial como recebido")
    void deveRegistrarStatusInicial() {
        Pedido pedido = new Pedido();
        pedido.setProduto("Monitor");
        pedido.setQuantidade(1);

        Pedido resultado = pedidoService.publicar(pedido);

        verify(statusService).atualizarStatus(resultado.getId(), "recebido");
    }
}
