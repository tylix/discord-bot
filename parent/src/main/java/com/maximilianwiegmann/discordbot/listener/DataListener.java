package com.maximilianwiegmann.discordbot.listener;

import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DataListener extends ListenerAdapter {

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        DiscordBot.INSTANCE.getDataHandler().addChannel(event.getChannel(), event.getGuild());
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        DiscordBot.INSTANCE.getDataHandler().removeChannel(event.getChannel(), event.getGuild());
    }

    @Override
    public void onGenericChannelUpdate(GenericChannelUpdateEvent<?> event) {
        DiscordBot.INSTANCE.getDataHandler().changeChannel(event.getChannel(), event.getGuild());
    }
}
