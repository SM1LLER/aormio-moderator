package by.pklabs.aormiobot.command;

import by.pklabs.aormiobot.Config;
import by.pklabs.aormiobot.database.Database;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UnmuteCommand extends SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(UnmuteCommand.class);
    private static final Config config = Config.getInstance();
    private static final String MUTE_ROLE_ID = config.get("muteRoleId");
    private static final String ADMIN_ROLE_ID = config.get("adminRoleId");
    private static final String MODERATOR_ROLE_ID = config.get("moderatorRoleId");
    private static final String LOG_CHANNEL_ID = config.get("logChannelId");

    public UnmuteCommand() {
        this.name = "unmute";
        this.help = "Размутить пользователя";
        this.defaultEnabled = false;
        this.enabledRoles = new String[]{ADMIN_ROLE_ID, MODERATOR_ROLE_ID};
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "Пользователь которого нужно размутить").setRequired(true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().setEphemeral(true).queue();

        User user = event.getOption("user").getAsUser();
        User moderator = event.getUser();
        Guild guild = event.getGuild();
        logger.debug("Searching member for guild " + guild.getName());
        Member memberToUnMute = guild.retrieveMember(user).complete();;
        Role muteRole = guild.getRoleById(MUTE_ROLE_ID);
        MessageChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
        EmbedBuilder embed = new EmbedBuilder();
        if(memberToUnMute.getRoles().contains(muteRole)){
            guild.removeRoleFromMember(memberToUnMute, muteRole).complete();
            Database.deleteMuted(memberToUnMute.getIdLong());
            embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                    .setTitle("Пользователю был снят мут")
                    .setDescription(user.getAsMention() + " размучен модератором")
                    .setFooter(moderator.getName(), moderator.getAvatarUrl());
            logChannel.sendMessageEmbeds(embed.build()).complete();
            event.getHook().sendMessage("Пользователю снят мут").setEphemeral(true).queue();
        } else {
            Database.deleteMuted(memberToUnMute.getIdLong());
            logChannel.sendMessageEmbeds(embed.build()).complete();
            event.getHook().sendMessage("Пользователь не был заглушён").setEphemeral(true).queue();
        }
    }
}
