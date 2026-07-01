package com.laccadev.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.time.Instant;

public class EmbedUtil {

    public static MessageEmbed success(String title, String description) {
        return new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.GREEN)
            .setTimestamp(Instant.now())
            .build();
    }

    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(Color.RED)
            .setTimestamp(Instant.now())
            .build();
    }

    public static MessageEmbed info(String title, String description) {
        return new EmbedBuilder()
            .setTitle(title)
            .setDescription(description)
            .setColor(new Color(88, 101, 242)) // Discord blurple
            .setTimestamp(Instant.now())
            .build();
    }

    public static MessageEmbed imageEmbed(String title, String imageUrl) {
        return new EmbedBuilder()
            .setTitle(title)
            .setImage(imageUrl)
            .setColor(new Color(88, 101, 242))
            .setTimestamp(Instant.now())
            .build();
    }
}
