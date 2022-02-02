package by.pklabs.aormiobot;
import by.pklabs.aormiobot.command.MuteCommand;
import by.pklabs.aormiobot.command.UnmuteCommand;
import by.pklabs.aormiobot.database.Database;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static ScheduledExecutorService threadpool;

    public static void main(String[] args) {
        Database.init();
        Config config = Config.getInstance();
        String token = config.get("token");
        threadpool = Executors.newScheduledThreadPool(10, r -> new Thread(r, "vortex"));
        try {
            CommandClient commandClient = buildCommandClient(config);
            JDA api = JDABuilder.createDefault(token)
                    .addEventListeners(commandClient)
                    .setStatus(OnlineStatus.ONLINE)
                    .build();
            api.awaitReady();
            api.updateCommands();
            threadpool.scheduleWithFixedDelay(() ->
                    Database.checkUnmutes(api.getGuildById(config.get("guildId"))), 0, 45, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static CommandClient buildCommandClient(Config config) {
        CommandClientBuilder builder = new CommandClientBuilder();
        logger.debug("guildId " + config.get("guildId"));
        builder.forceGuildOnly(config.get("guildId"))
                .setOwnerId(config.get("ownerId"))
                .addSlashCommand(new MuteCommand())
                .addSlashCommand(new UnmuteCommand());
        return builder.build();
    }
}
