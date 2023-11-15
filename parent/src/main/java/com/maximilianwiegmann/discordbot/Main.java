package com.maximilianwiegmann.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        new DiscordBot().init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DiscordBot.INSTANCE.getAutoChannelHandler().saveTempChannel();

            System.out.println("Disabling DiscordBot.");
        }));
    }

}
