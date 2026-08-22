package com.example.avaliacao.services;
import com.example.avaliacao.record.StatusInfo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatusService {
    private final Map<UUID, StatusInfo> statusMap = new ConcurrentHashMap<>();

    public void atualizarStatus(UUID id, String status) {
        statusMap.put(id, new StatusInfo(status, null));
    }

    public void registrarFalha(UUID id, String mensagemErro) {
        statusMap.put(id, new StatusInfo("falha", mensagemErro));
    }

    public StatusInfo buscarStatus(UUID id) {
        return statusMap.getOrDefault(id, new StatusInfo("não encontrado", null));
    }
}
