package com.maximilian.discordbot.commands.handler;

import com.maximilian.discordbot.commands.AbstractCommand;

public interface ICommandManager {

    void loadCommands();

    void addCommand(AbstractCommand command);

    AbstractCommand getCommandByName(String name);

    /*AbstractCommand getCommandByButtonId(String id);*/
}
