/************************************************************************
 * This file is part of AdminCmd.									
 *																		
 * AdminCmd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by	
 * the Free Software Foundation, either version 3 of the License, or		
 * (at your option) any later version.									
 *																		
 * AdminCmd is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of		
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
 * GNU General Public License for more details.							
 *																		
 * You should have received a copy of the GNU General Public License
 * along with AdminCmd.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/
package be.Balor.Manager.Commands.Weather;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Balor.bukkit.AdminCmd.AdminCmdWorker;

import be.Balor.Manager.ACCommands;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class Strike extends ACCommands {

	/**
	 * 
	 */
	public Strike() {
		permNode = "admincmd.weather.strike";
		cmdName = "bal_wstrike";
		other = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.Balor.Manager.ACCommands#execute(org.bukkit.command.CommandSender,
	 * java.lang.String[])
	 */
	@Override
	public void execute(CommandSender sender, String... args) {
		Player p = AdminCmdWorker.getInstance().getUser(args, permNode);
		p.getWorld().strikeLightning(p.getLocation());
		sender.sendMessage(ChatColor.GOLD + p.getName() + " was striked by Thor");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see be.Balor.Manager.ACCommands#argsCheck(java.lang.String[])
	 */
	@Override
	public boolean argsCheck(String... args) {
		return args != null;
	}

}