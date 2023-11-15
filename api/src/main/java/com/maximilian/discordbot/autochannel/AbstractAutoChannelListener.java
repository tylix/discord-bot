package com.maximilian.discordbot.autochannel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@RequiredArgsConstructor
@Getter
public abstract class AbstractAutoChannelListener extends ListenerAdapter {

    private final IAutoChannelHandler autoChannelHandler;

    @Override
    public abstract void onGuildVoiceUpdate(GuildVoiceUpdateEvent event);
}
