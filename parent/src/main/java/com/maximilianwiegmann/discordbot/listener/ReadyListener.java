package com.maximilianwiegmann.discordbot.listener;

import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        DiscordBot.INSTANCE.getAutoChannelHandler().loadAutoChannel();

        for (Guild guild : DiscordBot.INSTANCE.getJda().getGuilds()) {
            if (guild == null) continue;
            DiscordBot.INSTANCE.getDataHandler().addGuild(guild);

            System.out.println("Loading users on" + guild.getName() + "...");

            List<Member> members = new ArrayList<>();

            guild.loadMembers(members::add).onSuccess(unused -> {
                System.out.println(MessageFormat.format("{0} user(s) has been successfully loaded.", members.size()));

                System.out.println(guild.getName() + " " + guild.getIconUrl() + " " + members.size() + " " + guild.getTimeCreated().toInstant().toEpochMilli());
            });

            DiscordBot.INSTANCE.getDataHandler().loadChannels(guild);

        }

        DiscordBot.INSTANCE.getCommandManager().loadCommands();
        DiscordBot.INSTANCE.startPing();

    }
}
