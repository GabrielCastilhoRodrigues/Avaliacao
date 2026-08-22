# Sistema de Pedidos Desktop Assíncrono

Teste técnico para desenvolvedor Java. A ideia é simples: uma aplicação desktop em Swing envia pedidos para um serviço Spring Boot, que processa tudo de forma assíncrona via RabbitMQ e devolve o status — que a tela acompanha por polling.

**Stacks:** Java 21, Spring Boot 4.1.1, RabbitMQ, Java Swing, JUnit 5 + Mockito.

## Como rodar

Você vai precisar de JDK 21, Maven e Docker.

**1.** Clone e entre na pasta:
```bash
git clone https://github.com/GabrielCastilhoRodrigues/Avaliacao.git
cd Avaliacao
```

**2.** Suba o RabbitMQ:
```bash
docker run -d --hostname meu-rabbit --name rabbitmq-local -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```
O painel fica em http://localhost:15672 (guest / guest). Se o container já existir, só `docker start rabbitmq-local`.

**3.** Compile:
```bash
mvn clean install -U
```

**4.** Rode a classe `AvaliacaoApplication` e espere o Tomcat subir na porta 8080. As filas e a DLQ são criadas sozinhas na inicialização.

**5.** Com o backend no ar, rode `SwingApp`:

![Tela inicial](img/imagem%20inicial.png)

Preenche produto, quantidade e data, clica em enviar. A tabela se atualiza a cada 3 segundos.

Para rodar os testes: `mvn test`.

## Como funciona
* O Swing gera o UUID e manda o pedido via `POST /api/pedidos/`. Se passar na validação (produto preenchido, quantidade maior que zero, data informada), o backend publica na fila e responde 202 com o id — senão, 400 com o que deu errado.
* Do outro lado, o consumidor pega a mensagem, simula um processamento de 1 a 3 segundos e, em 20% das vezes, força uma falha. No caminho feliz, publica em `pedidos.status.sucesso.gabriel-castilho`. Quando falha, publica em `pedidos.status.falha.gabriel-castilho` e rejeita a mensagem original, que cai na DLQ.
* A fila de entrada é declarada com `x-dead-letter-exchange` apontando para uma exchange ligada à `pedidos.entrada.gabriel-castilho.dlq`. Quando o consumidor lança `AmqpRejectAndDontRequeueException`, o RabbitMQ faz o roteamento automaticamente.
* Enquanto isso, o Swing consulta `GET /api/pedidos/status/{id}` a cada 3 segundos para os pedidos que ainda não terminaram, e atualiza a linha correspondente na tabela.

## Estrutura

```
src/main/java/com/example/avaliacao/
├── configuration/   Filas, DLX e bindings do RabbitMQ
├── consumer/        Processamento assíncrono
├── controller/      Endpoints REST
├── entity/          Pedido, StatusPedido, LinhaPedido
├── services/        Publicação na fila e controle de status
└── swing/           Aplicação desktop

src/test/java/.../PedidoServiceTest.java
```

## Notas de desenvolvimento

* **Sobre o RabbitMQ do enunciado:** tentei usar o servidor do CloudAMQP, mas não passei da autenticação — a aplicação retornava `ACCESS_REFUSED` e o painel web recusava o login com as mesmas credenciais.

![Erro de login](img/erro%20de%20login.png)

* Para não travar o desenvolvimento, subi o RabbitMQ local no Docker, mantendo a mesma estrutura de filas e roteamento. Trocar para o servidor remoto é só questão de definir as variáveis de ambiente — o código não muda.

**Algumas decisões que tomei pelo caminho:**

* Os status ficam em memória num `ConcurrentHashMap`, já que o consumidor escreve neles numa thread e as requisições HTTP leem em outras.
* O UUID é gerado no Swing e enviado no payload; o backend só cria um novo se vier nulo, o que cobre chamadas diretas na API.
* No polling usei `javax.swing.Timer` com `SwingWorker` — as chamadas HTTP rodam em background e a tabela só é tocada na EDT.
* As credenciais do RabbitMQ vêm de variáveis de ambiente com valores padrão apontando para o local, para não deixar nada sensível no repositório.

---

Foi muito bom fazer esse teste. Obrigado pela oportunidade!
