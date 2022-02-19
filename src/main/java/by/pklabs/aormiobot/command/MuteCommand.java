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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class MuteCommand extends SlashCommand {
    private static final Logger logger = LoggerFactory.getLogger(MuteCommand.class);
    private final Config config = Config.getInstance();
    private final String MUTE_ROLE_ID = config.get("muteRoleId");
    private final String ADMIN_ROLE_ID = config.get("adminRoleId");
    private final String MODERATOR_ROLE_ID = config.get("moderatorRoleId");
    private final String LOG_CHANNEL_ID = config.get("logChannelId");
    private MessageSender msgSender;
    private Database db = Database.getInstance();

    public MuteCommand() {
        this.name = "mute";
        this.help = "Замутить пользователя";
        this.defaultEnabled = false;
        this.enabledRoles = new String[]{ADMIN_ROLE_ID, MODERATOR_ROLE_ID};
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "Пользователь которого нужно замутить").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "reason", "Причина мута").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "timeunit", "Замутить на дни/часы/минуты").setRequired(true)
                            .addChoice("minutes", "minutes")
                            .addChoice("hours", "hours")
                            .addChoice("days","days"));
        options.add(new OptionData(OptionType.INTEGER, "time", "На сколько").setRequired(true));
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
        String reason = event.getOption("reason").getAsString();
        String timeEnd = "";
        long time = event.getOption("time").getAsLong();
        ChronoUnit timeUnit = ChronoUnit.valueOf(event.getOption("timeunit").getAsString().toUpperCase());
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

        Member memberToMute = guild.retrieveMember(user).complete();;
        Role muteRole = guild.getRoleById(MUTE_ROLE_ID);
        LocalDateTime muteTime = LocalDateTime.now().plus(time, timeUnit).plusHours(3);
        if(!memberToMute.getRoles().contains(muteRole)){
            guild.addRoleToMember(memberToMute, muteRole).complete();
            db.insertMuted(memberToMute.getIdLong(), muteTime);
            msgSender.sendMutedMessage(user, moderator, reason, time, timeEnd, muteTime, false);
            event.getHook().sendMessage("Пользователь успешно заглушён").setEphemeral(true).queue();
        } else {
            db.insertMuted(memberToMute.getIdLong(), muteTime);
            msgSender.sendMutedMessage(user, moderator, reason, time, timeEnd, muteTime, true);
            event.getHook().sendMessage("Обновлено время заглушения для пользователя").setEphemeral(true).queue();
        }

    }
}
