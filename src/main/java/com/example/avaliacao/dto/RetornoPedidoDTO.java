package com.example.avaliacao.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class RetornoPedidoDTO {
    private String status;
    private UUID id;

    public RetornoPedidoDTO(String status, UUID id) {
        this.status = status;
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
