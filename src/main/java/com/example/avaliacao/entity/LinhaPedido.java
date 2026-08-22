package com.example.avaliacao.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class LinhaPedido {

    public static final String STATUS_INICIAL = "ENVIADO, AGUARDANDO PROCESSO";

    private final UUID id;
    private final String produto;
    private final int quantidade;
    private String status;
    private final LocalDateTime dataCriacao;
    private String mensagemErro;

    public LinhaPedido(UUID id, String produto, int quantidade, LocalDateTime dataCriacao) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.status = STATUS_INICIAL;
        this.dataCriacao = dataCriacao;
    }

    public UUID getId() {
        return id;
    }

    public String getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public boolean aguardandoProcessamento() {
        if (status == null) {
            return true;
        }
        String s = status.toLowerCase();
        return !s.contains("sucesso") && !s.contains("falha");
    }
}
