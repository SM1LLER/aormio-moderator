package by.pklabs.aormiobot.service;

import by.pklabs.aormiobot.Config;
import by.pklabs.aormiobot.database.Database;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UnmuteTimeChecker {
    private static final Logger logger = LoggerFactory.getLogger(UnmuteTimeChecker.class);
    private static final Config config = Config.getInstance();
    private static final String MUTE_ROLE_ID = config.get("muteRoleId");
    private static final String LOG_CHANNEL_ID = config.get("logChannelId");
    private Database db = Database.getInstance();
    private Role muteRole;
    private MessageSender msgSender;
    private Guild guild;
    public UnmuteTimeChecker(Guild guild){
        this.guild = guild;
        this.muteRole = guild.getRoleById(MUTE_ROLE_ID);
        this.msgSender = new MessageSender(guild.getTextChannelById(LOG_CHANNEL_ID));
    }

    public void checkUnmutes(){
        try{
            ResultSet rs = db.getAll();
            LocalDateTime currTime = LocalDateTime.now().plusHours(3);
            while (rs.next()) {
                LocalDateTime unmuteDate = rs.getTimestamp("unmute_time").toLocalDateTime();
                if(currTime.isAfter(unmuteDate)){
                    long userId = rs.getLong("user");
                    guild.retrieveMemberById(userId).queue(memberToUnmute -> {
                        guild.removeRoleFromMember(memberToUnmute, muteRole).queue();
                        db.deleteMuted(userId);
                        msgSender.sendAutoUnmutedMessage(memberToUnmute.getUser());
                    }, error -> {
                        db.deleteMuted(userId);
                        logger.info("No such member in guild");
                    });
                }
            }
        } catch (SQLException e){
            logger.error(e.getMessage());
        }
    }
}
