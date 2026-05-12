package com.iescanarias.db;

import com.iescanarias.model.Category;
import com.iescanarias.model.Song;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {

    // Insertar canción
    public void insert(String title, String filePath, Category category) {
        String sql = "INSERT INTO songs (title, filePath, category) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, filePath);
            ps.setString(3, category.name());
            ps.executeUpdate();
            System.out.println("[DAO] Canción insertada: " + title);
        } catch (SQLException e) {
            System.err.println("[DAO] Error al insertar: " + e.getMessage());
        }
    }

    // Obtener todas las canciones activas de una categoría
    public List<Song> getByCategory(Category category) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE category = ? AND active = 1";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, category.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error al consultar: " + e.getMessage());
        }
        return songs;
    }

    // Obtener todas las canciones activas
    public List<Song> getAll() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE active = 1";
        try (Statement stmt = DatabaseManager.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                songs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error al consultar todas: " + e.getMessage());
        }
        return songs;
    }

    // Desactivar canción (borrado lógico)
    public void deactivate(int id) {
        String sql = "UPDATE songs SET active = 0 WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAO] Error al desactivar: " + e.getMessage());
        }
    }

    // Eliminar canción definitivamente
    public void delete(int id) {
        String sql = "DELETE FROM songs WHERE id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("[DAO] Canción eliminada (id=" + id + ")");
        } catch (SQLException e) {
            System.err.println("[DAO] Error al eliminar: " + e.getMessage());
        }
    }

    private Song mapRow(ResultSet rs) throws SQLException {
        return new Song(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("filePath"),
            Category.valueOf(rs.getString("category")),
            rs.getInt("active") == 1
        );
    }
}