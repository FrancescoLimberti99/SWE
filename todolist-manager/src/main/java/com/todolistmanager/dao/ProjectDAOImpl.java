package com.todolistmanager.dao;

import com.todolistmanager.db.DBConnection;
import com.todolistmanager.model.Project;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDAOImpl implements ProjectDAO {

    @Override
    public Project save(Project project) {
        String sql = "INSERT INTO projects (user_id, name, description, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, project.getUserId());
            pstmt.setString(2, project.getName());
            pstmt.setString(3, project.getDescription());
            pstmt.setTimestamp(4, Timestamp.valueOf(project.getCreatedAt()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                project.setId(rs.getLong("id"));
            }
            
            return project;
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio del progetto", e);
        }
    }

    @Override
    public void update(Project project) {
        String sql = "UPDATE projects SET name = ?, description = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, project.getName());
            pstmt.setString(2, project.getDescription());
            pstmt.setLong(3, project.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento del progetto", e);
        }
    }

    @Override
    public Optional<Project> findById(Long id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRowToProject(rs));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca del progetto", e);
        }
    }

    @Override
    public List<Project> findByUserId(Long userId) {
        String sql = "SELECT * FROM projects WHERE user_id = ? ORDER BY created_at DESC";
        List<Project> projects = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei progetti dell'utente", e);
        }
        
        return projects;
    }

    @Override
    public List<Project> findAll() {
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";
        List<Project> projects = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                projects.add(mapRowToProject(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero dei progetti", e);
        }
        
        return projects;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'eliminazione del progetto", e);
        }
    }

    @Override
    public int countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM projects WHERE user_id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel conteggio dei progetti", e);
        }
    }

    //mappa riga del ResultSet a un oggetto Project
    private Project mapRowToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setUserId(rs.getLong("user_id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return project;
    }
}