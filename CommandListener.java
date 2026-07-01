package com.laccadev.listeners;

import com.laccadev.commands.admin.*;
import com.laccadev.commands.community.*;
import com.laccadev.commands.fun.*;
import com.laccadev.commands.general.*;
import com.laccadev.commands.levels.*;
import com.laccadev.commands.moderation.*;
import com.laccadev.commands.owner.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

    private final ModerationCommands moderation = new ModerationCommands();
    private final AdminCommands admin = new AdminCommands();
    private final LevelCommands levels = new LevelCommands();
    private final CommunityCommands community = new CommunityCommands();
    private final FunCommands fun = new FunCommands();
    private final GeneralCommands general = new GeneralCommands();
    private final OwnerCommands owner = new OwnerCommands();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String cmd = event.getName();

        // 👮 Moderáció
        switch (cmd) {
            case "ban", "unban", "kick", "timeout", "untimeout",
                 "mute", "unmute", "warn", "warnings", "clear",
                 "purgebots", "purgeuser", "purgeimages", "purgelinks",
                 "purgecontains", "lock", "unlock", "slowmode", "nick"
                    -> moderation.handle(event);

            // ⚙️ Admin
            case "setup", "config", "welcome", "goodbye", "autorole",
                 "logs", "backup", "restore", "verification", "announcement",
                 "embed", "reactionrole", "ticketsetup", "starboard"
                    -> admin.handle(event);

            // 📈 Szint
            case "rank", "leaderboard", "levelroles", "setlevelrole", "remlevelrole",
                 "givexp", "removexp", "setxp", "setlevel", "resetlevel",
                 "xpmultiplier", "xptoggle", "xpcooldown"
                    -> levels.handle(event);

            // 💬 Közösségi
            case "userinfo", "serverinfo", "avatar", "banner", "roleinfo",
                 "membercount", "boosts", "inviteinfo", "suggest", "approve",
                 "deny", "poll", "report", "afk"
                    -> community.handle(event);

            // 🎉 Szórakozás
            case "meme", "cat", "dog", "8ball", "coinflip", "roll", "joke",
                 "ship", "rate", "pp", "gay", "hack", "iq", "kiss", "hug", "slap"
                    -> fun.handle(event);

            // 🤖 Általános
            case "help", "ping", "uptime", "botinfo"
                    -> general.handle(event);

            // 👑 Tulajdonosi
            case "reload", "restart", "shutdown", "status", "maintenance"
                    -> owner.handle(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        // XP szerzés üzenetküldésnél
        levels.handleXP(event);
        // AFK ellenőrzés
        community.handleAfkCheck(event);
    }
}
