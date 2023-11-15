package com.maximilianwiegmann.discordbot.listener.autochannel;

import com.maximilian.discordbot.autochannel.AbstractAutoChannelListener;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
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

        if (joined == null && left == null) return;

        if (left == null) {
            if (!getAutoChannelHandler().isAutoChannel(joined.getIdLong())) return;

            getAutoChannelHandler().interactChannel(event.getMember(), joined.asVoiceChannel(), null, IAutoChannelHandler.InteractType.CONNECT);
            return;
        }

        if (joined == null) {
            if (!getAutoChannelHandler().hasParent(left.getIdLong())) return;

            getAutoChannelHandler().interactChannel(event.getMember(), null, left.asVoiceChannel(), IAutoChannelHandler.InteractType.DISCONNECT);
            return;
        }

        getAutoChannelHandler().interactChannel(event.getMember(), joined.asVoiceChannel(), left.asVoiceChannel(), IAutoChannelHandler.InteractType.SWITCH);

    }
}
