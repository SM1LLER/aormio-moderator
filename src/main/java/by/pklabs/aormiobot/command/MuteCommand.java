package by.pklabs.aormiobot.command;

import by.pklabs.aormiobot.Config;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(MuteCommand.class);
    private static final Config config = Config.getInstance();
    private static final String MUTE_ROLE_ID = config.get("muteRoleId");
    private static final String ADMIN_ROLE_ID = config.get("adminRoleId");
    private static final String MODERATOR_ROLE_ID = config.get("moderatorRoleId");
    private static final String LOG_CHANNEL_ID = config.get("logChannelId");

    public MuteCommand() {
        this.name = "mute";
        this.help = "Замутить пользователя";
        this.defaultEnabled = false;
        this.enabledRoles = new String[]{ADMIN_ROLE_ID, MODERATOR_ROLE_ID};
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "Пользователь которого нужно замутить").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "reason", "Причина мута").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "timeunit", "Замутить на дни/часы/минуты").setRequired(true)
                                                                                                                .addChoice("days","days")
                                                                                                                .addChoice("hours", "hours")
                                                                                                                .addChoice("minutes", "minutes"));
        options.add(new OptionData(OptionType.INTEGER, "time", "На сколько").setRequired(true));
        this.options = options;
    }
    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();
        User user = null;
        User moderator = event.getUser();
        String reason = null;
        TimeUnit timeUnit = null;
        String timeEnd = "";
        long time = 0;
        for (OptionMapping opMap : event.getOptions()){
            switch(opMap.getName()){
                case "user":
                    user = opMap.getAsUser();
                    break;
                case "reason":
                    reason = opMap.getAsString();
                    break;
                case "timeunit":
                    timeUnit = TimeUnit.valueOf(opMap.getAsString().toUpperCase());
                    switch(timeUnit){
                        case DAYS:
                            timeEnd = "д.";
                            break;
                        case HOURS:
                            timeEnd = "ч.";
                            break;
                        case MINUTES:
                            timeEnd = "мин.";
                            break;
                    }
                    break;
                case "time":
                    time = opMap.getAsLong();
                    break;
            }
        }
        Guild guild = event.getGuild();
        logger.debug("Searching member for guild " + guild.getName());

        Member memberToMute = guild.retrieveMember(user).complete();;

        Role muteRole = guild.getRoleById(MUTE_ROLE_ID);
        if(!memberToMute.getRoles().contains(muteRole)){
            guild.addRoleToMember(memberToMute, muteRole).complete();
            MessageChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                    .setTitle("Пользователю выдан мут")
                    .setDescription(user.getAsMention() + " получил мут на " + time + " " + timeEnd)
                    .setFooter(moderator.getName(), moderator.getAvatarUrl());
            logChannel.sendMessageEmbeds(embed.build()).complete();
        }
        event.getHook().sendMessage("Мут выдан").setEphemeral(true).queue();
    }
}
