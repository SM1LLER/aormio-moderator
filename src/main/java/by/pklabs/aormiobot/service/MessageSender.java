package by.pklabs.aormiobot.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private TextChannel channel;
    public MessageSender(TextChannel channel){
        this.channel = channel;
    }

    public void sendMutedMessage(User user, User moderator, String reason, long time,
                                    String timeEnd, boolean isUpdate){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                .setTitle(isUpdate ? "Обновлено время мута" : "Пользователю выдан мут")
                .setDescription(user.getAsMention() + " получил мут на " + time + " " + timeEnd)
                .addField("Причина", reason, false)
                .setFooter(moderator.getName() + " | "
                                + LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss"))
                        , moderator.getAvatarUrl());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendUnmutedMessage(User user, User moderator){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                .setTitle("Пользователю был снят мут")
                .setDescription("С пользователя " + user.getAsMention() + " был снят мут модератором")
                .setFooter(moderator.getName() + " | "
                                + LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss"))
                            , moderator.getAvatarUrl());
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendAutoUnmutedMessage(User user){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                .setTitle("Автоматическое снятие мута")
                .setDescription("С пользователя " + user.getAsMention() + " был снят мут")
                .setFooter("AORMIO BOT" + " | "
                        + LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss")));
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendBanMessage(User user, User moderator, String reason){
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(user.getName() + "#" + user.getDiscriminator(), null, user.getAvatarUrl())
                .setTitle("Пользователь забанен")
                .setDescription(user.getAsMention() + " получил бан")
                .addField("Причина", reason, false)
                .setFooter(moderator.getName() + " | "
                                    + LocalDateTime.now().plusHours(3).format(DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss"))
                            , moderator.getAvatarUrl());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
