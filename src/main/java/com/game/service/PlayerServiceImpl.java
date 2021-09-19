package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public List<Player> getPlayers(
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
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach((player -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (after != null && !player.getBirthday().after(afterDate)) return;
            if (before != null && !player.getBirthday().before(beforeDate)) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            list.add(player);
        }));
        return list;
    }

    @Override
    public List<Player> sortPlayers(List<Player> players, PlayerOrder order) {
        if (order != null) {
            players.sort((p1, p2) -> {
                switch (order) {
                    case ID:
                        return p1.getId().compareTo(p2.getId());
                    case NAME:
                        return p1.getName().compareTo(p2.getName());
                    case EXPERIENCE:
                        return p1.getExperience().compareTo(p2.getExperience());
                    case BIRTHDAY:
                        return p1.getBirthday().compareTo(p2.getBirthday());
                    case LEVEL:
                        return p1.getLevel().compareTo(p2.getLevel());
                    default:
                        return 0;
                }
            });
        }
        return players;

    }


    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size()) to = players.size();
        return players.subList(from, to);
    }

    @Override
    public Player getPlayer(Long id) {
        final Player player = playerRepository.findById(id).orElse(null);
        return player;
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        Calendar c = Calendar.getInstance();
        c.set(2000, Calendar.JANUARY, 0);
        Date before = c.getTime();
        c.clear();
        c.set(3000, Calendar.DECEMBER, 31);
        Date after = c.getTime();

        if (player == null) return false;
        if (player.getRace() == null ||
                player.getProfession() == null) return false;
        if (player.getName() == null || player.getName().length() > 12) return false;
        if (player.getTitle() == null || player.getTitle().length() > 30) return false;
        if (player.getExperience() < 0 || player.getExperience() > 10_000_000) return false;
        if (player.getBirthday().after(after) || player.getBirthday().before(before)) return false;
        return true;
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player updatePlayer(Player oldPlayer, Player newPlayer) throws IllegalArgumentException {
        String name = newPlayer.getName();
        if (name != null) {
            if (name.length() <= 12) {
                oldPlayer.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        String title = newPlayer.getTitle();
        if (title != null) {
            if (title.length() <= 30) {
                oldPlayer.setTitle(title);
            } else {
                throw new IllegalArgumentException();
            }
        }

        Race race = newPlayer.getRace();
        if (race != null) {
            oldPlayer.setRace(race);
        }

        Profession profession = newPlayer.getProfession();
        if (profession != null) {
            oldPlayer.setProfession(profession);
        }
        Integer experience = newPlayer.getExperience();

        if (experience != null) {
            if (experience > 0 && experience <= 10_000_000) {
                oldPlayer.setExperience(experience);
                oldPlayer.setLevel(
                        calcLevel(experience));
                oldPlayer.setUntilNextLevel(
                        untilExp(experience, oldPlayer.getLevel()));

            } else {
                throw new IllegalArgumentException();
            }
        }

        Calendar c = Calendar.getInstance();
        c.set(2000, Calendar.JANUARY, 0);
        Date after = c.getTime();
        c.clear();
        c.set(3000, Calendar.DECEMBER, 31);
        Date before = c.getTime();
        Date birthday = newPlayer.getBirthday();
        if (birthday != null) {
            if (birthday.before(before) && birthday.after(after)) {
                oldPlayer.setBirthday(birthday);
            } else {
                throw new IllegalArgumentException();
            }
        }

        Boolean banned = newPlayer.getBanned();
        if (banned != null) {
            oldPlayer.setBanned(banned);
        }


        playerRepository.save(oldPlayer);
        return oldPlayer;
    }

    @Override
    public Integer calcLevel(Integer exp) {
        return  (int) (Math.sqrt(2500 + 200 * exp) - 50) / 100;
    }

    @Override
    public Integer untilExp(Integer exp, Integer lv) {
        return 50 * (lv + 1) * (lv + 2) - exp;
    }
}
