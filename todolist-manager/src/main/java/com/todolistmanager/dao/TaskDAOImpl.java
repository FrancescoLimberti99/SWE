package com.todolistmanager.dao;

import com.todolistmanager.db.DBConnection;
import com.todolistmanager.model.Priority;
import com.todolistmanager.model.Task;
import com.todolistmanager.model.TaskStatus;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDAOImpl implements TaskDAO {

    @Override
    public Task save(Task task) {
        String sql = "INSERT INTO tasks (project_id, title, description, status, priority, deadline, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, task.getProjectId());
            pstmt.setString(2, task.getTitle());
            pstmt.setString(3, task.getDescription());
            pstmt.setString(4, task.getStatus().name());
            pstmt.setString(5, task.getPriority().name());
            pstmt.setDate(6, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setTimestamp(7, Timestamp.valueOf(task.getCreatedAt()));
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                task.setId(rs.getLong("id"));
            }
            
            return task;
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel salvataggio della task", e);
        }
    }

    @Override
    public void update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, priority = ?, " +
                     "deadline = ?, completed_at = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getStatus().name());
            pstmt.setString(4, task.getPriority().name());
            pstmt.setDate(5, task.getDeadline() != null ? Date.valueOf(task.getDeadline()) : null);
            pstmt.setDate(6, task.getCompletedAt() != null ? Date.valueOf(task.getCompletedAt()) : null);
            pstmt.setLong(7, task.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornamento della task", e);
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapRowToTask(rs));
            }
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella ricerca della task", e);
        }
    }

    @Override
    public List<Task> findByProjectId(Long projectId) {
        String sql = "SELECT * FROM tasks WHERE project_id = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task del progetto", e);
        }
        
        return tasks;
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        String sql = "SELECT * FROM tasks WHERE status = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task per stato", e);
        }
        
        return tasks;
    }

    @Override
    public List<Task> findByPriority(Priority priority) {
        String sql = "SELECT * FROM tasks WHERE priority = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, priority.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task per priorità", e);
        }
        
        return tasks;
    }

    @Override
    public List<Task> findByDeadlineBefore(LocalDate date) {
    	String sql = "SELECT * FROM tasks WHERE deadline <= ? AND deadline IS NOT NULL AND status != 'DONE' ORDER BY deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task in scadenza", e);
        }
        
        return tasks;
    }

    @Override
    public List<Task> findOverdueTasks() {
    	String sql = "SELECT * FROM tasks WHERE deadline < CURRENT_DATE AND deadline IS NOT NULL AND status != 'DONE' ORDER BY deadline ASC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task in ritardo", e);
        }
        
        return tasks;
    }

    @Override
    public List<Task> findAll() {
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel recupero delle task", e);
        }
        
        return tasks;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'eliminazione della task", e);
        }
    }

    @Override
    public int countByProjectIdAndStatus(Long projectId, TaskStatus status) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND status = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, projectId);
            pstmt.setString(2, status.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel conteggio delle task", e);
        }
    }

    //mappa riga del ResultSet a un oggetto Task
    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setProjectId(rs.getLong("project_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        
        Date deadline = rs.getDate("deadline");
        if (deadline != null) {
            task.setDeadline(deadline.toLocalDate());
        }
        
        Date completedAt = rs.getDate("completed_at");
        if (completedAt != null) {
            task.setCompletedAt(completedAt.toLocalDate());
        }
        
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return task;
    }
}