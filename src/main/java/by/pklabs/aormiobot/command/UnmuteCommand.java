package by.pklabs.aormiobot.command;

import by.pklabs.aormiobot.Config;
import by.pklabs.aormiobot.database.Database;
import by.pklabs.aormiobot.service.MessageSender;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UnmuteCommand extends SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(UnmuteCommand.class);
    private final Config config = Config.getInstance();
    private final String MUTE_ROLE_ID = config.get("muteRoleId");
    private final String ADMIN_ROLE_ID = config.get("adminRoleId");
    private final String MODERATOR_ROLE_ID = config.get("moderatorRoleId");
    private final String LOG_CHANNEL_ID = config.get("logChannelId");
    private MessageSender msgSender;
    private Database db = Database.getInstance();

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
        Guild guild = event.getGuild();
        if(this.msgSender == null){
            this.msgSender = new MessageSender(guild.getTextChannelById(LOG_CHANNEL_ID));
        }
        event.deferReply().setEphemeral(true).queue();

        User user = event.getOption("user").getAsUser();
        User moderator = event.getUser();
        logger.debug("Searching member for guild " + guild.getName());
        Member memberToUnmute = guild.retrieveMember(user).complete();;
        Role muteRole = guild.getRoleById(MUTE_ROLE_ID);
        if(memberToUnmute.getRoles().contains(muteRole)){
            guild.removeRoleFromMember(memberToUnmute, muteRole).complete();
            db.deleteMuted(memberToUnmute.getIdLong());
            msgSender.sendUnmutedMessage(user, moderator);
            event.getHook().sendMessage("Пользователю снят мут").setEphemeral(true).queue();
        } else {
            db.deleteMuted(memberToUnmute.getIdLong());
            event.getHook().sendMessage("Пользователь не был в муте").setEphemeral(true).queue();
        }
    }
}
