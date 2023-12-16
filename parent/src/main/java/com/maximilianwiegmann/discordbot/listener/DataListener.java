package com.maximilianwiegmann.discordbot.listener;

import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateOnlineStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DataListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        DiscordBot.INSTANCE.getAutoChannelHandler().loadAutoChannel(guild);
        DiscordBot.INSTANCE.getDataHandler().addGuild(guild);
        DiscordBot.INSTANCE.getDataHandler().loadUsers(guild);
        DiscordBot.INSTANCE.getDataHandler().loadChannels(guild);
    }

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

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        DiscordBot.INSTANCE.getDataHandler().addMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        DiscordBot.INSTANCE.getDataHandler().removeMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onGenericGuildMemberUpdate(GenericGuildMemberUpdateEvent event) {
        DiscordBot.INSTANCE.getDataHandler().updateMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onGenericGuildMember(GenericGuildMemberEvent event) {
        DiscordBot.INSTANCE.getDataHandler().updateMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onUserUpdateActivities(UserUpdateActivitiesEvent event) {
        DiscordBot.INSTANCE.getDataHandler().updateMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onUserUpdateOnlineStatus(UserUpdateOnlineStatusEvent event) {
        DiscordBot.INSTANCE.getDataHandler().updateMember(event.getGuild(), event.getMember());
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        Guild guild = event.getGuild();

        DiscordBot.INSTANCE.getDataHandler().updateMember(guild, event.getMember());

        AudioChannelUnion joined = event.getChannelJoined();
        AudioChannelUnion left = event.getChannelLeft();

        if (joined == null && left == null) return;

        if (left == null) {
            DiscordBot.INSTANCE.getDataHandler().changeChannel(joined, guild);
            return;
        }

        if (joined == null) {
            DiscordBot.INSTANCE.getDataHandler().changeChannel(left, guild);
            return;
        }
        DiscordBot.INSTANCE.getDataHandler().changeChannel(left, guild);
        DiscordBot.INSTANCE.getDataHandler().changeChannel(joined, guild);
    }

    @Override
    public void onGenericGuildVoice(GenericGuildVoiceEvent event) {
        DiscordBot.INSTANCE.getDataHandler().updateMember(event.getGuild(), event.getMember());
    }
}
