package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

public class Killer extends Role implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8964457287463525833L;

	@Override
	public void action(DataPacket dp) {
		dp.setCommand(Commands.KILL);
	}
}
