package com.laccadev.commands.owner;

import com.laccadev.database.Database;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class OwnerCommands {

    // A bot tulajdonosának Discord ID-ja - cseréld le a saját ID-dra!
    private static final String OWNER_ID = "IDE_JON_A_TE_DISCORD_ID-D";

    public void handle(SlashCommandInteractionEvent event) {
        if (!event.getMember().getId().equals(OWNER_ID)) {
            event.replyEmbeds(EmbedUtil.error("❌ Nincs jogosultságod", "Csak a bot tulajdonosa használhatja ezt.")).setEphemeral(true).queue();
            return;
        }

        switch (event.getName()) {
            case "reload" -> {
                event.replyEmbeds(EmbedUtil.success("🔄 Újratöltve", "Parancsok újratöltve.")).queue();
            }
            case "shutdown" -> {
                event.replyEmbeds(EmbedUtil.success("🛑 Leállítás", "Bot leáll...")).queue(s -> {
                    event.getJDA().shutdown();
                    System.exit(0);
                });
            }
            case "restart" -> {
                event.replyEmbeds(EmbedUtil.success("🔁 Újraindítás", "Bot újraindul...")).queue(s -> {
                    event.getJDA().shutdown();
                    // Újraindítás a futtatási környezettől függ (pl. systemd, screen, pm2)
                    System.exit(1);
                });
            }
            case "status" -> {
                String status = event.getOption("status").getAsString();
                event.getJDA().getPresence().setActivity(Activity.playing(status));
                event.replyEmbeds(EmbedUtil.success("✅ Státusz módosítva", "Új státusz: " + status)).queue();
            }
            case "maintenance" -> {
                try {
                    String current = Database.getConfig("global", "maintenance");
                    String newVal = "true".equals(current) ? "false" : "true";
                    Database.setConfig("global", "maintenance", newVal);
                    event.replyEmbeds(EmbedUtil.success("🔧 Karbantartási mód",
                        "Karbantartási mód: " + ("true".equals(newVal) ? "🔴 BE" : "🟢 KI"))).queue();
                } catch (Exception e) {
                    event.replyEmbeds(EmbedUtil.error("Hiba", e.getMessage())).queue();
                }
            }
        }
    }
}
