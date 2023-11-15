package com.maximilianwiegmann.discordbot.command;

import com.maximilian.discordbot.commands.AbstractCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class AutoChannelCommand extends AbstractCommand {

    public AutoChannelCommand(String name, String description) {
        super(name, description);
        addOption(
                new OptionData(OptionType.CHANNEL, "add", "Add Channel to auto channels"),
                new OptionData(OptionType.CHANNEL, "remove", "Remove Channel von auto channel")
        );
    }

    @Override
    public void onExecute(SlashCommandInteractionEvent event) {
        OptionMapping add = event.getOption("add");
        if(add == null) {

            return;
        }

        OptionMapping remove = event.getOption("remove");
        if(remove == null) {

            return;
        }

    }
}
