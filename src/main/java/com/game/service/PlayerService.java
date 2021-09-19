package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Optional;

public interface PlayerService {

    List<Player> getPlayers(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel

    );
    List<Player> sortPlayers(List<Player> players, PlayerOrder order);
    List<Player> getPage(List<Player> players, Integer pageNumber, Integer PageSize);
    Player getPlayer(Long id);
    void deletePlayer(Player player);
    boolean isPlayerValid(Player player);

    Player savePlayer(Player player);
    Player updatePlayer(Player oldPlayer, Player newPlayer);
    Integer calcLevel(Integer exp);
    Integer untilExp(Integer exp,  Integer lv);
}
