package by.pklabs.aormiobot;
import by.pklabs.aormiobot.command.MuteCommand;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) {
        Config configParser = Config.getConfigInstance();
        Map<String, String> config = configParser.getConfig();
        String token = config.get("token");
        try {
            CommandClient commandClient = buildCommandClient(config);
            JDA api = JDABuilder.createDefault(token)
                    .addEventListeners(commandClient)
                    .setStatus(OnlineStatus.ONLINE)
                    .build();
            api.awaitReady();
            api.updateCommands();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static CommandClient buildCommandClient(Map<String, String> config) {
        CommandClientBuilder builder = new CommandClientBuilder();
        logger.debug("guildId " + config.get("guildId"));
        builder.forceGuildOnly(config.get("guildId"))
                .setOwnerId(config.get("ownerId"))
                .addSlashCommand(new MuteCommand());
        return builder.build();
    }
}
