package com.maximilianwiegmann.discordbot;

import com.maximilian.discordbot.IDiscordBot;
import com.maximilian.discordbot.JsonConfig;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import com.maximilian.discordbot.logger.Logger;
import com.maximilianwiegmann.discordbot.autochannel.AutoChannelHandler;
import com.maximilianwiegmann.discordbot.listener.ReadyListener;
import com.maximilianwiegmann.discordbot.listener.autochannel.AutoChannelListener;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.sql.SQLOutput;
import java.util.EnumSet;

@Getter
public class DiscordBot implements IDiscordBot {

    public static IDiscordBot INSTANCE;

    private final JsonConfig config = new JsonConfig(new File("config.json"));
    private long guildId;
    private JDA jda;

    private IAutoChannelHandler autoChannelHandler;

    @Override
    public void init() throws InterruptedException {
        new Logger();
        INSTANCE = this;

        System.out.println("Loading Discord Bot...");
        autoChannelHandler = new AutoChannelHandler();
        guildId = config.getOrDefaultSet("guildId", Long.class, 0L);

        JDABuilder builder = JDABuilder.createDefault(config.getOrDefaultSet("token", String.class, "yourtokenhere"));
        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS
        );

        builder.setActivity(Activity.watching("nach dem rechten"));
        jda = builder
                .enableIntents(intents)
                .setStatus(OnlineStatus.ONLINE)
                .setRawEventsEnabled(true)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(
                        new ReadyListener(),
                        new AutoChannelListener(autoChannelHandler)
                ).build();

        jda.setAutoReconnect(true);
        jda.awaitReady();

        System.out.println("Discord bot has been enabled.");
    }
}
