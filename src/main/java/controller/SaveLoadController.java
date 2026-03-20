package controller;

import model.Board;
import model.GameState;
import model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SaveLoadController {
    private final DatabaseController databaseController;

    public SaveLoadController(DatabaseController databaseController) {
        if (databaseController == null) {
            throw new IllegalArgumentException("DatabaseController is required");
        }
        this.databaseController = databaseController;
    }

    public int saveGame(GameState gameState, Player winner) throws SQLException {
        try (Connection connection = databaseController.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int gameId = insertGame(connection);
                insertGameState(connection, gameId, gameState, winner);
                insertPlayers(connection, gameId, gameState.getPlayers());
                connection.commit();
                return gameId;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void updateGame(int gameId, GameState gameState, Player winner) throws SQLException {
        try (Connection connection = databaseController.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateGameState(connection, gameId, gameState, winner);
                updatePlayers(connection, gameId, gameState.getPlayers());
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public GameState loadGame(int gameId) throws SQLException {
        try (Connection connection = databaseController.getConnection()) {
            List<Player> players = loadPlayers(connection, gameId);
            if (players.isEmpty()) {
                throw new SQLException("No players found for game id " + gameId);
            }
            GameState gameState = new GameState(players, new Board());
            loadGameState(connection, gameId, gameState);
            return gameState;
        }
    }

    public Integer findLatestUnfinishedGameId() throws SQLException {
        String sql = """
                SELECT g.id
                FROM games g
                JOIN game_state gs ON gs.game_id = g.id
                WHERE gs.finished = FALSE
                ORDER BY g.created_at DESC
                LIMIT 1
                """;
        try (Connection connection = databaseController.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    public void recordDisaster(int gameId, int tileIndex, String question, int chosenIndex, boolean correct) throws SQLException {
        String sql = "INSERT INTO disasters (game_id, tile_index, question, chosen_index, correct) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = databaseController.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            ps.setInt(2, tileIndex);
            ps.setString(3, question);
            ps.setInt(4, chosenIndex);
            ps.setBoolean(5, correct);
            ps.executeUpdate();
        }
    }

    public void recordTurn(int gameId, int turnNumber, String playerName, int roll, int startPos, int endPos) throws SQLException {
        String sql = "INSERT INTO turns (game_id, turn_number, player_name, roll, start_pos, end_pos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseController.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            ps.setInt(2, turnNumber);
            ps.setString(3, playerName);
            ps.setInt(4, roll);
            ps.setInt(5, startPos);
            ps.setInt(6, endPos);
            ps.executeUpdate();
        }
    }

    public List<String> loadTurnHistory(int gameId) throws SQLException {
        String sql = """
                SELECT turn_number, player_name, roll, start_pos, end_pos, created_at
                FROM turns
                WHERE game_id = ?
                ORDER BY turn_number ASC, id ASC
                """;
        List<String> history = new ArrayList<>();
        try (Connection connection = databaseController.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int turn = rs.getInt("turn_number");
                    String name = rs.getString("player_name");
                    int roll = rs.getInt("roll");
                    int start = rs.getInt("start_pos");
                    int end = rs.getInt("end_pos");
                    history.add("Turn " + turn + ": " + name + " rolled " + roll + " (" + start + " -> " + end + ")");
                }
            }
        }
        return history;
    }

    public Integer findLatestGameId() throws SQLException {
        String sql = "SELECT id FROM games ORDER BY created_at DESC LIMIT 1";
        try (Connection connection = databaseController.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return null;
    }

    private int insertGame(Connection connection) throws SQLException {
        String sql = "INSERT INTO games DEFAULT VALUES";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to generate game id");
    }

    private void insertGameState(Connection connection, int gameId, GameState gameState, Player winner) throws SQLException {
        String sql = "INSERT INTO game_state (game_id, current_player_index, finished, winner_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            ps.setInt(2, gameState.getCurrentPlayerIndex());
            ps.setBoolean(3, gameState.isFinished());
            ps.setString(4, winner == null ? null : winner.getName());
            ps.executeUpdate();
        }
    }

    private void updateGameState(Connection connection, int gameId, GameState gameState, Player winner) throws SQLException {
        String sql = "UPDATE game_state SET current_player_index = ?, finished = ?, winner_name = ? WHERE game_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameState.getCurrentPlayerIndex());
            ps.setBoolean(2, gameState.isFinished());
            ps.setString(3, winner == null ? null : winner.getName());
            ps.setInt(4, gameId);
            ps.executeUpdate();
        }
    }

    private void insertPlayers(Connection connection, int gameId, List<Player> players) throws SQLException {
        String sql = "INSERT INTO players (game_id, name, position, turn_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int order = 0;
            for (Player player : players) {
                ps.setInt(1, gameId);
                ps.setString(2, player.getName());
                ps.setInt(3, player.getPosition());
                ps.setInt(4, order++);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void updatePlayers(Connection connection, int gameId, List<Player> players) throws SQLException {
        String sql = """
                UPDATE players
                SET name = ?, position = ?
                WHERE game_id = ? AND turn_order = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int order = 0;
            for (Player player : players) {
                ps.setString(1, player.getName());
                ps.setInt(2, player.getPosition());
                ps.setInt(3, gameId);
                ps.setInt(4, order++);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<Player> loadPlayers(Connection connection, int gameId) throws SQLException {
        String sql = "SELECT name, position FROM players WHERE game_id = ? ORDER BY turn_order";
        List<Player> players = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    Player player = new Player(name);
                    player.setPosition(rs.getInt("position"));
                    players.add(player);
                }
            }
        }
        return players;
    }

    private void loadGameState(Connection connection, int gameId, GameState gameState) throws SQLException {
        String sql = "SELECT current_player_index, finished FROM game_state WHERE game_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, gameId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    gameState.setCurrentPlayerIndex(rs.getInt("current_player_index"));
                    gameState.setFinished(rs.getBoolean("finished"));
                }
            }
        }
    }
}
