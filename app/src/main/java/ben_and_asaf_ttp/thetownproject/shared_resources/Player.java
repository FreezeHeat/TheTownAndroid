package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.util.List;

/**
 * {@code Player} class, holds the information about each player in the database
 * @author Ben Gilad and Asaf Yeshayahu
 * @version %I%
 * @since 1.0
 */

public class Player extends User{
	
	/**
     * {@code Stats} that hold the statistics of the player
     * @see Stats
     */
    private Stats stats;

    /**
     * {@code PlayerStatus} holds information about the player's status in the server
     * @see PlayerStatus
     */
    private PlayerStatus status;
    
    /**
     * {@code Role} holds information about the player's role in the game
     * @see Roles
     */
    private Roles role;
	
    /**
     * This list holds all the games the players has played (Game history)
     * @see Game
     */
    private List<Game> gameHistory;
    
    /**
     * The player's status, alive or not
     */
    private boolean alive;

	/**
	 * If the player acted or not
	 */
	private boolean acted;

    /**
     * This constructor creates an empty <code>Player</code>
     */    
    public Player(){}
    
    /**
     * Full constructor for the {@code Player}
     * @param username Player's username
     * @param password Player's password
     * @param stats Player's stats
     * @param status Player's current status
     * @param role Player's role
     * @see Stats
     * @see PlayerStatus
     * @see Roles
     */
    public Player(
    		final String username, final String password,
    		final Stats stats, final PlayerStatus status, final Roles role){
    	this.setUsername(username);
    	this.setPassword(password);
    	this.setStats(stats);
    	this.setStatus(status);
    	this.setRole(role);
    }
    
    /**
     * A constructor for the {@code Player}, for initializing new players,
     * since it uses only the username and password, the rest is set to their
     * default values
     * @param username the Player's username
     * @param password the Player's password
     */
    public Player(final String username, final String password){
    	this(username, password, new Stats(), PlayerStatus.OFFLINE, null);
    }
    
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}

	@Override
	public void setUsername(final String username) {
		// TODO Auto-generated method stub
		this.username = username;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}

	@Override
	public void setPassword(final String password) {
		// TODO Auto-generated method stub
		this.password = password;
	}

	/**
     * Return the {@code Stats} of the player
     * @return the player's stats
     */
    public Stats getStats(){
    	return this.stats;
    }
    
    /**
     * Set the {@code Stats} of the player
     * @param stats The player's statistics
     */
	public void setStats(final Stats stats){
		this.stats = stats;
	}
	
	/**
	 * Return the player's status (such as ONLINE, OFFLINE etc..)
	 * @return the player's status
	 * @see PlayerStatus
	 */
	public PlayerStatus getStatus(){
		return this.status;
	}
	
	/**
	 * Set the player's status (such as ONLINE, OFFLINE etc..)
	 * @param status the player's status to be set
	 * @see PlayerStatus
	 */
	public void setStatus(final PlayerStatus status){
		this.status = status;
	}
	
	/**
	 * Get the player's role
	 * @return the player's role
	 * @see Roles
	 */
	public Roles getRole() {
		return role;
	}

	/**
	 * Set the player's role
	 * @param role the role to be set
	 * @see Roles
	 */
	public void setRole(final Roles role) {
		this.role = role;
	}

	/**
	 * Get the player's game history
	 * @return The player's game history
	 */
	public List<Game> getGameHistory() {
		return gameHistory;
	}

	/**
	 * Set the player's game history
	 * @param gameHistory The player's game history to be set
	 */
	public void setGameHistory(final List<Game> gameHistory) {
		this.gameHistory = gameHistory;
	}

	/**
	 * Check if the player is alive
	 * @return true\false if alive
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * Set if the player is alive or not
	 * @param alive true/false player is alive/dead
	 */
	public void setAlive(final boolean alive) {
		this.alive = alive;
	}

	/**
	 * Set if action was done or not
	 * @param acted true/false if action was done or not
	 */
	public void setActed(final boolean acted) {
		this.acted = acted;
	}

	/**
	 * Check if the player has acted
	 * @return true\false if acted
	 */
	public boolean hasActed() {
		return this.acted;
	}

	@Override
    public boolean equals(final Object o){
		if(o instanceof Player){
			Player p = (Player) o;
			if(this.getUsername().equals(p.getUsername())&& 
			   this.getPassword().equals(p.getPassword())){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return getUsername() + " [stats=" + stats + "\n, status=" + status + "\n, role=" + role + ", gameHistory=" + gameHistory
				+ "\n, alive=" + alive + ", acted=" + acted + "]";
	}
}
