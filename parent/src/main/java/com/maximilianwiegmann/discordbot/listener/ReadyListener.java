package com.maximilianwiegmann.discordbot.listener;

import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Guild guild = DiscordBot.INSTANCE.getJda().getGuildById(DiscordBot.INSTANCE.getGuildId());

        if(guild == null) return;

        System.out.println("Loading users...");

        guild.loadMembers(member -> {

        }).onSuccess(unused -> {
            System.out.println(MessageFormat.format("{0} user(s) has been successfully loaded.", 10));

            DiscordBot.INSTANCE.getAutoChannelHandler().loadAutoChannel();
        });
    }
}
