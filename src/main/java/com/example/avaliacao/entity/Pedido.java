package com.example.avaliacao.entity;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class Pedido {
    private UUID id;

    @NotBlank(message = "Deve ser informado o nome do Produto")
    private String produto;

    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private int quantidade;

    @NotNull(message = "Deve ser informado uma data de criação")
    private LocalDateTime dataCriacao;

    public Pedido(UUID id, String produto, int quantidade, LocalDateTime dataCriacao) {
        this.id = id;
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataCriacao = dataCriacao;
    }

    public Pedido() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
