package com.maximilianwiegmann.discordbot.command;

import com.maximilian.discordbot.commands.AbstractCommand;
import com.maximilian.discordbot.commands.handler.ICommandManager;
import com.maximilianwiegmann.discordbot.DiscordBot;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements ICommandManager {

    private final List<AbstractCommand> queue = new ArrayList<>();
    private final Map<String, AbstractCommand> commands = new HashMap<>();

    @Override
    public void loadCommands() {
        if (queue.isEmpty()) return;
        CommandListUpdateAction commands = DiscordBot.INSTANCE.getGuild().updateCommands();
        for (AbstractCommand abstractCommand : queue) {
            commands.addCommands(toData(abstractCommand));
        }
        commands.queue();
        System.out.println(MessageFormat.format("{0} command(s) were successfully loaded!", queue.size()));
    }

    private CommandData toData(AbstractCommand command) {
        commands.put(command.getName(), command);
        if (command.getAliases() != null)
            for (String alias : command.getAliases())
                commands.put(alias, command);

        CommandData commandData = new CommandDataImpl(command.getName(), command.getDescription());

        return commandData;
    }

    @Override
    public void addCommand(AbstractCommand command) {
        queue.add(command);
    }

    @Override
    public AbstractCommand getCommandByName(String name) {
        return commands.getOrDefault(name, null);
    }

    /*@Override
    public AbstractCommand getCommandByButtonId(String id) {
        return commands.values().stream().filter(abstractCommand -> abstractCommand.getBtnId().equals(id)).findFirst().orElse(null);
    }*/
}
