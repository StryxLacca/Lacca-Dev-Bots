package com.laccadev;

import com.laccadev.database.Database;
import com.laccadev.listeners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.time.Instant;

public class Main {

    public static final String VERSION = "1.0.0";
    public static final String BOT_NAME = "Lacca Dev Bot";
    public static Instant startTime;

    public static void main(String[] args) throws Exception {
        String token = System.getenv("BOT_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("Hiányzó BOT_TOKEN környezeti változó!");
            System.exit(1);
        }

        Database.init();
        startTime = Instant.now();

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MODERATION
                )
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("Lacca Dev Server"))
                .addEventListeners(new CommandListener())
                .build();

        jda.awaitReady();
        registerCommands(jda);
        System.out.println(BOT_NAME + " v" + VERSION + " elindult!");
    }

    private static void registerCommands(JDA jda) {
        jda.updateCommands().addCommands(
            // 👮 Moderáció
            Commands.slash("ban", "Végleg kitilt egy felhasználót").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "reason", "Ok", false),
            Commands.slash("unban", "Feloldja a kitiltást").addOption(OptionType.STRING, "userid", "Felhasználó ID", true),
            Commands.slash("kick", "Kirúgja a felhasználót").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "reason", "Ok", false),
            Commands.slash("timeout", "Ideiglenesen némítja").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "minutes", "Percek", true).addOption(OptionType.STRING, "reason", "Ok", false),
            Commands.slash("untimeout", "Leveszi a timeoutot").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("mute", "Némítja a felhasználót").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "reason", "Ok", false),
            Commands.slash("unmute", "Feloldja a némítást").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("warn", "Figyelmeztetést ad").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "reason", "Ok", true),
            Commands.slash("warnings", "Figyelmeztetések listája").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("clear", "Üzenetek törlése").addOption(OptionType.INTEGER, "amount", "Mennyiség (1-100)", true),
            Commands.slash("purgebots", "Botok üzeneteinek törlése").addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("purgeuser", "Egy user üzeneteinek törlése").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("purgeimages", "Képek törlése").addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("purgelinks", "Linkek törlése").addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("purgecontains", "Adott szöveget tartalmazó üzenetek törlése").addOption(OptionType.STRING, "text", "Szöveg", true).addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("lock", "Lezárja a csatornát"),
            Commands.slash("unlock", "Feloldja a csatorna zárolását"),
            Commands.slash("slowmode", "Lassú mód beállítása").addOption(OptionType.INTEGER, "seconds", "Másodpercek", true),
            Commands.slash("nick", "Becenév megváltoztatása").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "nick", "Új becenév", true),

            // ⚙️ Admin
            Commands.slash("setup", "Bot alapbeállítások"),
            Commands.slash("config", "Bot konfiguráció"),
            Commands.slash("welcome", "Üdvözlő üzenet beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true).addOption(OptionType.STRING, "message", "Üzenet", true),
            Commands.slash("goodbye", "Kilépési üzenet beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true).addOption(OptionType.STRING, "message", "Üzenet", true),
            Commands.slash("autorole", "Automatikus szerepkör beállítása").addOption(OptionType.ROLE, "role", "Szerepkör", true),
            Commands.slash("logs", "Log csatorna beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true),
            Commands.slash("backup", "Szerver beállításainak mentése"),
            Commands.slash("restore", "Mentett beállítások visszaállítása"),
            Commands.slash("verification", "Verify rendszer beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true),
            Commands.slash("announcement", "Bejelentés küldése").addOption(OptionType.CHANNEL, "channel", "Csatorna", true).addOption(OptionType.STRING, "message", "Üzenet", true),
            Commands.slash("embed", "Embed üzenet létrehozása").addOption(OptionType.STRING, "title", "Cím", true).addOption(OptionType.STRING, "description", "Leírás", true).addOption(OptionType.STRING, "color", "Szín (hex)", false),
            Commands.slash("reactionrole", "Reakciós szerepkör létrehozása").addOption(OptionType.STRING, "messageid", "Üzenet ID", true).addOption(OptionType.STRING, "emoji", "Emoji", true).addOption(OptionType.ROLE, "role", "Szerepkör", true),
            Commands.slash("ticketsetup", "Ticket rendszer beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true),
            Commands.slash("starboard", "Starboard beállítása").addOption(OptionType.CHANNEL, "channel", "Csatorna", true).addOption(OptionType.INTEGER, "stars", "Csillagok száma", true),

            // 📈 Szint rendszer
            Commands.slash("rank", "Saját szinted és XP-d").addOption(OptionType.USER, "user", "Felhasználó (opcionális)", false),
            Commands.slash("leaderboard", "XP ranglista"),
            Commands.slash("levelroles", "Szint szerepkörök listája"),
            Commands.slash("setlevelrole", "Szerepkör beállítása szinthez").addOption(OptionType.INTEGER, "level", "Szint", true).addOption(OptionType.ROLE, "role", "Szerepkör", true),
            Commands.slash("remlevelrole", "Szint szerepkör eltávolítása").addOption(OptionType.INTEGER, "level", "Szint", true),
            Commands.slash("givexp", "XP adása").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("removexp", "XP levonása").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("setxp", "XP beállítása").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "amount", "Mennyiség", true),
            Commands.slash("setlevel", "Szint beállítása").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.INTEGER, "level", "Szint", true),
            Commands.slash("resetlevel", "Szint nullázása").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("xpmultiplier", "XP szorzó beállítása").addOption(OptionType.NUMBER, "multiplier", "Szorzó (pl. 1.5)", true),
            Commands.slash("xptoggle", "XP rendszer be/ki"),
            Commands.slash("xpcooldown", "XP várakozási idő").addOption(OptionType.INTEGER, "seconds", "Másodpercek", true),

            // 💬 Közösségi
            Commands.slash("userinfo", "Felhasználó adatai").addOption(OptionType.USER, "user", "Felhasználó", false),
            Commands.slash("serverinfo", "Szerver adatai"),
            Commands.slash("avatar", "Profilkép megjelenítése").addOption(OptionType.USER, "user", "Felhasználó", false),
            Commands.slash("banner", "Banner megjelenítése").addOption(OptionType.USER, "user", "Felhasználó", false),
            Commands.slash("roleinfo", "Szerepkör adatai").addOption(OptionType.ROLE, "role", "Szerepkör", true),
            Commands.slash("membercount", "Tagok száma"),
            Commands.slash("boosts", "Booster információk"),
            Commands.slash("inviteinfo", "Meghívó információk").addOption(OptionType.STRING, "code", "Meghívó kód", true),
            Commands.slash("suggest", "Ötlet beküldése").addOption(OptionType.STRING, "suggestion", "Ötlet", true),
            Commands.slash("approve", "Ötlet elfogadása").addOption(OptionType.STRING, "messageid", "Üzenet ID", true),
            Commands.slash("deny", "Ötlet elutasítása").addOption(OptionType.STRING, "messageid", "Üzenet ID", true).addOption(OptionType.STRING, "reason", "Ok", false),
            Commands.slash("poll", "Szavazás létrehozása").addOption(OptionType.STRING, "question", "Kérdés", true).addOption(OptionType.STRING, "options", "Opciók (vesszővel elválasztva)", true),
            Commands.slash("report", "Felhasználó jelentése").addOption(OptionType.USER, "user", "Felhasználó", true).addOption(OptionType.STRING, "reason", "Ok", true),
            Commands.slash("afk", "AFK státusz beállítása").addOption(OptionType.STRING, "reason", "Ok", false),

            // 🎉 Szórakozás
            Commands.slash("meme", "Véletlen mém"),
            Commands.slash("cat", "Véletlen macskás kép"),
            Commands.slash("dog", "Véletlen kutyás kép"),
            Commands.slash("8ball", "Véletlenszerű válasz").addOption(OptionType.STRING, "question", "Kérdés", true),
            Commands.slash("coinflip", "Fej vagy írás"),
            Commands.slash("roll", "Dobókocka").addOption(OptionType.INTEGER, "sides", "Oldalak száma (alap: 6)", false),
            Commands.slash("joke", "Vicc"),
            Commands.slash("ship", "Összeillőség").addOption(OptionType.USER, "user1", "1. felhasználó", true).addOption(OptionType.USER, "user2", "2. felhasználó", true),
            Commands.slash("rate", "Értékelés").addOption(OptionType.STRING, "thing", "Mit értékelünk?", true),
            Commands.slash("pp", "Vicces PP méret"),
            Commands.slash("gay", "Vicces százalék"),
            Commands.slash("hack", "Hamis hackelés animáció").addOption(OptionType.USER, "user", "Célpont", true),
            Commands.slash("iq", "Vicces IQ generálása").addOption(OptionType.USER, "user", "Felhasználó", false),
            Commands.slash("kiss", "Virtuális puszi").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("hug", "Virtuális ölelés").addOption(OptionType.USER, "user", "Felhasználó", true),
            Commands.slash("slap", "Virtuális pofon").addOption(OptionType.USER, "user", "Felhasználó", true),

            // 🤖 Általános
            Commands.slash("help", "Parancsok listája"),
            Commands.slash("ping", "Bot válaszideje"),
            Commands.slash("uptime", "Bot futási ideje"),
            Commands.slash("botinfo", "Bot információk"),

            // 👑 Tulajdonosi
            Commands.slash("reload", "Parancsok újratöltése"),
            Commands.slash("restart", "Bot újraindítása"),
            Commands.slash("shutdown", "Bot leállítása"),
            Commands.slash("status", "Bot státuszának módosítása").addOption(OptionType.STRING, "status", "Új státusz", true),
            Commands.slash("maintenance", "Karbantartási mód")
        ).queue();
        System.out.println("Slash parancsok regisztrálva!");
    }
}
