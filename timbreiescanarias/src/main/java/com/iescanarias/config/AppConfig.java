package com.iescanarias.config;

import com.iescanarias.model.Song;

public class AppConfig {
    private static Song selectedTimbre = null;
    private static Song selectedAmbiente = null;
    
    private static int timbreVolume = 100;
    private static int ambienteVolume = 70;

    public static Song getSelectedTimbre() {
        return selectedTimbre;
    }

    public static void setSelectedTimbre(Song song) {
        selectedTimbre = song;
    }

    public static Song getSelectedAmbiente() {
        return selectedAmbiente;
    }

    public static void setSelectedAmbiente(Song song) {
        selectedAmbiente = song;
    }

    public static int getTimbreVolume() {
        return timbreVolume;
    }

    public static void setTimbreVolume(int volume) {
        timbreVolume = volume;
    }

    public static int getAmbienteVolume() {
        return ambienteVolume;
    }

    public static void setAmbienteVolume(int volume) {
        ambienteVolume = volume;
    }
}
