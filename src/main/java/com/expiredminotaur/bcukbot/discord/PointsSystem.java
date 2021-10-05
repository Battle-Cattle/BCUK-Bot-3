package com.expiredminotaur.bcukbot.discord;

import com.expiredminotaur.bcukbot.discord.command.DiscordCommandEvent;
import com.expiredminotaur.bcukbot.sql.discord.points.UserPoints;
import com.expiredminotaur.bcukbot.sql.discord.points.UserPointsRepository;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class PointsSystem
{
    @Autowired
    private UserPointsRepository pointsData;

    private static final int pointsMin = 15;
    private static final int pointsMax = 25;

    private final Random RNG = new Random();

    public void addXP(Member user)
    {
        long time = System.currentTimeMillis();
        long userID = user.getId().asLong();
        UserPoints pointsUser = pointsData.findById(userID).orElse(new UserPoints(userID));
        pointsUser.setLastUserName(user.getDisplayName());
        if (time - pointsUser.getLastPointsReceived() > TimeUnit.MINUTES.toMillis(1))
        {
            pointsUser.setLastPointsReceived(time);
            int points = RNG.nextInt(pointsMax - pointsMin + 1) + pointsMin;
            pointsUser.givePoints(points);
            //TODO: pointsRewards.process(user, member.points);
            pointsData.save(pointsUser);
        }
    }

    public Mono<Void> points(DiscordCommandEvent eventIn)
    {
        MessageCreateEvent event = eventIn.getEvent();
        if (event.getMember().isPresent())
        {
            Member member = event.getMember().get();
            long userID = member.getId().asLong();
            UserPoints userPoints = pointsData.findById(userID).orElse(new UserPoints(userID));

            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .author(member.getDisplayName(), "", member.getAvatarUrl())
                    .addField("Rank", String.format("%d/%d", pointsData.getRank(member.getId().asLong()).get(0), pointsData.count()), true)
                    .addField("points", String.format("%d", userPoints.getPoints()), true).build();


            return event.getMessage().getChannel()
                    .flatMap(channel -> channel.createMessage(embed)).then();
        }
        return Mono.empty().then();
    }
}
