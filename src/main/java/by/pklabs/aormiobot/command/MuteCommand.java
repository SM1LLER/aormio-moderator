package by.pklabs.aormiobot.command;

import by.pklabs.aormiobot.Config;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends SlashCommand {
    private static final String MUTE_ROLE_ID = Config.getConfigInstance().getConfig().get("muteRoleId");
    private static final String ADMIN_ROLE_ID = Config.getConfigInstance().getConfig().get("adminRoleId");
    private static final String MODERATOR_ROLE_ID = Config.getConfigInstance().getConfig().get("moderatorRoleId");
    private static final String LOG_CHANNEL_ID = Config.getConfigInstance().getConfig().get("logChannelId");

    public MuteCommand() {
        this.name = "mute";
        this.help = "Замутить пользователя";
        this.defaultEnabled = false;
        this.enabledRoles = new String[]{ADMIN_ROLE_ID, MODERATOR_ROLE_ID};
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "Пользователь которого нужно замутить").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "reason", "Причина мута").setRequired(true));
        options.add(new OptionData(OptionType.STRING, "timeUnit", "Замутить на дни/часы/минуты").setRequired(true)
                                                                                                                .addChoice("days","days")
                                                                                                                .addChoice("hours", "hours")
                                                                                                                .addChoice("mins", "mins"));
        options.add(new OptionData(OptionType.NUMBER, "time", "На сколько").setRequired(true));
        this.options = options;
    }
    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        User user = null;
        String reason = null;
        TimeUnit timeUnit = null;
        long time = 0;
        for (OptionMapping opMap : event.getOptions()){
            switch(opMap.getName()){
                case "user":
                    user = opMap.getAsUser();
                    break;
                case "reason":
                    reason = opMap.getAsString();
                    break;
                case "timeUnit":
                    timeUnit = TimeUnit.valueOf(opMap.getAsString());
                    break;
                case "time":
                    time = opMap.getAsLong();
                    break;
            }
        }
        Member memberToMute = event.getGuild().getMemberById(user.getId());
        Role muteRole = event.getGuild().getRoleById(MUTE_ROLE_ID);
        if(!memberToMute.getRoles().contains(muteRole)){
            MessageChannel logChannel = event.getGuild().getTextChannelById(LOG_CHANNEL_ID);
        }
    }
}
