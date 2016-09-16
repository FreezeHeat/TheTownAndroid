package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;

/**
 *  This abstract class is the base class for all the roles in the game,
 * this is used by the server and client to identify and set roles in the game
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @see Game
 * @since 1.0
 */
public abstract class Role implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4180920739866703229L;

	/**
	 * Use your action (Based on Role) and add it to the DataPacket
	 * @param dp The DataPacket which is set to your action
	 */
    public abstract void action(DataPacket dp);
}
