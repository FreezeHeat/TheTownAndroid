package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

public class Snitch extends Role implements Serializable{

	@Override
	public void action(DataPacket dp) {
		dp.setCommand(Commands.SNITCH);
	}
}
