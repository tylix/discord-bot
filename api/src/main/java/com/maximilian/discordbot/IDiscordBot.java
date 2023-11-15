package com.maximilian.discordbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public interface IDiscordBot {

    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    void init() throws InterruptedException;

    JsonConfig getConfig();

    JDA getJda();

    long getGuildId();

    IAutoChannelHandler getAutoChannelHandler();

    default Guild getGuild() {
        return getJda().getGuildById(getGuildId());
    }

    default String intToRoman(int num) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

}
