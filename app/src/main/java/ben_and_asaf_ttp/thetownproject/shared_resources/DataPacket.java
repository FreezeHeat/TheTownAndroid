package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.io.Serializable;
import java.util.List;

public class DataPacket implements Serializable{
	private Commands command;
	private Game game;
	private List<Game> games;
	private Player player;
	private List<Player> players;
	private String message;
	private String server_message;
	private int number;
	
	public DataPacket(){};
	
	public DataPacket(Commands command, Game game, List<Game> games, Player player, List<Player> players, 
			String message,String server_message, int number) {
		this.command = command;
		this.game = game;
		this.games = games;
		this.player = player;
		this.players = players;
		this.message = message;
		this.server_message = server_message;
		this.number = number;
	}

	public Commands getCommand() {
		return command;
	}

	public void setCommand(Commands command) {
		this.command = command;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}
	
	public List<Game> getGames() {
		return games;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getServer_message() {
		return server_message;
	}

	public void setServer_message(String server_message) {
		this.server_message = server_message;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "DataPacket [command=" + command + ", game=" + game + ", player=" + player + ", players=" + players
				+ ", message=" + message + ", server_message=" + server_message + "]";
	}
	
	
}
