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
package be.Balor.Tools.Egg.Types;

import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEggThrowEvent;

import be.Balor.Manager.Commands.CommandArgs;
import be.Balor.Tools.Utils;
import be.Balor.Tools.Egg.EggType;
import be.Balor.Tools.Egg.Exceptions.ProcessingArgsException;
import be.Balor.bukkit.AdminCmd.ConfigEnum;

/**
 * @author Balor (aka Antoine Aflalo)
 * 
 */
public class ExplosionEgg extends EggType<Float> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 729116147611485304L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see be.Balor.Tools.Egg.EggType#execute(org.bukkit.event.player.
	 * PlayerEggThrowEvent)
	 */
	@Override
	public void onEvent(PlayerEggThrowEvent event) {
		Egg egg = event.getEgg();
		egg.remove();
		event.setHatching(false);
		egg.getWorld().createExplosion(egg.getLocation(), value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.Balor.Tools.Egg.EggType#processArguments(be.Balor.Manager.Commands
	 * .CommandArgs)
	 */
	@Override
	protected void processArguments(Player sender, CommandArgs args) throws ProcessingArgsException {
		float power = ConfigEnum.DVULCAN.getFloat();
		if (args.hasFlag('p')) {
			String flag = args.getValueFlag('p');
			try {
				power = Float.parseFloat(flag);
			} catch (NumberFormatException e) {
				Utils.sI18n(sender, "NaN", "number", flag);
				return;
			}
		}
		value = power;

	}

}
