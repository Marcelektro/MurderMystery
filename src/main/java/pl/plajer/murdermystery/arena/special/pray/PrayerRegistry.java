/*
 * Murder Mystery is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Murder Mystery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Murder Mystery.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.murdermystery.arena.special.pray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.api.StatsStorage;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.arena.ArenaRegistry;
import pl.plajer.murdermystery.arena.ArenaState;
import pl.plajer.murdermystery.arena.role.Role;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.language.LanguageManager;
import pl.plajer.murdermystery.user.User;
import pl.plajer.murdermystery.utils.ItemPosition;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * @author Plajer
 * <p>
 * Created at 16.10.2018
 */
public class PrayerRegistry {

  private static Main plugin;
  private static List<Prayer> prayers = new ArrayList<>();
  private static Random rand;

  public static void init(Main plugin) {
    PrayerRegistry.plugin = plugin;
    //good prayers
    prayers.add(new Prayer(Prayer.PrayerType.DETECTIVE_REVELATION, true, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Detective-Revelation")));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_RUSH, true, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Gold-Rush")));
    prayers.add(new Prayer(Prayer.PrayerType.SINGLE_COMPENSATION, true, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Single-Compensation")));
    prayers.add(new Prayer(Prayer.PrayerType.BOW_TIME, true, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Gifts.Bow-Time")));

    //bad prayers
    prayers.add(new Prayer(Prayer.PrayerType.SLOWNESS_CURSE, false, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Slowness-Curse")));
    prayers.add(new Prayer(Prayer.PrayerType.BLINDNESS_CURSE, false, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Blindness-Curse")));
    prayers.add(new Prayer(Prayer.PrayerType.GOLD_BAN, false, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Gold-Ban")));
    prayers.add(new Prayer(Prayer.PrayerType.INCOMING_DEATH, false, ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Curses.Incoming-Death")));
    rand = new Random();
  }

  public static Prayer getRandomPray() {
    return prayers.get(rand.nextInt(prayers.size()));
  }

  public static Prayer getRandomBadPray() {
    Prayer prayer = prayers.get(rand.nextInt(prayers.size()));
    if (prayer.isGoodPray()) {
      getRandomBadPray();
    }
    return prayer;
  }

  public static List<Prayer> getPrayers() {
    return prayers;
  }

  public static void applyPrayer(User user, Prayer prayer) {
    user.setStat(StatsStorage.StatisticType.LOCAL_CURRENT_PRAY, prayer.getPrayerType().ordinal());
    Player player = user.toPlayer();
    final Arena arena = ArenaRegistry.getArena(user.toPlayer());
    List<String> prayMessage = LanguageManager.getLanguageList("In-Game.Messages.Special-Blocks.Praises.Message");
    if (prayer.isGoodPray()) {
      prayMessage = prayMessage.stream().map(msg -> msg.replace("%feeling%", ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Feelings.Blessed"))).collect(Collectors.toList());
    } else {
      prayMessage = prayMessage.stream().map(msg -> msg.replace("%feeling%", ChatManager.colorMessage("In-Game.Messages.Special-Blocks.Praises.Feelings.Cursed"))).collect(Collectors.toList());
    }
    prayMessage = prayMessage.stream().map(msg -> msg.replace("%praise%", prayer.getPrayerDescription())).collect(Collectors.toList());
    switch (prayer.getPrayerType()) {
      case BLINDNESS_CURSE:
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0, false, false));
        break;
      case BOW_TIME:
        if (Role.isRole(Role.INNOCENT, player)) {
          ItemPosition.setItem(player, ItemPosition.BOW, new ItemStack(Material.BOW, 1));
          ItemPosition.setItem(player, ItemPosition.INFINITE_ARROWS, new ItemStack(Material.ARROW, 64));
        }
        break;
      case DETECTIVE_REVELATION:
        String detectiveName;
        if (arena.getDetective() != null) {
          detectiveName = Bukkit.getOfflinePlayer(arena.getDetective()).getName();
        } else if (arena.getFakeDetective() != null) {
          detectiveName = Bukkit.getOfflinePlayer(arena.getFakeDetective()).getName();
        } else {
          detectiveName = "????";
        }
        prayMessage = prayMessage.stream().map(msg -> msg.replace("%detective%", detectiveName)).collect(Collectors.toList());
        break;
      case GOLD_BAN:
        break;
      case GOLD_RUSH:
        break;
      case INCOMING_DEATH:
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          if (arena.getArenaState() == ArenaState.IN_GAME) {
            player.damage(1000);
          }
        }, 20 * 60);
        break;
      case SINGLE_COMPENSATION:
        ItemStack stack = player.getInventory().getItem(8);
        if (stack == null) {
          stack = new ItemStack(Material.GOLD_INGOT, 5);
        } else {
          stack.setAmount(stack.getAmount() + 5);
        }
        player.getInventory().setItem(8, stack);
        break;
      case SLOWNESS_CURSE:
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false, false));
        break;
    }
    for (String msg : prayMessage) {
      MinigameUtils.sendCenteredMessage(player, msg);
    }
  }

}