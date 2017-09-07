package BossSummoner.bossSummoner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class main extends JavaPlugin{
	static boolean warned=false;
	static HashMap<String,Object> settings=null;
	static int warningtime = 0;
	static long NextSummon=0;
	static String NextBoss=null;
	static long SummonInterval=0;
	static ArrayList<String> BossList=null;
	final main instance = this;
	String World;
	long timeOfSummoning=0;
	int x;
	int y;
	static boolean enabled = false;
	int z;
	static long r=0;
	static int bossSummonResolution = 20;
	BukkitTask summoningTask=null;
	BukkitTask broadcastTask=null;
	BukkitTask st = null;
	Random rnd=new Random();
	@SuppressWarnings("unchecked")
	@Override 
	public void onEnable() {
		File f = getDataFolder();
		if (!f.exists()){
			f.mkdir();
			saveResource("config.yml", false);
			//saveResource("plugin.yml", false);
			NextSummon=14400;
		}
		settings=(HashMap<String, Object>) getConfig().getValues(true);
		NextSummon=Long.parseLong(settings.get("NextSummon").toString());
		r = Long.parseLong(settings.get("radius").toString());
		SummonInterval=Long.parseLong(settings.get("SummonInterval").toString())*60;
		BossList=(ArrayList<String>) settings.get("Bosses");
		NextBoss=settings.get("NextBoss").toString();
		World=settings.get("World").toString();
		if (Bukkit.getServer().getWorld(World)==null){
			Bukkit.getServer().getLogger().warning("The world " + World + " wasn't found, this plugin will disable itself");
			Bukkit.getServer().getPluginManager().disablePlugin(this);
		}else{
			enabled = true;
			x=Integer.parseInt(settings.get("x").toString());
			y=Integer.parseInt(settings.get("y").toString());
			z=Integer.parseInt(settings.get("z").toString());
			bossSummonResolution = Integer.parseInt(settings.get("bossSummonResolution").toString());
			getLogger().info(BossList.toString());
			getLogger().info("BossSummoner Loaded Successfully");
			warningtime = Integer.parseInt(settings.get("WarningTime").toString());
			timeOfSummoning=System.currentTimeMillis()+NextSummon*1000;
			summoningTask=Bukkit.getServer().getScheduler().runTaskLater(this, new Runnable(){
				public void run(){
						RecurringSummon();
				}
			}, bossSummonResolution);
			broadcastTask=Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
				public void run(){
					getServer().broadcastMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] "+ChatColor.BLUE + NextBoss + " will be summoned in " + getRemainingTimeString());
				}
			}, 1200, 2400*Integer.parseInt(settings.get("AutoBroadcastTime").toString()));
		}
	}
	@Override 
	public void onDisable() {
		getLogger().info("Unloading BossSummoner");
		if (enabled){
			if (st!=null){
				st.cancel();
			}
			broadcastTask.cancel();
			summoningTask.cancel();
			NextSummon=getRemainingMillis()/1000;
			this.getConfig().options().header(
					"--------------------------------------\r\n"
					+ "bossSummoner Configuration File\r\n"
					+ "--------------------------------------\r\n"
					+ "Bosses: a list of the bosses to roll through\r\n"
					+ "SummonInterval: Interval between boss summons (minutes)\r\n"
					+ "NextSummon: When to summon the next boss (seconds) (DON'T EDIT)\r\n"
					+ "NextBoss: Which boss to summon Next (name) (DON'T EDIT)\r\n"
					+ "World: Which world to spawn the boss in\r\n"
					+ "x,y,z: coordinates where to spawn the boss\r\n"
					+ "AutoBroadcastTime: Time between [Boss] Broadcasts (Minutes)\r\n"
					+ "WarningTime: How many minutes before the spawn shall there be an extra [Boss] broadcast?\r\n"
					+ "bossSummonResolution: How many ticks should pass between each summon time check? \r\n"
					+ "---------------------------------------------------\r\n"
					);
			this.getConfig().options().copyHeader(true);
			this.getConfig().set("Bosses", settings.get("Bosses"));
			this.getConfig().set("SummonInterval", (int) SummonInterval/60);
			this.getConfig().set("NextSummon", NextSummon);
			this.getConfig().set("NextBoss", NextBoss);
			this.getConfig().set("World",World);
			this.getConfig().set("x",x);
			this.getConfig().set("y",y);
			this.getConfig().set("z",z);
			this.getConfig().set("AutoBroadcastTime",settings.get("AutoBroadcastTime"));
			this.getConfig().set("WarningTime", settings.get("WarningTime"));
			this.getConfig().set("bossSummonResolution", settings.get("bossSummonResolution"));
			this.saveConfig();
			getLogger().info("BossSummoner Unloaded Successfully");
		}
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("boss")){
			sender.sendMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] "+ChatColor.BLUE+NextBoss+" will be summoned in " + getRemainingTimeString());
			return true;
		}
		return false;
	}
	public void RecurringSummon(){
		summoningTask=Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable(){
			public void run(){
				if (getRemainingMillis() < (warningtime * 60*1000) && !warned){
					getServer().broadcastMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] "+ChatColor.BLUE+NextBoss+" will be summoned in " + getRemainingTimeString());
					warned=true;
				}
				if (getRemainingMillis() <= 0){
					//Summoning
					Location loc = new Location(Bukkit.getWorld(World), x, y, z);
					Collection<Entity> ent= Bukkit.getWorld(World).getNearbyEntities(loc, r, r, r);
					ArrayList<Player> pl = new ArrayList<Player>();
					for (Entity e: ent){
						if (e instanceof Player){
							pl.add((Player) e);
						}
					}
					if (pl.size() != 0){
						getServer().dispatchCommand(getServer().getConsoleSender(), "mm mobs spawn "+NextBoss+" 1 "+World+","+x+","+y+","+z);
						getServer().broadcastMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] " + ChatColor.BLUE + NextBoss+ " has been summoned");
					}else{
						getServer().broadcastMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] " + ChatColor.BLUE + "Nobody was near the boss area for the boss, the boss was not summoned");
					}
					timeOfSummoning=System.currentTimeMillis()+SummonInterval*1000;
					NextBoss=BossList.get(rnd.nextInt(BossList.size())).toString();
					warned=false;
				}
			}
		},0,bossSummonResolution);
	}
	public String getRemainingTimeString(){
		/*long mil = getRemainingMillis();
		String str="";
		if (mil > 3600000){
			str=String.format("%02d Hours, %02d Minutes, %02d Seconds", mil/3600000,
								(mil/60000) % 60,
								(mil/1000) % 60);
		}else{
			if (mil > 60000){
				str=String.format("%02d Minutes, %02d Seconds", (mil/60000) % 60,
						(mil/1000) % 60);
			}else{
				str=String.format("%02d Seconds", (mil/1000) % 60);
			}
		}
		return str;*/
		double hrs=0;
		double mins=0;
		double remaining=getRemainingMillis()/1000L;
		StringBuilder str=new StringBuilder();
		hrs=(remaining/60)/60;
		if ((int)(hrs)>=1){
			str.append((int) hrs+" Hours, ");
		}
		remaining=remaining-(int)hrs*60*60;
		mins=(remaining/60);
		if ((int) mins>=1 || hrs!=0){
			str.append((int) mins+ " Minutes, ");
		}
		remaining=remaining-(int)mins*60;
		str.append((int) remaining + " Seconds");
		return str.toString();
	}
	public long getRemainingMillis(){
		return timeOfSummoning-System.currentTimeMillis();
	}
}
