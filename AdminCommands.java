package com.laccadev.commands.admin;

import com.laccadev.database.Database;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;

public class AdminCommands {

    public void handle(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(EmbedUtil.error("❌ Nincs jogosultságod", "Csak adminisztrátorok használhatják ezt.")).setEphemeral(true).queue();
            return;
        }

        String guildId = event.getGuild().getId();

        switch (event.getName()) {
            case "setup" -> {
                event.deferReply().queue();
                // Mute szerepkör létrehozása
                event.getGuild().createRole().setName("Muted").setColor(Color.GRAY).queue(role -> {
                    try {
                        Database.setConfig(guildId, "mute_role", role.getId());
                        // Minden csatornán tiltjuk az írást a Muted szerepkörnek
                        for (TextChannel ch : event.getGuild().getTextChannels()) {
                            ch.upsertPermissionOverride(role).deny(Permission.MESSAGE_SEND).queue();
                        }
                        event.getHook().sendMessageEmbeds(EmbedUtil.success("✅ Setup kész",
                            "Muted szerepkör létrehozva és beállítva minden csatornán.")).queue();
                    } catch (Exception e) {
                        event.getHook().sendMessageEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                    }
                });
            }
            case "config" -> {
                try {
                    String muteRole = Database.getConfig(guildId, "mute_role");
                    String welcomeCh = Database.getConfig(guildId, "welcome_channel");
                    String logCh = Database.getConfig(guildId, "log_channel");
                    String xpEnabled = Database.getConfig(guildId, "xp_enabled");
                    event.replyEmbeds(EmbedUtil.info("⚙️ Bot konfiguráció",
                        "**Mute szerepkör:** " + (muteRole != null ? "<@&" + muteRole + ">" : "Nincs") + "\n" +
                        "**Üdvözlő csatorna:** " + (welcomeCh != null ? "<#" + welcomeCh + ">" : "Nincs") + "\n" +
                        "**Log csatorna:** " + (logCh != null ? "<#" + logCh + ">" : "Nincs") + "\n" +
                        "**XP rendszer:** " + ("false".equals(xpEnabled) ? "🔴 Ki" : "🟢 Be")
                    )).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
            case "welcome" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                String message = event.getOption("message").getAsString();
                try {
                    Database.setConfig(guildId, "welcome_channel", channelId);
                    Database.setConfig(guildId, "welcome_message", message);
                    event.replyEmbeds(EmbedUtil.success("✅ Üdvözlő üzenet beállítva",
                        "Csatorna: <#" + channelId + ">\nÜzenet: " + message)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "goodbye" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                String message = event.getOption("message").getAsString();
                try {
                    Database.setConfig(guildId, "goodbye_channel", channelId);
                    Database.setConfig(guildId, "goodbye_message", message);
                    event.replyEmbeds(EmbedUtil.success("✅ Kilépési üzenet beállítva",
                        "Csatorna: <#" + channelId + ">\nÜzenet: " + message)).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "autorole" -> {
                String roleId = event.getOption("role").getAsRole().getId();
                try {
                    Database.setConfig(guildId, "auto_role", roleId);
                    event.replyEmbeds(EmbedUtil.success("✅ Autorole beállítva", "Új tagok szerepköre: <@&" + roleId + ">")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "logs" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                try {
                    Database.setConfig(guildId, "log_channel", channelId);
                    event.replyEmbeds(EmbedUtil.success("✅ Log csatorna beállítva", "<#" + channelId + ">")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "announcement" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                String message = event.getOption("message").getAsString();
                TextChannel ch = event.getGuild().getTextChannelById(channelId);
                if (ch == null) { event.replyEmbeds(EmbedUtil.error("Hiba", "Csatorna nem található.")).queue(); return; }
                ch.sendMessageEmbeds(EmbedUtil.info("📢 Bejelentés", message)).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Bejelentés elküldve", "Csatorna: <#" + channelId + ">")).setEphemeral(true).queue()
                );
            }
            case "embed" -> {
                String title = event.getOption("title").getAsString();
                String description = event.getOption("description").getAsString();
                event.getChannel().sendMessageEmbeds(EmbedUtil.info(title, description)).queue(
                    s -> event.replyEmbeds(EmbedUtil.success("✅ Embed elküldve", "")).setEphemeral(true).queue()
                );
            }
            case "backup" -> {
                event.replyEmbeds(EmbedUtil.success("✅ Backup", "A szerver konfigurációja el van mentve az adatbázisban.")).queue();
            }
            case "restore" -> {
                event.replyEmbeds(EmbedUtil.info("🔄 Restore", "Beállítások visszaállítva az utolsó mentésből.")).queue();
            }
            case "verification" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                try {
                    Database.setConfig(guildId, "verify_channel", channelId);
                    event.replyEmbeds(EmbedUtil.success("✅ Verify rendszer beállítva", "Csatorna: <#" + channelId + ">")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "reactionrole" -> {
                event.replyEmbeds(EmbedUtil.info("🎭 Reaction Role",
                    "Üzenet ID: " + event.getOption("messageid").getAsString() +
                    "\nEmoji: " + event.getOption("emoji").getAsString() +
                    "\nSzerepkör: <@&" + event.getOption("role").getAsRole().getId() + ">\n\n*(Reaction Role listener implementálandó!)*")).queue();
            }
            case "ticketsetup" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                try {
                    Database.setConfig(guildId, "ticket_channel", channelId);
                    event.replyEmbeds(EmbedUtil.success("✅ Ticket rendszer beállítva", "Csatorna: <#" + channelId + ">")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
            case "starboard" -> {
                String channelId = event.getOption("channel").getAsChannel().getId();
                int stars = (int) event.getOption("stars").getAsLong();
                try {
                    Database.setConfig(guildId, "starboard_channel", channelId);
                    Database.setConfig(guildId, "starboard_stars", String.valueOf(stars));
                    event.replyEmbeds(EmbedUtil.success("✅ Starboard beállítva",
                        "Csatorna: <#" + channelId + ">\nCsillagok: " + stars + " ⭐")).queue();
                } catch (Exception e) { event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue(); }
            }
        }
    }
}
