package com.laccadev.commands.community;

import com.laccadev.database.Database;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class CommunityCommands {

    public void handle(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();

        switch (event.getName()) {
            case "userinfo" -> {
                Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
                String roles = target.getRoles().stream().map(Role::getAsMention).reduce("", (a, b) -> a + " " + b);
                event.replyEmbeds(EmbedUtil.info("👤 " + target.getUser().getAsTag(),
                    "**ID:** " + target.getId() + "\n" +
                    "**Becenév:** " + (target.getNickname() != null ? target.getNickname() : "Nincs") + "\n" +
                    "**Csatlakozott:** " + target.getTimeJoined().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                    "**Fiók létrehozva:** " + target.getUser().getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                    "**Szerepkörök:** " + (roles.isBlank() ? "Nincs" : roles)
                )).queue();
            }
            case "serverinfo" -> {
                var guild = event.getGuild();
                event.replyEmbeds(EmbedUtil.info("🏠 " + guild.getName(),
                    "**ID:** " + guild.getId() + "\n" +
                    "**Tulajdonos:** <@" + guild.getOwnerId() + ">\n" +
                    "**Tagok:** " + guild.getMemberCount() + "\n" +
                    "**Csatornák:** " + guild.getChannels().size() + "\n" +
                    "**Szerepkörök:** " + guild.getRoles().size() + "\n" +
                    "**Boost szint:** " + guild.getBoostTier().getKey() + " (" + guild.getBoostCount() + " boost)\n" +
                    "**Létrehozva:** " + guild.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )).queue();
            }
            case "avatar" -> {
                Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
                String avatarUrl = target.getUser().getEffectiveAvatarUrl() + "?size=512";
                event.replyEmbeds(EmbedUtil.imageEmbed("🖼️ " + target.getUser().getAsTag() + " profilképe", avatarUrl)).queue();
            }
            case "banner" -> {
                Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
                target.getUser().retrieveProfile().queue(profile -> {
                    String bannerUrl = profile.getBannerUrl();
                    if (bannerUrl == null) {
                        event.replyEmbeds(EmbedUtil.error("Nincs banner", target.getUser().getAsTag() + "-nak nincs bannerje.")).queue();
                    } else {
                        event.replyEmbeds(EmbedUtil.imageEmbed("🖼️ " + target.getUser().getAsTag() + " bannere", bannerUrl + "?size=512")).queue();
                    }
                });
            }
            case "roleinfo" -> {
                Role role = event.getOption("role").getAsRole();
                event.replyEmbeds(EmbedUtil.info("🎭 " + role.getName(),
                    "**ID:** " + role.getId() + "\n" +
                    "**Szín:** #" + Integer.toHexString(role.getColorRaw()) + "\n" +
                    "**Taglétszám:** " + event.getGuild().getMembersWithRoles(role).size() + "\n" +
                    "**Mentionelhető:** " + (role.isMentionable() ? "Igen" : "Nem") + "\n" +
                    "**Hoisted:** " + (role.isHoisted() ? "Igen" : "Nem") + "\n" +
                    "**Létrehozva:** " + role.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )).queue();
            }
            case "membercount" -> {
                int total = event.getGuild().getMemberCount();
                long bots = event.getGuild().getMembers().stream().filter(m -> m.getUser().isBot()).count();
                event.replyEmbeds(EmbedUtil.info("👥 Tagok száma",
                    "**Összes:** " + total + "\n**Emberek:** " + (total - bots) + "\n**Botok:** " + bots)).queue();
            }
            case "boosts" -> {
                var guild = event.getGuild();
                StringBuilder sb = new StringBuilder();
                guild.getBoosters().forEach(m -> sb.append(m.getUser().getAsTag()).append("\n"));
                event.replyEmbeds(EmbedUtil.info("🚀 Boosterek",
                    "**Boost szint:** " + guild.getBoostTier().getKey() + "\n**Boostok:** " + guild.getBoostCount() + "\n\n" +
                    (sb.length() > 0 ? sb.toString() : "Nincs booster."))).queue();
            }
            case "inviteinfo" -> {
                String code = event.getOption("code").getAsString();
                event.getJDA().retrieveInvite(code).queue(
                    invite -> event.replyEmbeds(EmbedUtil.info("📨 Meghívó info",
                        "**Kód:** " + invite.getCode() + "\n" +
                        "**Létrehozta:** " + (invite.getInviter() != null ? invite.getInviter().getAsTag() : "Ismeretlen") + "\n" +
                        "**Felhasználva:** " + invite.getUses() + "x\n" +
                        "**Csatorna:** " + (invite.getChannel() != null ? invite.getChannel().getName() : "Ismeretlen")
                    )).queue(),
                    err -> event.replyEmbeds(EmbedUtil.error("Hiba", "Érvénytelen meghívó kód.")).queue()
                );
            }
            case "suggest" -> {
                String suggestion = event.getOption("suggestion").getAsString();
                try {
                    String suggestCh = Database.getConfig(guildId, "suggest_channel");
                    if (suggestCh == null) { event.replyEmbeds(EmbedUtil.error("Hiba", "Nincs javaslat csatorna beállítva!")).setEphemeral(true).queue(); return; }
                    var ch = event.getGuild().getTextChannelById(suggestCh);
                    ch.sendMessageEmbeds(EmbedUtil.info("💡 Javaslat — " + event.getMember().getUser().getAsTag(), suggestion)).queue(msg -> {
                        msg.addReaction(event.getJDA().getEmojiById("✅") != null ? event.getJDA().getEmojiById("✅") : null).queue(s -> {}, e -> {});
                        msg.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("✅")).queue();
                        msg.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("❌")).queue();
                    });
                    event.replyEmbeds(EmbedUtil.success("✅ Javaslat elküldve", suggestion)).setEphemeral(true).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "poll" -> {
                String question = event.getOption("question").getAsString();
                String[] options = event.getOption("options").getAsString().split(",");
                StringBuilder sb = new StringBuilder();
                String[] emojis = {"1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟"};
                for (int i = 0; i < Math.min(options.length, 10); i++) {
                    sb.append(emojis[i]).append(" ").append(options[i].trim()).append("\n");
                }
                int optCount = Math.min(options.length, 10);
                event.getChannel().sendMessageEmbeds(EmbedUtil.info("📊 " + question, sb.toString())).queue(msg -> {
                    for (int i = 0; i < optCount; i++) {
                        final String emoji = emojis[i];
                        msg.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode(emoji)).queue();
                    }
                });
                event.replyEmbeds(EmbedUtil.success("✅ Szavazás létrehozva", "")).setEphemeral(true).queue();
            }
            case "report" -> {
                Member target = event.getOption("user").getAsMember();
                String reason = event.getOption("reason").getAsString();
                try {
                    String logCh = Database.getConfig(guildId, "log_channel");
                    if (logCh != null) {
                        var ch = event.getGuild().getTextChannelById(logCh);
                        ch.sendMessageEmbeds(EmbedUtil.error("🚨 Jelentés",
                            "**Bejelentő:** " + event.getMember().getUser().getAsTag() + "\n" +
                            "**Jelentett:** " + target.getUser().getAsTag() + "\n" +
                            "**Ok:** " + reason)).queue();
                    }
                    event.replyEmbeds(EmbedUtil.success("✅ Jelentés elküldve", "A moderátorok hamarosan megvizsgálják.")).setEphemeral(true).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "afk" -> {
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "AFK";
                try {
                    PreparedStatement ps = Database.get().prepareStatement(
                        "INSERT OR REPLACE INTO afk (guild_id, user_id, reason) VALUES (?, ?, ?)");
                    ps.setString(1, guildId); ps.setString(2, event.getMember().getId()); ps.setString(3, reason);
                    ps.executeUpdate(); ps.close();
                    event.replyEmbeds(EmbedUtil.success("😴 AFK beállítva", "Ok: " + reason)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "approve" -> {
                event.replyEmbeds(EmbedUtil.success("✅ Javaslat elfogadva", "Üzenet ID: " + event.getOption("messageid").getAsString())).queue();
            }
            case "deny" -> {
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Nincs megadva";
                event.replyEmbeds(EmbedUtil.error("❌ Javaslat elutasítva", "Ok: " + reason)).queue();
            }
        }
    }

    public void handleAfkCheck(MessageReceivedEvent event) {
        try {
            // AFK törlése ha üzenetet ír
            PreparedStatement ps = Database.get().prepareStatement(
                "DELETE FROM afk WHERE guild_id = ? AND user_id = ?");
            ps.setString(1, event.getGuild().getId());
            ps.setString(2, event.getAuthor().getId());
            ps.executeUpdate(); ps.close();

            // AFK ellenőrzés mentions-nél
            for (Member mentioned : event.getMessage().getMentions().getMembers()) {
                PreparedStatement ps2 = Database.get().prepareStatement(
                    "SELECT reason FROM afk WHERE guild_id = ? AND user_id = ?");
                ps2.setString(1, event.getGuild().getId());
                ps2.setString(2, mentioned.getId());
                ResultSet rs = ps2.executeQuery();
                if (rs.next()) {
                    event.getChannel().sendMessageEmbeds(EmbedUtil.info("😴 AFK",
                        mentioned.getUser().getAsTag() + " jelenleg AFK: " + rs.getString("reason"))).queue();
                }
                ps2.close();
            }
        } catch (Exception ignored) {}
    }
}
