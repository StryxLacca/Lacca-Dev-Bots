package com.laccadev.database;

import java.sql.*;

public class Database {

    private static Connection connection;

    public static void init() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:laccadev.db");
        createTables();
        System.out.println("Adatbázis inicializálva.");
    }

    public static Connection get() { return connection; }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Figyelmeztetések
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS warnings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guild_id TEXT, user_id TEXT, reason TEXT, moderator_id TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )""");

        // XP / Szint
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS levels (
                guild_id TEXT, user_id TEXT, xp INTEGER DEFAULT 0,
                level INTEGER DEFAULT 0, last_message DATETIME,
                PRIMARY KEY (guild_id, user_id)
            )""");

        // Szint szerepkörök
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS level_roles (
                guild_id TEXT, level INTEGER, role_id TEXT,
                PRIMARY KEY (guild_id, level)
            )""");

        // Config (welcome, log csatornák, stb.)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS config (
                guild_id TEXT, key TEXT, value TEXT,
                PRIMARY KEY (guild_id, key)
            )""");

        // AFK státuszok
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS afk (
                guild_id TEXT, user_id TEXT, reason TEXT,
                PRIMARY KEY (guild_id, user_id)
            )""");

        // Mute szerepkör tárolás
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS muted (
                guild_id TEXT, user_id TEXT,
                PRIMARY KEY (guild_id, user_id)
            )""");

        stmt.close();
    }

    // --- Config segédfüggvények ---
    public static void setConfig(String guildId, String key, String value) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO config (guild_id, key, value) VALUES (?, ?, ?)");
        ps.setString(1, guildId); ps.setString(2, key); ps.setString(3, value);
        ps.executeUpdate(); ps.close();
    }

    public static String getConfig(String guildId, String key) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT value FROM config WHERE guild_id = ? AND key = ?");
        ps.setString(1, guildId); ps.setString(2, key);
        ResultSet rs = ps.executeQuery();
        String val = rs.next() ? rs.getString("value") : null;
        ps.close(); return val;
    }

    // --- XP segédfüggvények ---
    public static int[] getLevel(String guildId, String userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT xp, level FROM levels WHERE guild_id = ? AND user_id = ?");
        ps.setString(1, guildId); ps.setString(2, userId);
        ResultSet rs = ps.executeQuery();
        int[] result = rs.next() ? new int[]{rs.getInt("xp"), rs.getInt("level")} : new int[]{0, 0};
        ps.close(); return result;
    }

    public static void setXP(String guildId, String userId, int xp, int level) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT OR REPLACE INTO levels (guild_id, user_id, xp, level, last_message) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
        ps.setString(1, guildId); ps.setString(2, userId);
        ps.setInt(3, xp); ps.setInt(4, level);
        ps.executeUpdate(); ps.close();
    }

    // --- Warnings segédfüggvények ---
    public static void addWarning(String guildId, String userId, String reason, String modId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO warnings (guild_id, user_id, reason, moderator_id) VALUES (?, ?, ?, ?)");
        ps.setString(1, guildId); ps.setString(2, userId);
        ps.setString(3, reason); ps.setString(4, modId);
        ps.executeUpdate(); ps.close();
    }

    public static ResultSet getWarnings(String guildId, String userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT * FROM warnings WHERE guild_id = ? AND user_id = ? ORDER BY timestamp DESC");
        ps.setString(1, guildId); ps.setString(2, userId);
        return ps.executeQuery();
    }
}
