package com.iescanarias;

import com.iescanarias.core.MusicScheduler;
import com.iescanarias.core.PlayerEngine;
import com.iescanarias.db.DatabaseManager;
import com.iescanarias.db.SongDAO;
import com.iescanarias.model.Category;

public class Main {

    public static final String MUSIC_DIR = resolveMusicDir();

    private static String resolveMusicDir() {
        // Ruta correcta relativa al directorio de trabajo del proceso
        java.io.File dir = new java.io.File("timbreiescanarias/src/main/resources/music");
        if (dir.exists()) {
            System.out.println("[MAIN] Carpeta de música encontrada: " + dir.getAbsolutePath());
            return dir.getAbsolutePath() + "/";
        }

        // Si ya estamos dentro de timbreiescanarias (otro directorio de trabajo)
        dir = new java.io.File("src/main/resources/music");
        if (dir.exists()) {
            System.out.println("[MAIN] Carpeta de música encontrada: " + dir.getAbsolutePath());
            return dir.getAbsolutePath() + "/";
        }

        System.err.println("[MAIN] ¡ADVERTENCIA! No se encontró la carpeta de música.");
        return "";
    }
    public static void main(String[] args) {

        // 1. Inicializar base de datos
        DatabaseManager.initialize();

        SongDAO songDAO = new SongDAO();

        // 2. Datos de ejemplo (solo se insertan la primera vez)
        seedData(songDAO);

        // 3. Arrancar motor y planificador
        PlayerEngine player = new PlayerEngine();
        MusicScheduler scheduler = new MusicScheduler(songDAO, player);
        scheduler.start();

        // 4. Apagado limpio al cerrar la app
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stop();
            DatabaseManager.close();
            System.out.println("[MAIN] Aplicación cerrada.");
        }));

        // Mantener vivo el hilo principal
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void seedData(SongDAO dao) {
        // Inserta canciones de ejemplo si la BD está vacía
        if (dao.getAll().isEmpty()) {
            dao.insert("Tuchat - Quevedo",   MUSIC_DIR + "TUCHAT - Quevedo.mp3",   Category.RECURRENTE);
            dao.insert("El Baifo - Quevedo", MUSIC_DIR + "EL BAIFO - Quevedo.mp3", Category.ESPECIAL);
            dao.insert("Brasilian Skies",    MUSIC_DIR + "Brasilian Skies.mp3",     Category.AMBIENTE);
            System.out.println("[MAIN] Datos de ejemplo insertados (base: " + MUSIC_DIR + ")");
        }
    }
}