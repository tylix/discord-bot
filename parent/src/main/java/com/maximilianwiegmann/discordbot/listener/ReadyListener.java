package com.maximilianwiegmann.discordbot.listener;

import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {

        for (Guild guild : DiscordBot.INSTANCE.getJda().getGuilds()) {
            if (guild == null) continue;
            DiscordBot.INSTANCE.getAutoChannelHandler().loadAutoChannel(guild);
            DiscordBot.INSTANCE.getDataHandler().addGuild(guild);
            DiscordBot.INSTANCE.getDataHandler().loadUsers(guild);
            DiscordBot.INSTANCE.getDataHandler().loadChannels(guild);
        }

        DiscordBot.INSTANCE.getCommandManager().loadCommands();
        DiscordBot.INSTANCE.startPing();

    }
}
