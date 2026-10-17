package com.example.dearfreshman;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GameRepository {

    public int createNewSave(Player player) throws SQLException {
        String playerSql = "INSERT INTO players (player_name) VALUES (?)";

        String saveSql = """
                INSERT INTO game_saves
                (player_id, save_name, current_chapter,
                 current_event_index, game_day, game_minutes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String statsSql = """
                INSERT INTO player_stats
                (save_id, energy, friendship, reputation,
                 academics, suspicion, gino_trust, z_trust, obet_trust, william_suspicion,
                 programming_score, database_score, history_score, quiz_answered)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int playerId;
                try (PreparedStatement ps = con.prepareStatement(playerSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, player.getName());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Could not create player.");
                        }
                        playerId = rs.getInt(1);
                    }
                }

                int saveId;
                try (PreparedStatement ps = con.prepareStatement(saveSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, playerId);
                    ps.setString(2, "Save 1");
                    ps.setInt(3, 1);
                    ps.setInt(4, 0);
                    ps.setInt(5, 1);
                    ps.setInt(6, 480);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Could not create save slot.");
                        }
                        saveId = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(statsSql)) {
                    ps.setInt(1, saveId);
                    ps.setInt(2, player.getEnergy());
                    ps.setInt(3, player.getFriendship());
                    ps.setInt(4, player.getReputation());
                    ps.setInt(5, player.getAcademics());
                    ps.setInt(6, player.getSuspicion());
                    ps.setInt(7, player.getGinoTrust());
                    ps.setInt(8, player.getZTrust());
                    ps.setInt(9, player.getObetTrust());
                    ps.setInt(10, player.getWilliamSuspicion());
                    ps.setInt(11, player.getProgrammingScore());
                    ps.setInt(12, player.getDatabaseScore());
                    ps.setInt(13, player.getHistoryScore());
                    ps.setInt(14, player.getQuizAnswered());
                    ps.executeUpdate();
                }

                con.commit();
                return saveId;

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public int getLatestSaveId() throws SQLException {
        String sql = "SELECT save_id FROM game_saves ORDER BY save_id DESC LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("save_id");
            }
        }
        return -1;
    }

    public Player loadPlayer(int saveId) throws SQLException {
        String sql = """
                SELECT p.player_name,
                       s.energy, s.friendship, s.reputation,
                       s.academics, s.suspicion,
                       s.gino_trust, s.z_trust, s.obet_trust, s.william_suspicion,
                       s.programming_score, s.database_score, s.history_score,
                       s.quiz_answered
                FROM players p
                JOIN game_saves g ON g.player_id = p.player_id
                JOIN player_stats s ON s.save_id = g.save_id
                WHERE g.save_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, saveId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Player player = new Player(rs.getString("player_name"));

                // Restore numeric progress including all three subject scores.
                player.restoreProgress(
                        rs.getInt("energy"),
                        rs.getInt("friendship"),
                        rs.getInt("reputation"),
                        rs.getInt("academics"),
                        rs.getInt("suspicion"),
                        rs.getInt("programming_score"),
                        rs.getInt("database_score"),
                        rs.getInt("history_score"),
                        rs.getInt("quiz_answered")
                );

                // Restore NPC trust values as absolute numbers.
                int ginoTrust = rs.getInt("gino_trust");
                int zTrust = rs.getInt("z_trust");
                int obetTrust = rs.getInt("obet_trust");
                int williamSuspicion = rs.getInt("william_suspicion");

                player.restoreNpcTrust(ginoTrust, zTrust, obetTrust, williamSuspicion);

                return player;
            }
        }
    }

    /**
     * Load basic saved game metadata (current_event_index, game_day, game_minutes)
     * so the app can restore play position and time.
     */
    public int[] loadSaveMetadata(int saveId) throws SQLException {
        String sql = "SELECT current_event_index, game_day, game_minutes FROM game_saves WHERE save_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, saveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new int[]{0, 1, 8 * 60}; // defaults
                }
                return new int[]{
                        rs.getInt("current_event_index"),
                        rs.getInt("game_day"),
                        rs.getInt("game_minutes")
                };
            }
        }
    }
}
