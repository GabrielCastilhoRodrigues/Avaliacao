package com.example.avaliacao.swing.ui;

import com.example.avaliacao.entity.LinhaPedido;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PedidoTableModel extends AbstractTableModel {

    private static final String[] COLUNAS =
            {"ID", "Produto", "Qtd", "Data Criação", "Status", "Detalhe"};

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<LinhaPedido> linhas = new ArrayList<>();

    public void adicionar(LinhaPedido linha) {
        linhas.add(linha);
        int index = linhas.size() - 1;
        fireTableRowsInserted(index, index);
    }

    public void atualizarStatus(UUID id, String novoStatus, String mensagemErro) {
        for (int i = 0; i < linhas.size(); i++) {
            LinhaPedido linha = linhas.get(i);
            if (linha.getId().equals(id)) {
                linha.setStatus(novoStatus);
                linha.setMensagemErro(mensagemErro);
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }

    public List<LinhaPedido> getPendentes() {
        List<LinhaPedido> pendentes = new ArrayList<>();
        for (LinhaPedido linha : linhas) {
            if (linha.aguardandoProcessamento()) {
                pendentes.add(linha);
            }
        }
        return pendentes;
    }

    public LinhaPedido getLinha(int rowIndex) {
        return linhas.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return linhas.size();
    }

    @Override
    public int getColumnCount() {
        return COLUNAS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUNAS[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // area de exibicao nao editavel, conforme requisito
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LinhaPedido linha = linhas.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> linha.getId().toString();
            case 1 -> linha.getProduto();
            case 2 -> linha.getQuantidade();
            case 3 -> linha.getDataCriacao() == null ? "" : linha.getDataCriacao().format(FORMATO);
            case 4 -> linha.getStatus();
            case 5 -> linha.getMensagemErro() == null ? "" : linha.getMensagemErro();
            default -> "";
        };
    }
}
