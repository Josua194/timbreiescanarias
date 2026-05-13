package com.iescanarias.core;

import com.iescanarias.db.SongDAO;
import com.iescanarias.model.Category;
import com.iescanarias.model.Song;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicScheduler {

    private static final LocalTime AMBIENTE_START   = LocalTime.of(7, 0);
    private static final LocalTime RECURRENTE_START = LocalTime.of(8, 0);
    private static final LocalTime RECREO_START     = LocalTime.of(10, 45);
    private static final LocalTime RECREO_END       = LocalTime.of(11, 15);
    private static final LocalTime SYSTEM_END       = LocalTime.of(14, 0);
    private static final int INTERVAL_MINUTES       = 55;

    private final SongDAO songDAO;
    private final PlayerEngine player;
    private final ScheduledExecutorService scheduler;

    private int lastBellInterval = -1;

    public MusicScheduler(SongDAO songDAO, PlayerEngine player) {
        this.songDAO   = songDAO;
        this.player    = player;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "music-scheduler");
            t.setDaemon(true); // hilo daemon: no impide que la JVM termine
            return t;
        });
    }

    public void start() {
        System.out.println("[SCHEDULER] Sistema de timbre iniciado.");
        scheduler.scheduleAtFixedRate(this::tick, 0, 10, TimeUnit.SECONDS); // tick cada 10s
    }

    public void stop() {
        scheduler.shutdownNow();
        player.stop();
        System.out.println("[SCHEDULER] Sistema detenido.");
    }

    private boolean ambienteActivo = false;
    private boolean primerTimbreSonado = false;

    private void tick() {
        try {
            LocalTime now = LocalTime.now();
            System.out.println("[TICK] Hora actual: " + now + " | isPlaying: " + player.isPlaying());

            // ── Fuera de horario ──────────────────────────────────────────
            if (now.isBefore(AMBIENTE_START) || !now.isBefore(SYSTEM_END)) {
                if (player.isPlaying()) {
                    player.stop();
                    System.out.println("[TICK] → Fuera de horario. Parando.");
                }
                ambienteActivo = false;
                primerTimbreSonado = false;
                lastBellInterval = -1;
                return;
            }

            // ── Franja AMBIENTE: 7:00–7:59 ───────────────────────────────
            if (now.isBefore(RECURRENTE_START)) {
                ambienteActivo = true;
                primerTimbreSonado = false;
                if (!player.isPlaying()) {
                    System.out.println("[TICK] → Franja AMBIENTE, poniendo siguiente");
                    playNext(Category.AMBIENTE);
                }
                return;
            }

            // ── Transición AMBIENTE → TIMBRE (exactamente 8:00) ──────────
            if (ambienteActivo) {
                System.out.println("[TICK] → Transición: parando ambiente, sonando primer timbre");
                player.stop();
                ambienteActivo = false;
                primerTimbreSonado = true;
                lastBellInterval = 0;
                playNext(pickCategory());
                return;
            }

            // ── Franja RECREO: 10:45–11:15 ───────────────────────────────
            boolean esRecreo = !now.isBefore(RECREO_START) && now.isBefore(RECREO_END);
            if (esRecreo) {
                if (!player.isPlaying()) {
                    System.out.println("[TICK] → Franja RECREO, poniendo ambiente");
                    playNext(Category.AMBIENTE);
                }
                lastBellInterval = -2; // marca especial: venimos del recreo
                return;
            }

            // ── Timbre de vuelta del recreo (exactamente 11:15) ──────────
            boolean esVueltaRecreo = !now.isBefore(RECREO_END) &&
                                    now.isBefore(RECREO_END.plusSeconds(10)) &&
                                    lastBellInterval == -2;
            if (esVueltaRecreo) {
                System.out.println("[TICK] → Vuelta de recreo, sonando timbre");
                player.stop();
                lastBellInterval = -1;
                playNext(pickCategory());
                return;
            }

            // ── Franja TIMBRE: 8:00–14:00, cada 55 min ───────────────────
            int minutesSinceStart = (now.getHour() - RECURRENTE_START.getHour()) * 60
                                    + (now.getMinute() - RECURRENTE_START.getMinute());
            int totalSeconds      = minutesSinceStart * 60 + now.getSecond();
            int currentInterval   = totalSeconds / (INTERVAL_MINUTES * 60);
            int secondsIntoSlot   = totalSeconds % (INTERVAL_MINUTES * 60);

            System.out.println("[TICK] → Franja TIMBRE | intervalo=" + currentInterval
                + " | segundosEnSlot=" + secondsIntoSlot + " | lastBell=" + lastBellInterval);

            if (secondsIntoSlot < 10 && currentInterval != lastBellInterval) {
                lastBellInterval = currentInterval;
                playNext(pickCategory());
            }

        } catch (Exception e) {
            System.err.println("[SCHEDULER] Error en tick: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Category pickCategory() {
        List<Song> especiales = songDAO.getByCategory(Category.ESPECIAL);
        return especiales.isEmpty() ? Category.RECURRENTE : Category.ESPECIAL;
    }

    private void playNext(Category category) {
        Song song = null;
        if (category == Category.AMBIENTE) {
            song = com.iescanarias.config.AppConfig.getSelectedAmbiente();
        } else {
            song = com.iescanarias.config.AppConfig.getSelectedTimbre();
        }

        if (song == null) {
            System.out.println("[SCHEDULER] Sin canción seleccionada para: " + category);
            return;
        }

        player.play(song);
    }
}