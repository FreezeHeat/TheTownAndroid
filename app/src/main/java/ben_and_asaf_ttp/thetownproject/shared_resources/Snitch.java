package ben_and_asaf_ttp.thetownproject.shared_resources;

public class Snitch extends Role{

	@Override
	public void action(final DataPacket dp) {
		dp.setCommand(Commands.SNITCH);
	}
}
