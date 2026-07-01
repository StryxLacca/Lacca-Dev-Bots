package com.laccadev.commands.fun;

import com.laccadev.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Random;

public class FunCommands {

    private final Random random = new Random();

    private final String[] EIGHTBALL = {
        "Igen! 🟢", "Valószínűleg igen. 🟢", "Határozott igen! 🟢", "Igen, biztosan. 🟢",
        "Nem 🔴", "Valószínűleg nem. 🔴", "Határozottan nem! 🔴", "Semmi esélye. 🔴",
        "Nem tudni. 🟡", "Kérdezz később. 🟡", "Bizonytalan. 🟡"
    };

    private final String[] JOKES = {
        "Miért sír a matematika könyv? Mert tele van problémákkal.",
        "Mi a fekete és fehér és piros? Egy sunburnt zebra!",
        "Miért nem játszik az erdő kártyát? Mert túl sok puskos van benne.",
        "Mit mond a tehén a másik tehénnek? Muu-cool vagyok!",
        "Miért nem tud a csontváznak barátja lenni? Mert nincs gerince hozzá."
    };

    public void handle(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "meme" -> {
                // Egyszerű placeholder – valós API integrációhoz cseréld le
                String[] memes = {
                    "https://i.imgur.com/example1.jpg",
                    "https://i.imgur.com/example2.jpg"
                };
                event.replyEmbeds(
                    EmbedUtil.imageEmbed("😂 Véletlenszerű mém", memes[random.nextInt(memes.length)])
                ).queue();
            }
            case "cat" -> {
                // Valós API: https://api.thecatapi.com/v1/images/search
                event.replyEmbeds(EmbedUtil.info("🐱 Macska!", "Kép betöltése... (integrálj The Cat API-t a valódi képekért!)")).queue();
            }
            case "dog" -> {
                event.replyEmbeds(EmbedUtil.info("🐶 Kutya!", "Kép betöltése... (integrálj Dog CEO API-t a valódi képekért!)")).queue();
            }
            case "8ball" -> {
                String question = event.getOption("question").getAsString();
                String answer = EIGHTBALL[random.nextInt(EIGHTBALL.length)];
                event.replyEmbeds(EmbedUtil.info("🎱 8-as labda",
                    "**Kérdés:** " + question + "\n**Válasz:** " + answer)).queue();
            }
            case "coinflip" -> {
                String result = random.nextBoolean() ? "🪙 Fej!" : "🪙 Írás!";
                event.replyEmbeds(EmbedUtil.info("Fej vagy írás?", result)).queue();
            }
            case "roll" -> {
                int sides = event.getOption("sides") != null ? (int) event.getOption("sides").getAsLong() : 6;
                int result = random.nextInt(sides) + 1;
                event.replyEmbeds(EmbedUtil.info("🎲 Kockadobás", "**Dobott szám:** " + result + " (d" + sides + ")")).queue();
            }
            case "joke" -> {
                event.replyEmbeds(EmbedUtil.info("😄 Vicc", JOKES[random.nextInt(JOKES.length)])).queue();
            }
            case "ship" -> {
                Member u1 = event.getOption("user1").getAsMember();
                Member u2 = event.getOption("user2").getAsMember();
                int pct = random.nextInt(101);
                String bar = "█".repeat(pct / 10) + "░".repeat(10 - pct / 10);
                event.replyEmbeds(EmbedUtil.info("💕 Ship",
                    u1.getUser().getAsTag() + " ❤️ " + u2.getUser().getAsTag() +
                    "\n`" + bar + "` **" + pct + "%**")).queue();
            }
            case "rate" -> {
                String thing = event.getOption("thing").getAsString();
                int pct = random.nextInt(101);
                event.replyEmbeds(EmbedUtil.info("⭐ Értékelés",
                    "**" + thing + "** értékelése: **" + pct + "%**")).queue();
            }
            case "pp" -> {
                int size = random.nextInt(20);
                String pp = "8" + "=".repeat(size) + "D";
                event.replyEmbeds(EmbedUtil.info("🍆 PP méret",
                    event.getMember().getUser().getAsTag() + " PP-je:\n`" + pp + "`")).queue();
            }
            case "gay" -> {
                int pct = random.nextInt(101);
                event.replyEmbeds(EmbedUtil.info("🌈 Gay teszt",
                    event.getMember().getUser().getAsTag() + " **" + pct + "%** gay 🏳️‍🌈")).queue();
            }
            case "hack" -> {
                Member target = event.getOption("user").getAsMember();
                event.replyEmbeds(EmbedUtil.info("💻 Hackelés...",
                    "Hackelés folyamatban: " + target.getUser().getAsTag() + "\n" +
                    "`[██████████] 100%`\n" +
                    "✅ Jelszó megtalálva: `hunter2`\n" +
                    "✅ Email: `" + target.getId() + "@gmail.com`\n" +
                    "*(Ez csak vicc! 😄)*")).queue();
            }
            case "iq" -> {
                Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : event.getMember();
                int iq = random.nextInt(200) + 1;
                event.replyEmbeds(EmbedUtil.info("🧠 IQ teszt",
                    target.getUser().getAsTag() + " IQ-ja: **" + iq + "**")).queue();
            }
            case "kiss" -> {
                Member target = event.getOption("user").getAsMember();
                event.replyEmbeds(EmbedUtil.info("😘 Puszi",
                    event.getMember().getUser().getAsTag() + " puszit küld " + target.getUser().getAsTag() + "-nak! 💋")).queue();
            }
            case "hug" -> {
                Member target = event.getOption("user").getAsMember();
                event.replyEmbeds(EmbedUtil.info("🤗 Ölelés",
                    event.getMember().getUser().getAsTag() + " megöleli " + target.getUser().getAsTag() + "-t! 🫂")).queue();
            }
            case "slap" -> {
                Member target = event.getOption("user").getAsMember();
                event.replyEmbeds(EmbedUtil.info("👋 Pofon",
                    event.getMember().getUser().getAsTag() + " pofonvágja " + target.getUser().getAsTag() + "-t! 💥")).queue();
            }
        }
    }
}
