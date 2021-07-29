package com.expiredminotaur.bcukbot.sql.discord.points;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserPointsRepository extends CrudRepository<UserPoints, Long>
{
    @Query(value = "select rankByPoints " +
            "from(select discord_user_id, rank() over (order by points desc) as rankByPoints from user_points) as q1 " +
            "where q1.discord_user_id =:id", nativeQuery = true)
    List<Long> getRank(long id);
}
