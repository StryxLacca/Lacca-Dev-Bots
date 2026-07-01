package com.laccadev.commands.levels;

import com.laccadev.database.Database;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LevelCommands {

    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Random random = new Random();

    // XP szerzés üzenetküldésnél
    public void handleXP(MessageReceivedEvent event) {
        try {
            String toggle = Database.getConfig(event.getGuild().getId(), "xp_enabled");
            if ("false".equals(toggle)) return;

            String key = event.getGuild().getId() + ":" + event.getAuthor().getId();
            int cooldownSec = 60;
            try {
                String cd = Database.getConfig(event.getGuild().getId(), "xp_cooldown");
                if (cd != null) cooldownSec = Integer.parseInt(cd);
            } catch (Exception ignored) {}

            long now = Instant.now().getEpochSecond();
            if (cooldowns.containsKey(key) && now - cooldowns.get(key) < cooldownSec) return;
            cooldowns.put(key, now);

            double multiplier = 1.0;
            try {
                String m = Database.getConfig(event.getGuild().getId(), "xp_multiplier");
                if (m != null) multiplier = Double.parseDouble(m);
            } catch (Exception ignored) {}

            int[] current = Database.getLevel(event.getGuild().getId(), event.getAuthor().getId());
            int xpGain = (int) ((random.nextInt(15) + 5) * multiplier);
            int newXp = current[0] + xpGain;
            int newLevel = current[1];
            int xpNeeded = 100 * (newLevel + 1);

            if (newXp >= xpNeeded) {
                newXp -= xpNeeded;
                newLevel++;
                event.getChannel().sendMessageEmbeds(
                    EmbedUtil.success("🎉 Szint fel!", event.getAuthor().getAsMention() + " elérte a **" + newLevel + ". szintet!**")
                ).queue();
                checkLevelRoles(event, newLevel);
            }

            Database.setXP(event.getGuild().getId(), event.getAuthor().getId(), newXp, newLevel);
        } catch (Exception ignored) {}
    }

    private void checkLevelRoles(MessageReceivedEvent event, int level) {
        try {
            PreparedStatement ps = Database.get().prepareStatement(
                "SELECT role_id FROM level_roles WHERE guild_id = ? AND level = ?");
            ps.setString(1, event.getGuild().getId());
            ps.setInt(2, level);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String roleId = rs.getString("role_id");
                event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(roleId)).queue();
            }
            ps.close();
        } catch (Exception ignored) {}
    }

    public void handle(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();

        switch (event.getName()) {
            case "rank" -> {
                Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
                try {
                    int[] data = Database.getLevel(guildId, target.getId());
                    int xpNeeded = 100 * (data[1] + 1);
                    event.replyEmbeds(EmbedUtil.info(
                        "📈 " + target.getUser().getAsTag() + " rangsora",
                        "**Szint:** " + data[1] + "\n**XP:** " + data[0] + " / " + xpNeeded
                    )).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "leaderboard" -> {
                try {
                    PreparedStatement ps = Database.get().prepareStatement(
                        "SELECT user_id, xp, level FROM levels WHERE guild_id = ? ORDER BY level DESC, xp DESC LIMIT 10");
                    ps.setString(1, guildId);
                    ResultSet rs = ps.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    int i = 1;
                    while (rs.next()) {
                        sb.append("**").append(i++).append(".** <@").append(rs.getString("user_id"))
                          .append("> — Szint ").append(rs.getInt("level")).append(" | XP: ").append(rs.getInt("xp")).append("\n");
                    }
                    event.replyEmbeds(EmbedUtil.info("🏆 XP Ranglista", sb.length() > 0 ? sb.toString() : "Még nincs adat.")).queue();
                    ps.close();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "levelroles" -> {
                try {
                    PreparedStatement ps = Database.get().prepareStatement(
                        "SELECT level, role_id FROM level_roles WHERE guild_id = ? ORDER BY level");
                    ps.setString(1, guildId);
                    ResultSet rs = ps.executeQuery();
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append("**Szint ").append(rs.getInt("level")).append(":** <@&").append(rs.getString("role_id")).append(">\n");
                    }
                    event.replyEmbeds(EmbedUtil.info("📋 Szint szerepkörök", sb.length() > 0 ? sb.toString() : "Nincsenek beállítva.")).queue();
                    ps.close();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "setlevelrole" -> {
                if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) { noPerms(event); return; }
                int level = (int) event.getOption("level").getAsLong();
                String roleId = event.getOption("role").getAsRole().getId();
                try {
                    PreparedStatement ps = Database.get().prepareStatement(
                        "INSERT OR REPLACE INTO level_roles (guild_id, level, role_id) VALUES (?, ?, ?)");
                    ps.setString(1, guildId); ps.setInt(2, level); ps.setString(3, roleId);
                    ps.executeUpdate(); ps.close();
                    event.replyEmbeds(EmbedUtil.success("✅ Szint szerepkör beállítva", level + ". szinthez: <@&" + roleId + ">")).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "remlevelrole" -> {
                if (!event.getMember().hasPermission(Permission.MANAGE_ROLES)) { noPerms(event); return; }
                int level = (int) event.getOption("level").getAsLong();
                try {
                    PreparedStatement ps = Database.get().prepareStatement(
                        "DELETE FROM level_roles WHERE guild_id = ? AND level = ?");
                    ps.setString(1, guildId); ps.setInt(2, level);
                    ps.executeUpdate(); ps.close();
                    event.replyEmbeds(EmbedUtil.success("✅ Eltávolítva", level + ". szint szerepköre eltávolítva.")).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "givexp" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int amount = (int) event.getOption("amount").getAsLong();
                try {
                    int[] data = Database.getLevel(guildId, target.getId());
                    Database.setXP(guildId, target.getId(), data[0] + amount, data[1]);
                    event.replyEmbeds(EmbedUtil.success("✅ XP adva", target.getUser().getAsTag() + " kapott " + amount + " XP-t.")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "removexp" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int amount = (int) event.getOption("amount").getAsLong();
                try {
                    int[] data = Database.getLevel(guildId, target.getId());
                    Database.setXP(guildId, target.getId(), Math.max(0, data[0] - amount), data[1]);
                    event.replyEmbeds(EmbedUtil.success("✅ XP levonva", target.getUser().getAsTag() + "-tól levonva " + amount + " XP.")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "setxp" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int amount = (int) event.getOption("amount").getAsLong();
                try {
                    int[] data = Database.getLevel(guildId, target.getId());
                    Database.setXP(guildId, target.getId(), amount, data[1]);
                    event.replyEmbeds(EmbedUtil.success("✅ XP beállítva", target.getUser().getAsTag() + " XP-je: " + amount)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "setlevel" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int level = (int) event.getOption("level").getAsLong();
                try {
                    Database.setXP(guildId, target.getId(), 0, level);
                    event.replyEmbeds(EmbedUtil.success("✅ Szint beállítva", target.getUser().getAsTag() + " szintje: " + level)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "resetlevel" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                try {
                    Database.setXP(guildId, target.getId(), 0, 0);
                    event.replyEmbeds(EmbedUtil.success("✅ Nullázva", target.getUser().getAsTag() + " szintje nullázva.")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "xpmultiplier" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                double mult = event.getOption("multiplier").getAsDouble();
                try {
                    Database.setConfig(guildId, "xp_multiplier", String.valueOf(mult));
                    event.replyEmbeds(EmbedUtil.success("✅ XP szorzó", "XP szorzó beállítva: x" + mult)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "xptoggle" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                try {
                    String current = Database.getConfig(guildId, "xp_enabled");
                    String newVal = "false".equals(current) ? "true" : "false";
                    Database.setConfig(guildId, "xp_enabled", newVal);
                    event.replyEmbeds(EmbedUtil.success("✅ XP rendszer", "XP rendszer: " + ("true".equals(newVal) ? "🟢 BE" : "🔴 KI"))).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "xpcooldown" -> {
                if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) { noPerms(event); return; }
                int sec = (int) event.getOption("seconds").getAsLong();
                try {
                    Database.setConfig(guildId, "xp_cooldown", String.valueOf(sec));
                    event.replyEmbeds(EmbedUtil.success("✅ XP cooldown", "XP várakozási idő: " + sec + " másodperc")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
        }
    }

    private void noPerms(SlashCommandInteractionEvent event) {
        event.replyEmbeds(EmbedUtil.error("❌ Nincs jogosultságod", "Nincs megfelelő jogosultságod.")).setEphemeral(true).queue();
    }
}
