package com.example.avaliacao.swing.ui;

import com.example.avaliacao.entity.Pedido;
import com.example.avaliacao.swing.client.PedidoApiClient;
import com.example.avaliacao.entity.LinhaPedido;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

public class PedidoFrame extends JFrame {

    private static final int INTERVALO_POLLING_MS = 3000;

    private final PedidoApiClient apiClient = new PedidoApiClient();
    private final PedidoTableModel tableModel = new PedidoTableModel();
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField txtDataCriacao;

    private JTextField txtProduto;
    private JTextField txtQuantidade;
    private JButton btnEnviar;
    private JLabel lblRodape;
    private Timer timerPolling;

    public PedidoFrame() {
        super("Envio de Pedidos");
        montarTela();
        iniciarPolling();
    }

    private void montarTela() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (timerPolling != null) {
                    timerPolling.stop();
                }
            }
        });

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(criarPainelFormulario(), BorderLayout.NORTH);
        add(criarPainelTabela(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);

        setSize(950, 460);
        setLocationRelativeTo(null);
    }

    private JPanel criarPainelFormulario() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createTitledBorder("Novo pedido"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        painel.add(new JLabel("Produto:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtProduto = new JTextField(25);
        painel.add(txtProduto, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        painel.add(new JLabel("Quantidade:"), gbc);

        gbc.gridx = 3;
        txtQuantidade = new JTextField(6);
        painel.add(txtQuantidade, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(new JLabel("Data criação:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        txtDataCriacao = new JTextField(20);
        txtDataCriacao.setToolTipText("Formato: dd/MM/aaaa");
        painel.add(txtDataCriacao, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        btnEnviar = new JButton("Enviar Pedido");
        btnEnviar.addActionListener(e -> onEnviarPedido());
        painel.add(btnEnviar, gbc);

        getRootPane().setDefaultButton(btnEnviar);

        preencherDataAtual();

        return painel;
    }

    private void preencherDataAtual() {
        txtDataCriacao.setText(LocalDate.now().format(FORMATO_DATA));
    }

    private JScrollPane criarPainelTabela() {
        JTable tabela = new JTable(tableModel);
        tabela.setRowHeight(24);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setAutoCreateRowSorter(true);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(230); // ID
        tabela.getColumnModel().getColumn(1).setPreferredWidth(140); // Produto
        tabela.getColumnModel().getColumn(2).setPreferredWidth(40);  // Qtd
        tabela.getColumnModel().getColumn(3).setPreferredWidth(90);  // Data
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);  // Status
        tabela.getColumnModel().getColumn(5).setPreferredWidth(240); // Detalhe

        tabela.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createTitledBorder("Pedidos enviados"));

        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (c instanceof JComponent jc && v != null) {
                    jc.setToolTipText(v.toString());
                }
                return c;
            }
        });

        return scroll;
    }

    private JPanel criarRodape() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblRodape = new JLabel("Pronto. Atualizando status a cada "
                + (INTERVALO_POLLING_MS / 1000) + " segundos.");
        lblRodape.setForeground(new Color(90, 90, 90));
        painel.add(lblRodape);
        return painel;
    }

    private void onEnviarPedido() {
        String produto = txtProduto.getText().trim();
        String quantidadeTexto = txtQuantidade.getText().trim();

        if (produto.isEmpty()) {
            mostrarAviso("Informe o nome do produto.");
            txtProduto.requestFocus();
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(quantidadeTexto);
        } catch (NumberFormatException e) {
            mostrarAviso("A quantidade deve ser um numero inteiro.");
            txtQuantidade.requestFocus();
            return;
        }

        if (quantidade <= 0) {
            mostrarAviso("A quantidade deve ser maior que zero.");
            txtQuantidade.requestFocus();
            return;
        }

        String dataTexto = txtDataCriacao.getText().trim();
        LocalDateTime dataCriacao;

        try {
           LocalDate data = LocalDate.parse(dataTexto, FORMATO_DATA);
           dataCriacao = data.atStartOfDay();
        } catch (DateTimeParseException e) {
           mostrarAviso("Data inválida. Use o formato dd/MM/aaaa\n"
                   + "Exemplo: " + LocalDate.now().format(FORMATO_DATA));
           txtDataCriacao.requestFocus();
           txtDataCriacao.selectAll();

           return;
        }

        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido(id, produto, quantidade, dataCriacao);

        btnEnviar.setEnabled(false);
        lblRodape.setText("Enviando pedido...");

        new SwingWorker<Void, Void>() {
            private String erro;

            @Override
            protected Void doInBackground() {
                try {
                    apiClient.enviarPedido(pedido);
                } catch (PedidoApiClient.ApiException e) {
                    erro = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                btnEnviar.setEnabled(true);

                if (erro != null) {
                    lblRodape.setText("Falha ao enviar o ultimo pedido.");
                    mostrarErro(erro);
                    return;
                }

                tableModel.adicionar(new LinhaPedido(id, produto, quantidade, dataCriacao));
                txtProduto.setText("");
                txtQuantidade.setText("");
                preencherDataAtual();
                txtProduto.requestFocus();
                lblRodape.setText("Pedido enviado. Aguardando processamento...");
            }
        }.execute();
    }

    private void iniciarPolling() {
        timerPolling = new Timer(INTERVALO_POLLING_MS, e -> verificarStatusPendentes());
        timerPolling.start();
    }

    private void verificarStatusPendentes() {
        List<LinhaPedido> pendentes = tableModel.getPendentes();
        if (pendentes.isEmpty()) {
            return;
        }

        new SwingWorker<Void, Object[]>() {
            @Override
            protected Void doInBackground() {
                for (LinhaPedido linha : pendentes) {
                    PedidoApiClient.StatusResposta resp = apiClient.consultarStatus(linha.getId());
                    if (resp != null && resp.status() != null && !resp.status().isBlank()) {
                        publish(new Object[]{linha.getId(), resp.status().toUpperCase(), resp.mensagemErro()});
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] c : chunks) {
                    tableModel.atualizarStatus((UUID) c[0], (String) c[1], (String) c[2]);
                }
            }
        }.execute();
    }

    private void mostrarAviso(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem,
                "Atencao", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem,
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {

        private static final Color VERDE = new Color(0, 128, 0);
        private static final Color VERMELHO = new Color(178, 34, 34);
        private static final Color CINZA = new Color(120, 120, 120);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String texto = value == null ? "" : value.toString().toLowerCase();

            if (!isSelected) {
                if (texto.contains("sucesso")) {
                    c.setForeground(VERDE);
                } else if (texto.contains("falha")) {
                    c.setForeground(VERMELHO);
                } else {
                    c.setForeground(CINZA);
                }
            }
            return c;
        }
    }
}
