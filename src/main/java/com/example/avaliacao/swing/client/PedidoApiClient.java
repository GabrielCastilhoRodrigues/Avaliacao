package com.example.avaliacao.swing.client;

import com.example.avaliacao.entity.Pedido;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class PedidoApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/pedidos";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PedidoApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public void enviarPedido(Pedido pedido) throws ApiException {
        try {
            String json = objectMapper.writeValueAsString(pedido);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 202 || status == 200 || status == 201) {
                return;
            }

            if (status == 400) {
                throw new ApiException(extrairMensagemValidacao(response.body()));
            }

            throw new ApiException("O servidor respondeu com erro (HTTP " + status + ").");

        } catch (ApiException e) {
            throw e;
        } catch (java.net.ConnectException e) {
            throw new ApiException("Nao foi possivel conectar ao servidor.\n"
                    + "Verifique se o backend esta rodando em " + BASE_URL + ".");
        } catch (java.net.http.HttpTimeoutException e) {
            throw new ApiException("O servidor demorou demais para responder. Tente novamente.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("O envio foi interrompido.");
        } catch (Exception e) {
            throw new ApiException("Erro inesperado ao enviar o pedido: " + e.getMessage());
        }
    }

    public StatusResposta consultarStatus(UUID id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/status/" + id))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode node = objectMapper.readTree(response.body());
            String status = node.has("status") ? node.get("status").asString() : null;
            String erro = node.has("mensagemErro") && !node.get("mensagemErro").isNull()
                    ? node.get("mensagemErro").asString()
                    : null;

            return status == null ? null : new StatusResposta(status, erro);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public record StatusResposta(String status, String mensagemErro) {}

    private String extrairMensagemValidacao(String body) {
        String padrao = "Dados invalidos. Verifique se o produto foi preenchido "
                + "e se a quantidade e maior que zero.";

        if (body == null || body.isBlank()) {
            return padrao;
        }

        try {
            JsonNode node = objectMapper.readTree(body);
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                String chave = entry.getKey();
                if (!chave.equals("timestamp") && !chave.equals("status")
                        && !chave.equals("error") && !chave.equals("path")) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append("- ").append(entry.getValue().asString());
                }
            }

            if (!sb.isEmpty()) {
                return "Dados invalidos:\n" + sb;
            }
        } catch (Exception ignored) {
            throw new RuntimeException("Formato do JSON diferente do esperado");
        }

        return padrao;
    }

    public static class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }
    }
}
