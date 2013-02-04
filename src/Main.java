/*
 * PvpLevels support for LevelFlare.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.WeakHashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import me.Hoot215.LevelFlare.api.LevelFlareLeveller;
import me.Hoot215.LevelFlare.api.LevellerHandler;
import me.lenis0012.pvp.PvpPlayer;

@LevellerHandler(name = "PvpLevelsLeveller", version = "1.0")
public class Main extends LevelFlareLeveller implements Listener
  {
    private WeakHashMap<Player, Player> killers =
        new WeakHashMap<Player, Player>();
    private WeakHashMap<Player, Integer> killersLevels =
        new WeakHashMap<Player, Integer>();
    
    private void handleLevelUp (Player player, int level)
      {
        FileConfiguration config = this.getPlugin().getConfig();
        if (config.isInt("pvplevels.multiple"))
          {
            if (level % config.getInt("pvplevels.multiple") != 0)
              return;
          }
        this.getPlugin().launchFireworks(player);
      }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity (EntityDamageByEntityEvent event)
      {
        Entity defender = event.getEntity();
        Entity attacker = event.getDamager();
        int damage = event.getDamage();
        
        if ( ! (defender instanceof Player && attacker instanceof Player))
          return;
        
        Player p1 = (Player) defender;
        Player p2 = (Player) attacker;
        if (p1.getHealth() - damage <= 0)
          {
            if (killers.containsKey(p1))
              {
                if (killers.get(p1) != p2)
                  {
                    killers.put(p1, p2);
                  }
              }
            else
              {
                killers.put(p1, p2);
              }
          }
      }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDeathLow (EntityDeathEvent event)
      {
        Entity entity = event.getEntity();
        
        if ( ! (entity instanceof Player))
          return;
        
        Player p1 = (Player) entity;
        if (killers.containsKey(p1))
          {
            Player p2 = killers.get(p1);
            if (p2.isOnline())
              {
                killersLevels.put(p2, new PvpPlayer(p2).getLevel());
              }
          }
      }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathMonitor (EntityDeathEvent event)
      {
        Entity entity = event.getEntity();
        
        if ( ! (entity instanceof Player))
          return;
        
        Player p1 = (Player) entity;
        if (killers.containsKey(p1))
          {
            Player p2 = killers.get(p1);
            if (p2.isOnline())
              {
                if (killersLevels.containsKey(p2))
                  {
                    int level = new PvpPlayer(p2).getLevel();
                    if (level > killersLevels.get(p2))
                      {
                        this.handleLevelUp(p2, level);
                      }
                    killersLevels.remove(p2);
                  }
              }
            killers.remove(p1);
          }
      }
    
    @Override
    public void onUnload ()
      {
        this.getPlugin().getLevellerManager().unregisterBukkitEvents(this);
        this.getLogger().info("Is now unloaded");
      }
    
    @Override
    public void onLoad ()
      {
        this.getPlugin().getLevellerManager().registerBukkitEvents(this);
        this.getLogger().info("Is now loaded");
      }
  }
