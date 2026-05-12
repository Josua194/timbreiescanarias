package com.iescanarias.ui;

import com.iescanarias.config.AppConfig;
import com.iescanarias.db.SongDAO;
import com.iescanarias.model.Category;
import com.iescanarias.model.Song;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {
    private JComboBox<Song> timbreCombo;
    private JComboBox<Song> ambienteCombo;
    private JSlider timbreVolumeSlider;
    private JSlider ambienteVolumeSlider;
    private SongDAO songDAO;

    public MainFrame(SongDAO songDAO) {
        this.songDAO = songDAO;
        setTitle("Panel de Control - Timbre IES Canarias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 350);
        setLocationRelativeTo(null);
        
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Panel Timbre (Recurrente / Especial) ---
        JPanel timbrePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        timbrePanel.setBorder(BorderFactory.createTitledBorder("Configuración de Timbre"));
        
        timbreCombo = new JComboBox<>();
        timbreCombo.setRenderer(new SongCellRenderer());
        timbreCombo.addActionListener(e -> {
            Song selected = (Song) timbreCombo.getSelectedItem();
            if (selected != null) {
                AppConfig.setSelectedTimbre(selected);
            }
        });

        timbreVolumeSlider = new JSlider(0, 100, AppConfig.getTimbreVolume());
        timbreVolumeSlider.setMajorTickSpacing(20);
        timbreVolumeSlider.setPaintTicks(true);
        timbreVolumeSlider.setPaintLabels(true);
        timbreVolumeSlider.addChangeListener(e -> {
            AppConfig.setTimbreVolume(timbreVolumeSlider.getValue());
        });

        timbrePanel.add(new JLabel("Seleccionar Timbre:"));
        timbrePanel.add(timbreCombo);
        timbrePanel.add(new JLabel("Volumen:"));
        timbrePanel.add(timbreVolumeSlider);
        
        // --- Panel Ambiente ---
        JPanel ambientePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        ambientePanel.setBorder(BorderFactory.createTitledBorder("Configuración de Ambiente"));
        
        ambienteCombo = new JComboBox<>();
        ambienteCombo.setRenderer(new SongCellRenderer());
        ambienteCombo.addActionListener(e -> {
            Song selected = (Song) ambienteCombo.getSelectedItem();
            if (selected != null) {
                AppConfig.setSelectedAmbiente(selected);
            }
        });

        ambienteVolumeSlider = new JSlider(0, 100, AppConfig.getAmbienteVolume());
        ambienteVolumeSlider.setMajorTickSpacing(20);
        ambienteVolumeSlider.setPaintTicks(true);
        ambienteVolumeSlider.setPaintLabels(true);
        ambienteVolumeSlider.addChangeListener(e -> {
            AppConfig.setAmbienteVolume(ambienteVolumeSlider.getValue());
        });

        ambientePanel.add(new JLabel("Seleccionar Ambiente:"));
        ambientePanel.add(ambienteCombo);
        ambientePanel.add(new JLabel("Volumen:"));
        ambientePanel.add(ambienteVolumeSlider);

        mainPanel.add(timbrePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(ambientePanel);

        add(mainPanel);
    }

    private void loadData() {
        // Cargar canciones de timbre
        List<Song> recurrentes = songDAO.getByCategory(Category.RECURRENTE);
        List<Song> especiales = songDAO.getByCategory(Category.ESPECIAL);
        
        timbreCombo.removeAllItems();
        for (Song s : recurrentes) timbreCombo.addItem(s);
        for (Song s : especiales) timbreCombo.addItem(s);
        
        if (timbreCombo.getItemCount() > 0) {
            timbreCombo.setSelectedIndex(0);
        }

        // Cargar canciones de ambiente
        List<Song> ambientes = songDAO.getByCategory(Category.AMBIENTE);
        
        ambienteCombo.removeAllItems();
        for (Song s : ambientes) ambienteCombo.addItem(s);
        
        if (ambienteCombo.getItemCount() > 0) {
            ambienteCombo.setSelectedIndex(0);
        }
    }

    // Renderizador para mostrar solo el título (y quizás categoría)
    private static class SongCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Song) {
                Song song = (Song) value;
                setText(song.getTitle() + " (" + song.getCategory() + ")");
            }
            return this;
        }
    }
}
