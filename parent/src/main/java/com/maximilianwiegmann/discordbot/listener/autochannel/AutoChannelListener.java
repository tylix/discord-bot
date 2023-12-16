package com.maximilianwiegmann.discordbot.listener.autochannel;

import com.maximilian.discordbot.autochannel.AbstractAutoChannelListener;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

public class AutoChannelListener extends AbstractAutoChannelListener {

    public AutoChannelListener(IAutoChannelHandler autoChannelHandler) {
        super(autoChannelHandler);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion joined = event.getChannelJoined();
        AudioChannelUnion left = event.getChannelLeft();

        Guild guild = event.getGuild();

        if (joined == null && left == null) return;

        if (left == null) {
            if (!getAutoChannelHandler().isAutoChannel(guild, joined.getIdLong())) return;

            getAutoChannelHandler().interactChannel(guild, event.getMember(), joined.asVoiceChannel(), null, IAutoChannelHandler.InteractType.CONNECT);
            return;
        }

        if (joined == null) {
            if (!getAutoChannelHandler().hasParent(guild, left.getIdLong())) return;

            getAutoChannelHandler().interactChannel(guild, event.getMember(), null, left.asVoiceChannel(), IAutoChannelHandler.InteractType.DISCONNECT);
            return;
        }

        getAutoChannelHandler().interactChannel(guild, event.getMember(), joined.asVoiceChannel(), left.asVoiceChannel(), IAutoChannelHandler.InteractType.SWITCH);

    }
}
