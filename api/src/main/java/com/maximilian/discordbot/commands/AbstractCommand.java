package com.maximilian.discordbot.commands;

import com.maximilian.discordbot.user.IDiscordUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public abstract class AbstractCommand {

    private final String name;
    private final String description;
    private long channel = -1;
    private final List<String> aliases = new ArrayList<>();
    private final List<OptionData> optionData = new ArrayList<>();
    private final List<SubcommandData> subcommandData = new ArrayList<>();


    public abstract void onExecute(SlashCommandInteractionEvent event);

    public void onButtonClick(IDiscordUser user, ButtonInteractionEvent clickEvent, String name) {

    }

    public void setAliases(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public void addOption(OptionData... option) {
        optionData.addAll(List.of(option));
    }
    public void addSubCommand(SubcommandData... option) {
        subcommandData.addAll(List.of(option));
    }

}
