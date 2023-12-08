package com.maximilianwiegmann.discordbot;

import com.google.gson.Gson;
import com.maximilian.discordbot.IDiscordBot;
import com.maximilian.discordbot.JsonConfig;
import com.maximilian.discordbot.Request;
import com.maximilian.discordbot.SseClient;
import com.maximilian.discordbot.autochannel.IAutoChannelHandler;
import com.maximilian.discordbot.commands.handler.ICommandManager;
import com.maximilian.discordbot.data.GuildChannelData;
import com.maximilian.discordbot.data.GuildData;
import com.maximilian.discordbot.data.IDataHandler;
import com.maximilian.discordbot.data.music.MusicPlayerData;
import com.maximilian.discordbot.data.music.MusicTrackData;
import com.maximilian.discordbot.logger.Logger;
import com.maximilian.discordbot.music.IMusicManager;
import com.maximilianwiegmann.discordbot.autochannel.AutoChannelHandler;
import com.maximilianwiegmann.discordbot.command.CommandManager;
import com.maximilianwiegmann.discordbot.command.MusicCommand;
import com.maximilianwiegmann.discordbot.data.DataHandler;
import com.maximilianwiegmann.discordbot.listener.DataListener;
import com.maximilianwiegmann.discordbot.listener.ReadyListener;
import com.maximilianwiegmann.discordbot.listener.SlashListener;
import com.maximilianwiegmann.discordbot.listener.autochannel.AutoChannelListener;
import com.maximilianwiegmann.discordbot.music.MusicManager;
import com.maximilianwiegmann.discordbot.security.JwtService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.IPositionableChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class DiscordBot implements IDiscordBot {

    public static IDiscordBot INSTANCE;

    private final JsonConfig config = new JsonConfig(new File("config.json"));
    private final String url = "https://api.maximilianwiegmann.com/service";
    //private final String url = "http://localhost:8070/service";
    private long guildId;
    private JDA jda;

    private IAutoChannelHandler autoChannelHandler;
    private ICommandManager commandManager;
    private IMusicManager musicManager;
    private IDataHandler dataHandler;

    private JwtService jwtService;
    private String token;
    private String serviceId;

    @SneakyThrows
    @Override
    public void init() throws InterruptedException {
        new Logger();
        INSTANCE = this;

        jwtService = new JwtService();

        serviceId = config.getOrDefaultSet("serviceId", String.class, "yourserviceidhere");
        connect();

        System.out.println("Loading Discord Bot...");
        autoChannelHandler = new AutoChannelHandler();
        dataHandler = new DataHandler();
        commandManager = new CommandManager();
        commandManager.addCommand(new MusicCommand());
        musicManager = new MusicManager();
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

        builder.setActivity(Activity.watching("\uD83D\uDC40"));
        jda = builder
                .enableIntents(intents)
                .setStatus(OnlineStatus.ONLINE)
                .setRawEventsEnabled(true)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(
                        new ReadyListener(),
                        new AutoChannelListener(autoChannelHandler),
                        new SlashListener(),
                        new DataListener()
                ).build();

        jda.setAutoReconnect(true);
        jda.awaitReady();

        System.out.println("Discord bot has been enabled.");
    }

    @SneakyThrows
    public void connect() {
        token = jwtService.generateServiceToken(serviceId);
        Request.builder().url(url).cookies(getHeader()).build().sendRequest();

        SseClient client = new SseClient("https://api.maximilianwiegmann.com/test/655ba1e73657f658a5d1f2d2/client", Collections.singletonMap("jwt", token), response -> {
            //SseClient client = new SseClient("http://localhost:8070/test/655ba1e73657f658a5d1f2d2/client", Collections.singletonMap("jwt", token), response -> {

            Guild guild = jda.getGuildById((String) response.getData().get("guildId"));
            if (guild == null) return;

            switch (response.getTarget()) {
                case MUSIC -> {
                    switch (response.getAction()) {
                        case TOGGLE_PAUSE -> {
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            AudioTrack playingTrack = audioPlayer.getPlayingTrack();
                            if (playingTrack == null) return;
                            audioPlayer.setPaused(!audioPlayer.isPaused());
                        }
                        case ADD_TO_QUEUE -> {
                            String track = (String) response.getData().get("track");
                            if (!isUrl(track)) {
                                track = "ytsearch:" + track + " audio";
                            }

                            String voiceChannel = (String) response.getData().get("channelId");
                            String userId = (String) response.getData().get("userId");
                            if (userId != null) {
                                Member member = guild.getMemberById(userId);
                                if (member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel())
                                    voiceChannel = member.getVoiceState().getChannel().getId();
                            }

                            DiscordBot.INSTANCE.getMusicManager().loadAndPlay(null, guild, track, voiceChannel == null ? guild.getVoiceChannels().get(0).getIdLong() : Long.parseLong(voiceChannel));
                        }
                        case CHANGE_CHANNEL -> {
                            String newChannel = (String) response.getData().get("channelId");
                            if (guild.getVoiceChannelById(newChannel) == null) return;
                            guild.getAudioManager().openAudioConnection(guild.getVoiceChannelById(newChannel));
                        }
                        case CLEAR_QUEUE -> {
                            musicManager.getMusicManager(guild).getScheduler().getQueue().clear();
                        }
                        case CHANGE_POSITION -> {
                            long position = Long.parseLong((String) response.getData().get("position"));
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            AudioTrack playingTrack = audioPlayer.getPlayingTrack();
                            if (playingTrack == null) return;
                            playingTrack.setPosition(position);
                        }
                        case SKIP -> {
                            musicManager.getMusicManager(guild).getScheduler().nextTrack();
                        }
                        case REMOVE_FROM_QUEUE -> {
                            String track = (String) response.getData().get("identifier");
                            if (track == null) return;
                            musicManager.getMusicManager(guild).getScheduler().getQueue().removeIf(queueItem -> queueItem.getIdentifier().equals(track));
                        }
                        case CHANGE_VOLUME -> {
                            int volume = (int) response.getData().get("volume");
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            if (audioPlayer == null) return;
                            audioPlayer.setVolume(volume);
                        }
                        case RESUME -> {
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            AudioTrack playingTrack = audioPlayer.getPlayingTrack();
                            if (playingTrack == null) return;
                            float percentage = (playingTrack.getPosition() * 100F) / playingTrack.getInfo().length;
                            if (percentage <= 20) {
                                List<AudioTrack> history = musicManager.getMusicManager(guild).getScheduler().getHistory();
                                if (history == null || history.isEmpty()) {
                                    playingTrack.setPosition(0);
                                    return;
                                }
                                AudioTrack lastTrack = history.get(history.indexOf(playingTrack) - 1);
                                if (lastTrack == null) return;
                                audioPlayer.startTrack(lastTrack, false);
                            } else
                                playingTrack.setPosition(0);
                        }
                        case FORCE_QUEUE_ITEM -> {
                            String track = (String) response.getData().get("identifier");
                            if (track == null) return;
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            if (audioPlayer == null) return;

                            AudioTrack audioTrack = musicManager.getMusicManager(guild).getScheduler().getQueue().stream().filter(t -> t.getIdentifier().equals(track)).findFirst().orElse(null);
                            if (audioTrack == null) return;
                            musicManager.getMusicManager(guild).getScheduler().getQueue().remove(audioTrack);
                            audioPlayer.startTrack(audioTrack, false);
                        }
                        case STOP -> {
                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            if (audioPlayer == null) return;
                            audioPlayer.stopTrack();
                            audioPlayer.destroy();
                        }
                    }
                }
                case AUTO_CHANNEL -> {

                }
            }
        });
        client.connect();
    }

    @SneakyThrows
    public void ping() {
        if (jwtService.isTokenExpired(token))
            token = jwtService.generateServiceToken(serviceId);

        JSONObject data = new JSONObject()
                .put("timestamp", System.currentTimeMillis())
                .put("totalUser", jda.getGuilds()
                        .stream()
                        .mapToInt(guild -> guild.getMembers().size()).sum())
                .put("guilds", new JSONArray(dataHandler.getGuilds()
                        .stream()
                        .map(guild -> {
                            List<GuildChannelData> channel = dataHandler.getChannel(guild);

                            AudioPlayer audioPlayer = musicManager.getMusicManager(guild).getScheduler().getAudioPlayer();
                            AudioTrack playingTrack = audioPlayer.getPlayingTrack();

                            MusicPlayerData musicPlayerData = MusicPlayerData.builder()
                                    .pause(audioPlayer.isPaused())
                                    .volume(audioPlayer.getVolume())
                                    .currentTrack(playingTrack == null ? null : MusicTrackData.fromAudioTrackInfo(playingTrack.getInfo()))
                                    .currentPosition(playingTrack == null ? 0 : playingTrack.getPosition())
                                    .currentChannel(guild.getAudioManager().isConnected() ? guild.getAudioManager().getConnectedChannel().getId() : null)
                                    .queue(musicManager.getMusicManager(guild).getScheduler().getQueue().stream().map(track -> MusicTrackData.fromAudioTrackInfo(track.getInfo())).collect(Collectors.toList()))
                                    .build();

                            return GuildData.builder()
                                    .id(guild.getId())
                                    .name(guild.getName())
                                    .iconUrl(guild.getIconUrl())
                                    .channel(channel)
                                    .memberAmount(guild.getMemberCount())
                                    .created(guild.getTimeCreated().toInstant().toEpochMilli())
                                    .musicPlayerData(musicPlayerData)
                                    .build();
                        })
                        .collect(Collectors.toList())));

        Request.builder().url(url + "/ping")
                .cookies(getHeader())
                .body(new JSONObject()
                        .put("data", data)
                        .toString())
                .contentType("application/json").build().sendRequest();
    }

    private Map<String, String> getHeader() {
        return new HashMap<>() {
            {
                put("jwt", token);
            }
        };
    }

    @SneakyThrows
    public void disable() {
        Request.builder().method(Request.RequestMethod.DELETE).url(url)
                .cookies(getHeader()).build().sendRequest();
    }

    @Override
    public void startPing() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        }, 0, 1000);
    }

    @Override
    public boolean isUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

}
