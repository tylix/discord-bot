package com.maximilianwiegmann.discordbot.user;

import com.maximilian.discordbot.user.IDiscordUser;
import net.dv8tion.jda.api.entities.User;

import java.lang.reflect.Member;

public class DiscordUser implements IDiscordUser {
    @Override
    public boolean isBot() {
        return false;
    }

    @Override
    public void load(long id) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public String getIdString() {
        return null;
    }

    @Override
    public void setNickname(String name) {

    }

    @Override
    public Member getMember() {
        return null;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getNickname() {
        return null;
    }
}
