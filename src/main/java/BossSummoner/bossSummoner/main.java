package BossSummoner.bossSummoner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin{
	static HashMap<String,Object> settings=null;
	static long NextSummon=0;
	static String NextBoss=null;
	static long SummonInterval=0;
	static ArrayList<String> BossList=null;
	String World;
	long timeOfSummoning=0;
	int x;
	int y;
	int z;
	int summoningTask=0;
	int broadcastTask=0;
	Random rnd=new Random();
	@SuppressWarnings("unchecked")
	@Override 
	public void onEnable() {
		File f = getDataFolder();
		if (!f.exists()){
			f.mkdir();
			saveResource("config.yml", false);
			saveResource("plugin.yml", false);
			NextSummon=14400;
		}else{
			settings=(HashMap<String, Object>) getConfig().getValues(true);
			NextSummon=Long.parseLong(settings.get("NextSummon").toString());
		}
		SummonInterval=Long.parseLong(settings.get("SummonInterval").toString())*60;
		BossList=(ArrayList<String>) settings.get("Bosses");
		NextBoss=settings.get("NextBoss").toString();
		World=settings.get("World").toString();
		x=Integer.parseInt(settings.get("x").toString());
		y=Integer.parseInt(settings.get("y").toString());
		z=Integer.parseInt(settings.get("z").toString());
		getLogger().info(BossList.toString());
		getLogger().info("BossSummoner Loaded Successfully");
		timeOfSummoning=System.currentTimeMillis()+NextSummon*1000;
		summoningTask=Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
			public void run(){
				RecurringSummon();
			}
		}
				, NextSummon*20);
		broadcastTask=Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				getServer().broadcastMessage(ChatColor.DARK_RED+"["+ChatColor.GOLD+"Boss"+ChatColor.DARK_RED+"] "+ChatColor.BLUE+NextBoss+" will be summoned in " + getRemainingTimeString());
			}
		}, 1200, 2400*Integer.parseInt(settings.get("AutoBroadcastTime").toString()));
	}
	@Override 
	public void onDisable() {
		getServer().getScheduler().cancelTask(broadcastTask);
		getServer().getScheduler().cancelTask(summoningTask);
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
				+ "AutoBroadcastTime: Time between [Boss] Broadcasts\r\n"
				+ "---------------------------------------------------\r\n"
				+ "The first broadcast always happens 10 minutes after the reboot"
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
		this.saveConfig();
		getLogger().info("BossSummoner Unloaded Successfully");
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
		summoningTask=Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				timeOfSummoning=System.currentTimeMillis()+SummonInterval*1000;
				getServer().dispatchCommand(getServer().getConsoleSender(), "mm mobs spawn "+NextBoss+" 1 "+World+","+x+","+y+","+z);
				NextBoss=BossList.get(rnd.nextInt(BossList.size())).toString();
			}
		},0,SummonInterval*20);
	}
	public String getRemainingTimeString(){
		double hrs=0;
		double mins=0;
		double remaining=getRemainingMillis()/1000L;
		StringBuilder str=new StringBuilder();
		hrs=(remaining/60)/60;
		if ((int)(hrs)>1){
			str.append((int) hrs+" Hours, ");
		}
		remaining=remaining-(int)hrs*60*60;
		mins=(remaining/60);
		if ((int) mins>1 || hrs!=0){
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
