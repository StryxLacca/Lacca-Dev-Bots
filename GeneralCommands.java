package com.laccadev.commands.general;

import com.laccadev.Main;
import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.time.Duration;
import java.time.Instant;

public class GeneralCommands {

    public void handle(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping" -> {
                long ping = event.getJDA().getGatewayPing();
                event.replyEmbeds(EmbedUtil.info("🏓 Pong!", "Válaszidő: **" + ping + "ms**")).queue();
            }
            case "uptime" -> {
                Duration uptime = Duration.between(Main.startTime, Instant.now());
                long days = uptime.toDays();
                long hours = uptime.toHoursPart();
                long minutes = uptime.toMinutesPart();
                long seconds = uptime.toSecondsPart();
                event.replyEmbeds(EmbedUtil.info("⏱️ Uptime",
                    days + " nap, " + hours + " óra, " + minutes + " perc, " + seconds + " másodperc")).queue();
            }
            case "botinfo" -> {
                event.replyEmbeds(EmbedUtil.info("🤖 Bot információk",
                    "**Név:** " + Main.BOT_NAME + "\n" +
                    "**Verzió:** " + Main.VERSION + "\n" +
                    "**Fejlesztő:** Lacca Dev\n" +
                    "**Szerverek:** " + event.getJDA().getGuilds().size() + "\n" +
                    "**Ping:** " + event.getJDA().getGatewayPing() + "ms"
                )).queue();
            }
            case "help" -> {
                event.replyEmbeds(EmbedUtil.info("📋 " + Main.BOT_NAME + " — Parancsok",
                    "**👮 Moderáció:** /ban /unban /kick /timeout /mute /unmute /warn /warnings /clear /lock /unlock /slowmode /nick + purge parancsok\n\n" +
                    "**⚙️ Admin:** /setup /config /welcome /goodbye /autorole /logs /backup /restore /verification /announcement /embed /reactionrole /ticketsetup /starboard\n\n" +
                    "**📈 Szint:** /rank /leaderboard /levelroles /setlevelrole /givexp /removexp /setxp /setlevel /resetlevel /xpmultiplier /xptoggle /xpcooldown\n\n" +
                    "**💬 Közösségi:** /userinfo /serverinfo /avatar /banner /roleinfo /membercount /suggest /poll /report /afk\n\n" +
                    "**🎉 Szórakozás:** /meme /cat /dog /8ball /coinflip /roll /joke /ship /rate /pp /gay /hack /iq /kiss /hug /slap\n\n" +
                    "**🤖 Általános:** /help /ping /uptime /botinfo"
                )).queue();
            }
        }
    }
}
