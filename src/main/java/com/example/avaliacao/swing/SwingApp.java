package com.example.avaliacao.swing;

import com.example.avaliacao.swing.ui.PedidoFrame;

import javax.swing.*;

public class SwingApp {

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            throw new Exception("Erro ao executar parte Visual");
        }

        SwingUtilities.invokeLater(() -> new PedidoFrame().setVisible(true));
    }
}
