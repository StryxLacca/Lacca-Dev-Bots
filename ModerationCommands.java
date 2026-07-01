package com.laccadev.commands.moderation;

import com.laccadev.database.Database;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModerationCommands {

    public void handle(SlashCommandInteractionEvent event) {
        Member mod = event.getMember();
        String guildId = event.getGuild().getId();

        switch (event.getName()) {
            case "ban" -> {
                if (!mod.hasPermission(Permission.BAN_MEMBERS)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Nincs megadva";
                event.getGuild().ban(target, 0, TimeUnit.SECONDS).reason(reason).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Kitiltva", target.getUser().getAsTag() + " ki lett tiltva.\n**Ok:** " + reason)).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue()
                );
            }
            case "unban" -> {
                if (!mod.hasPermission(Permission.BAN_MEMBERS)) { noPerms(event); return; }
                String userId = event.getOption("userid").getAsString();
                event.getGuild().unban(event.getJDA().retrieveUserById(userId).complete()).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Kitiltás feloldva", "Felhasználó ID: " + userId)).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", "Nem található a felhasználó.")).queue()
                );
            }
            case "kick" -> {
                if (!mod.hasPermission(Permission.KICK_MEMBERS)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Nincs megadva";
                event.getGuild().kick(target).reason(reason).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Kirúgva", target.getUser().getAsTag() + " ki lett rúgva.\n**Ok:** " + reason)).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue()
                );
            }
            case "timeout" -> {
                if (!mod.hasPermission(Permission.MODERATE_MEMBERS)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int minutes = (int) event.getOption("minutes").getAsLong();
                String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "Nincs megadva";
                target.timeoutFor(Duration.ofMinutes(minutes)).reason(reason).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Timeout", target.getUser().getAsTag() + " " + minutes + " percre el lett némítva.")).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue()
                );
            }
            case "untimeout" -> {
                if (!mod.hasPermission(Permission.MODERATE_MEMBERS)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                target.removeTimeout().queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Timeout feloldva", target.getUser().getAsTag() + " timeoutja feloldva.")).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue()
                );
            }
            case "mute" -> {
                if (!mod.hasPermission(Permission.MANAGE_ROLES)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String muteRoleId;
                try { muteRoleId = Database.getConfig(guildId, "mute_role"); } catch (Exception e) { muteRoleId = null; }
                if (muteRoleId == null) { event.replyEmbeds(EmbedUtil.error("Hiba", "Nincs mute szerepkör beállítva! Használd a /setup parancsot.")).setEphemeral(true).queue(); return; }
                event.getGuild().addRoleToMember(target, event.getGuild().getRoleById(muteRoleId)).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Némítva", target.getUser().getAsTag() + " el lett némítva.")).queue()
                );
            }
            case "unmute" -> {
                if (!mod.hasPermission(Permission.MANAGE_ROLES)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String muteRoleId;
                try { muteRoleId = Database.getConfig(guildId, "mute_role"); } catch (Exception e) { muteRoleId = null; }
                if (muteRoleId == null) { event.replyEmbeds(EmbedUtil.error("Hiba", "Nincs mute szerepkör beállítva!")).setEphemeral(true).queue(); return; }
                event.getGuild().removeRoleFromMember(target, event.getGuild().getRoleById(muteRoleId)).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Némítás feloldva", target.getUser().getAsTag() + " némítása feloldva.")).queue()
                );
            }
            case "warn" -> {
                if (!mod.hasPermission(Permission.MODERATE_MEMBERS)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String reason = event.getOption("reason").getAsString();
                try {
                    Database.addWarning(guildId, target.getId(), reason, mod.getId());
                    event.replyEmbeds(EmbedUtil.success("⚠️ Figyelmeztetés", target.getUser().getAsTag() + " figyelmeztetést kapott.\n**Ok:** " + reason)).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "warnings" -> {
                Member target = event.getOption("user").getAsMember();
                try {
                    ResultSet rs = Database.getWarnings(guildId, target.getId());
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        sb.append("**").append(count).append(".** ").append(rs.getString("reason"))
                          .append(" — <@").append(rs.getString("moderator_id")).append("> | ")
                          .append(rs.getString("timestamp")).append("\n");
                    }
                    String desc = count == 0 ? "Nincs figyelmeztetés." : sb.toString();
                    event.replyEmbeds(EmbedUtil.info("⚠️ Figyelmeztetések — " + target.getUser().getAsTag(), desc)).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "clear" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                int amount = (int) event.getOption("amount").getAsLong();
                if (amount < 1 || amount > 100) { event.replyEmbeds(EmbedUtil.error("Hiba", "1 és 100 közé kell esnie!")).setEphemeral(true).queue(); return; }
                event.deferReply(true).queue();
                TextChannel ch = event.getChannel().asTextChannel();
                ch.getHistory().retrievePast(amount).queue(msgs -> {
                    ch.deleteMessages(msgs).queue(s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Törölve", amount + " üzenet törölve.")).queue());
                });
            }
            case "purgebots" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                int amount = (int) event.getOption("amount").getAsLong();
                event.deferReply(true).queue();
                event.getChannel().asTextChannel().getHistory().retrievePast(amount).queue(msgs -> {
                    List<Message> botMsgs = msgs.stream().filter(m -> m.getAuthor().isBot()).toList();
                    event.getChannel().asTextChannel().deleteMessages(botMsgs).queue(
                        s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Bot üzenetek törölve", botMsgs.size() + " üzenet törölve.")).queue()
                    );
                });
            }
            case "purgeuser" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                int amount = (int) event.getOption("amount").getAsLong();
                event.deferReply(true).queue();
                event.getChannel().asTextChannel().getHistory().retrievePast(amount).queue(msgs -> {
                    List<Message> userMsgs = msgs.stream().filter(m -> m.getAuthor().getId().equals(target.getId())).toList();
                    event.getChannel().asTextChannel().deleteMessages(userMsgs).queue(
                        s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Törölve", userMsgs.size() + " üzenet törölve " + target.getUser().getAsTag() + "-tól.")).queue()
                    );
                });
            }
            case "purgeimages" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                int amount = (int) event.getOption("amount").getAsLong();
                event.deferReply(true).queue();
                event.getChannel().asTextChannel().getHistory().retrievePast(amount).queue(msgs -> {
                    List<Message> imgMsgs = msgs.stream().filter(m -> !m.getAttachments().isEmpty()).toList();
                    event.getChannel().asTextChannel().deleteMessages(imgMsgs).queue(
                        s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Képek törölve", imgMsgs.size() + " üzenet törölve.")).queue()
                    );
                });
            }
            case "purgelinks" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                int amount = (int) event.getOption("amount").getAsLong();
                event.deferReply(true).queue();
                event.getChannel().asTextChannel().getHistory().retrievePast(amount).queue(msgs -> {
                    List<Message> linkMsgs = msgs.stream().filter(m -> m.getContentRaw().contains("http://") || m.getContentRaw().contains("https://")).toList();
                    event.getChannel().asTextChannel().deleteMessages(linkMsgs).queue(
                        s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Linkek törölve", linkMsgs.size() + " üzenet törölve.")).queue()
                    );
                });
            }
            case "purgecontains" -> {
                if (!mod.hasPermission(Permission.MESSAGE_MANAGE)) { noPerms(event); return; }
                String text = event.getOption("text").getAsString();
                int amount = (int) event.getOption("amount").getAsLong();
                event.deferReply(true).queue();
                event.getChannel().asTextChannel().getHistory().retrievePast(amount).queue(msgs -> {
                    List<Message> filtered = msgs.stream().filter(m -> m.getContentRaw().contains(text)).toList();
                    event.getChannel().asTextChannel().deleteMessages(filtered).queue(
                        s -> event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Törölve", filtered.size() + " \"" + text + "\" szöveget tartalmazó üzenet törölve.")).queue()
                    );
                });
            }
            case "lock" -> {
                if (!mod.hasPermission(Permission.MANAGE_CHANNEL)) { noPerms(event); return; }
                TextChannel ch = event.getChannel().asTextChannel();
                ch.getManager().putMemberPermissionOverride(
                    event.getGuild().getPublicRole().getIdLong(),
                    0L, Permission.MESSAGE_SEND.getRawValue()
                ).queue(s -> event.replyEmbeds(EmbedUtil.success("🔒 Csatorna lezárva", ch.getAsMention() + " le lett zárva.")).queue());
            }
            case "unlock" -> {
                if (!mod.hasPermission(Permission.MANAGE_CHANNEL)) { noPerms(event); return; }
                TextChannel ch = event.getChannel().asTextChannel();
                ch.getManager().putMemberPermissionOverride(
                    event.getGuild().getPublicRole().getIdLong(),
                    Permission.MESSAGE_SEND.getRawValue(), 0L
                ).queue(s -> event.replyEmbeds(EmbedUtil.success("🔓 Csatorna feloldva", ch.getAsMention() + " feloldva.")).queue());
            }
            case "slowmode" -> {
                if (!mod.hasPermission(Permission.MANAGE_CHANNEL)) { noPerms(event); return; }
                int seconds = (int) event.getOption("seconds").getAsLong();
                event.getChannel().asTextChannel().getManager().setSlowmode(seconds).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Slowmode", "Lassú mód: " + seconds + " másodperc")).queue()
                );
            }
            case "nick" -> {
                if (!mod.hasPermission(Permission.NICKNAME_MANAGE)) { noPerms(event); return; }
                Member target = event.getOption("user").getAsMember();
                String nick = event.getOption("nick").getAsString();
                target.modifyNickname(nick).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Becenév módosítva", target.getUser().getAsTag() + " új beceneve: " + nick)).queue(),
                    e -> event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue()
                );
            }
        }
    }

    private void noPerms(SlashCommandInteractionEvent event) {
        event.replyEmbeds(EmbedUtil.error("❌ Nincs jogosultságod", "Nincs megfelelő jogosultságod ehhez a parancshoz.")).setEphemeral(true).queue();
    }
}
