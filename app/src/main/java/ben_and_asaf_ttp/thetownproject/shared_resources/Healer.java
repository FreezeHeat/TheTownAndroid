package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

public class Healer extends Role implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2562831686790119946L;

	@Override
	public void action(DataPacket dp) {
		dp.setCommand(Commands.HEAL);
	}
}
