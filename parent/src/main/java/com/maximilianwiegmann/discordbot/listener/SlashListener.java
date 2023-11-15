package com.maximilianwiegmann.discordbot.listener;

import com.maximilian.discordbot.commands.AbstractCommand;
import com.maximilian.discordbot.user.IDiscordUser;
import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class SlashListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        AbstractCommand command = DiscordBot.INSTANCE.getCommandManager().getCommandByName(event.getName());
        if (command == null) return;
        if (command.getChannel() != -1 && command.getChannel() != event.getChannel().getIdLong()) {
            event.replyEmbeds(new EmbedBuilder().setColor(Color.red).setDescription("This command may only be executed in channel " + DiscordBot.INSTANCE.getGuild().getTextChannelById(command.getChannel()).getAsMention()).build()).setEphemeral(true).queue();
            return;
        }
        command.onExecute(event);
    }

}