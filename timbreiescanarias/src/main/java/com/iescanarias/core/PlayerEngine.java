package com.iescanarias.core;

import com.iescanarias.model.Category;
import com.iescanarias.model.Song;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class PlayerEngine {

    private static final int BELL_MAX_SECONDS = 15; // límite para RECURRENTE y ESPECIAL

    private AdvancedPlayer currentPlayer;
    private Thread playThread;
    private Thread timeoutThread;
    private volatile boolean playing = false;

    public void play(Song song) {
        stop();

        System.out.printf("[PLAYER] Reproduciendo: %s | Categoría: %s | Volumen: %s%n",
            song.getTitle(), song.getCategory(),
            song.getCategory() == Category.AMBIENTE ? "70%" : "100%");

        playThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(song.getFilePath());
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                currentPlayer = new AdvancedPlayer(bis);
                currentPlayer.setPlayBackListener(new PlaybackListener() {
                    @Override
                    public void playbackFinished(PlaybackEvent event) {
                        playing = false;
                        cancelTimeout();
                        System.out.println("[PLAYER] Fin natural: " + song.getTitle());
                    }
                });

                playing = true;

                // Si es timbre (no ambiente), programar corte a los 15 segundos
                if (song.getCategory() != Category.AMBIENTE) {
                    scheduleTimeout(BELL_MAX_SECONDS);
                }

                currentPlayer.play();

            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("[PLAYER] Error al reproducir: " + e.getMessage());
                }
                playing = false;
            }
        });

        playThread.setDaemon(true);
        playThread.start();
    }

    /** Corta la reproducción después de N segundos */
    private void scheduleTimeout(int seconds) {
        timeoutThread = new Thread(() -> {
            try {
                Thread.sleep(seconds * 1000L);
                if (playing) {
                    System.out.println("[PLAYER] Tiempo máximo alcanzado (" + seconds + "s). Cortando timbre.");
                    stop();
                }
            } catch (InterruptedException ignored) {}
        });
        timeoutThread.setDaemon(true);
        timeoutThread.start();
    }

    private void cancelTimeout() {
        if (timeoutThread != null && timeoutThread.isAlive()) {
            timeoutThread.interrupt();
        }
    }

    public void stop() {
        cancelTimeout();
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }
        playing = false;
    }

    public boolean isPlaying() {
        return playing;
    }
}