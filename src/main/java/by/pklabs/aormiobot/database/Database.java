package by.pklabs.aormiobot.database;

import by.pklabs.aormiobot.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static final Config config = Config.getInstance();
    private static HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource ds;
    private static Connection con;

    private Database(){}
    public static void init(){
        hikariConfig.setJdbcUrl(config.get("jdbcUrl"));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(config.get("databaseUser"));
        hikariConfig.setPassword(config.get("databasePassword"));
        hikariConfig.addDataSourceProperty( "cachePrepStmts" , "true" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        hikariConfig.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource(hikariConfig);
        try {
            con = ds.getConnection();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static void selectAll(Consumer<ResultSet> consumer){
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM muted_users");
            ResultSet rs = ps.executeQuery();
            consumer.accept(rs);
        } catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

    public static void insertMuted(long userId, LocalDateTime unmuteTime){
        try{
            logger.debug("Inserting in database");
            PreparedStatement ps = con.prepareStatement("INSERT INTO muted_users(user, unmute_time)"
                    + "VALUES(?, ?)");
            ps.setLong(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(unmuteTime));
            ps.executeUpdate();
            logger.debug("Inserted");
        }catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

    public static void deleteMuted(long userId){
        logger.debug("Deleting from database");
        try{
            PreparedStatement ps = con.prepareStatement("DELETE FROM muted_users WHERE user = ?");
            ps.setLong(1, userId);
            ps.executeUpdate();
            logger.debug("Deleted");
        }catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

    public static void checkUnmutes(Guild guild){
        logger.info("Checking unmutes");
        try{
            PreparedStatement ps = con.prepareStatement("SELECT * FROM muted_users", ResultSet.TYPE_SCROLL_INSENSITIVE
                                                        , ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = ps.executeQuery();
            logger.debug(rs.toString());
            LocalDateTime currTime = LocalDateTime.now();
            while (rs.next()){
                LocalDateTime unmuteDate = rs.getTimestamp("unmute_time").toLocalDateTime();
                logger.debug("Current time " + currTime.toString()
                        + " Unmuted time" + unmuteDate.toString());
                if(currTime.isAfter(unmuteDate)){
                    Role muteRole = guild.getRoleById(config.get("muteRoleId"));
                    long userId = rs.getLong("user");
                    Member memberToMute = guild.retrieveMemberById(userId).complete();
                    User user = memberToMute.getUser();
                    guild.removeRoleFromMember(memberToMute, muteRole).complete();
                    rs.deleteRow();
                    MessageChannel logChannel = guild.getTextChannelById(config.get("logChannelId"));
                    EmbedBuilder embed = new EmbedBuilder();
                    logger.info("Prepare embed");
                    embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                            .setTitle("Автоматическое снятие мута")
                            .setDescription(user.getAsMention() + "был размучен")
                            .setFooter("AORMIO BOT");
                    logger.info("Sending message");
                    logChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        } catch (SQLException e){
            logger.error(e.getMessage());
        }
    }

}
