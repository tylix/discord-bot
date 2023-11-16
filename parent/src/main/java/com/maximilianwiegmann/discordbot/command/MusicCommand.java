package com.maximilianwiegmann.discordbot.command;

import com.maximilian.discordbot.commands.AbstractCommand;
import com.maximilian.discordbot.user.IDiscordUser;
import com.maximilianwiegmann.discordbot.DiscordBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class MusicCommand extends AbstractCommand {

    public MusicCommand() {
        super("music", "Play music through the bot.");
        addOption(
                new OptionData(OptionType.STRING, "track", "URL or search", false, false),
                new OptionData(OptionType.BOOLEAN, "skip", "Skip the current song"),
                new OptionData(OptionType.BOOLEAN, "stop", "Stop the current song")
        );
    }

    @Override
    public void onExecute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) return;

        if (!member.getVoiceState().inAudioChannel()) {
            event.getChannel().sendMessage("You need to be in channel for this command to work.").queue();
            return;
        }

        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            AudioManager audioManager = event.getGuild().getAudioManager();
            VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);

        }

        OptionMapping skipMapping = event.getOption("skip");
        if (skipMapping != null) {
            DiscordBot.INSTANCE.getMusicManager().skip(event.getChannel().asTextChannel());
            return;
        }

        OptionMapping stopMapping = event.getOption("stop");
        if (stopMapping != null) {
            DiscordBot.INSTANCE.getMusicManager().stop(event.getGuild());
            return;
        }

        OptionMapping trackMapping = event.getOption("track");
        if (trackMapping == null) return;

        String track = trackMapping.getAsString();
        if (!isUrl(track)) {
            track = "ytsearch:" + track + " audio";
        }

        DiscordBot.INSTANCE.getMusicManager().loadAndPlay(event.getChannel().asTextChannel(), track);

        event.deferReply().queue();
    }

    private boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
