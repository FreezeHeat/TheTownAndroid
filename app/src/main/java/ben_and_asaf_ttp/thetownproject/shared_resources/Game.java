package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * {@code Game} class, holds the information about each game in the database
 * which includes the players currently playing the game
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @see Player
 * @since 1.0
 */
@Entity
@Table(name="Games")
public class Game implements Serializable{
	
	/**
	 * An auto generated ID for a game, used to identify a {@code Game} instance
	 */
	@Column(nullable=false, name="GameID")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int gameId;
	
	/**
	 * The maximum amount of players to play the game
	 */
	@Column(name="MaxPlayers", nullable=true)
	private int maxPlayers;
	
	/**
	 * The role bank({@code ArrayList} format) for this game based on {@code maxPlayers}
	 * (<b>Note:</b> this role bank is given by the server)
	 * @see Roles
	 */
	@Transient
	private ArrayList<Roles> rolesBank;
	
	/**
	 * This {@code ArrayList} holds all the players that are in the game
	 */
	@ManyToMany(targetEntity=Player.class)
	private List<Player> players;
	
	/**
	 * The game's creation date
	 */
	@Column(nullable=false, name="DateCreated")
	@Temporal(TemporalType.DATE)
	private Date date;

    /**
     * This constructor creates an empty <code>Game</code> with today's {@code Date}
     */
	public Game(){
		this.date = new Date();
	}

	/**
	 * Get the maximum allowed number of players
	 * @return maximum allowed number of players
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * Set the maximum allowed number of players
	 * @param maxPlayers maximum allowed number of players to be set
	 */
	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}
	
	/**
	 * Get the game's role bank
	 * @return the game's role bank
	 */
	public ArrayList<Roles> getRolesBank() {
		return rolesBank;
	}
	
	/**
	 * Set the game's role bank({@code ArrayList} format)
	 * @param rolesBank the game's role bank to be set
	 */
	public void setRolesBank(ArrayList<Roles> rolesBank) {
		this.rolesBank = rolesBank;
	}
	
	/**
	 * Get a list of all the players in the game
	 * @return list of all the players in the game
	 */
	public List<Player> getPlayers() {
		return this.players;
	}

	/**
	 * Set a list of all the players in the game
	 * @param players the list of players to be set
	 */
	public void setPlayers(ArrayList<Player> players) {
		this.players = players;
	}

	/**
	 * Get the game's unique ID
	 * @return the game's unique ID
	 */
	public int getGameId() {
		return gameId;
	}
	
	/**
	 * Get the game's creation date
	 * @return game's creation date
	 * @see java.util.Date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Get how many player slots are left in the game player's list
	 * <p><b>NOTE:</b><i> Used when a player wants to join an available game</i></p>
	 * @return
	 */
	public int getPlayerSlotsLeft(){
		return maxPlayers - players.size();
	}
	
	/**
	 * Returns the game's date as a {@code String}
	 * @return the game's date (String format)
	 */
	public String getDateString(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateFormat.format(this.date);
	}
	
	/**
	 * Shuffles the roles bank, and sets randomized roles for the players
	 * @see Game#rolesBank
	 * @see Game#players
	 */
	public void distributeRoles(){
		Collections.shuffle(this.rolesBank);
		int i = 0;
		for(Player p : this.players){
			p.setRole(rolesBank.get(i));
		}
	}
}
