package by.pklabs.aormiobot.command;

import by.pklabs.aormiobot.Config;
import by.pklabs.aormiobot.service.MessageSender;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BanCommand extends SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(UnmuteCommand.class);
    private final Config config = Config.getInstance();
    private final String MUTE_ROLE_ID = config.get("muteRoleId");
    private final String ADMIN_ROLE_ID = config.get("adminRoleId");
    private final String MODERATOR_ROLE_ID = config.get("moderatorRoleId");
    private final String LOG_CHANNEL_ID = config.get("logChannelId");
    private MessageSender msgSender;

    public BanCommand(){
        this.name = "ban";
        this.help = "Забанить пользователя";
        this.defaultEnabled = false;
        this.enabledRoles = new String[]{ADMIN_ROLE_ID, MODERATOR_ROLE_ID};
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "Пользователь которого нужно забанить").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "reason", "Причина бана").setRequired(true));
        options.add(new OptionData(OptionType.INTEGER, "days", "Удалить сообщения за кол-во дней"));
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
        int delDays = 0;
        if(event.hasOption("days")){
            delDays = (int) event.getOption("days").getAsLong();
        }
        String reason = event.getOption("reason").getAsString();
        Member memberToBan = guild.retrieveMember(user).complete();
        if(memberToBan != null){
            guild.ban(memberToBan, delDays).complete();
            msgSender.sendBanMessage(user, moderator, reason);
            event.getHook().sendMessage("Пользователь успешно забанен").setEphemeral(true).queue();
        } else {
            event.getHook().sendMessage("Данного пользователя нет на сервере").setEphemeral(true).queue();
        }
    }
}
