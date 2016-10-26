package ben_and_asaf_ttp.thetownproject.shared_resources;

import java.util.List;

import com.google.gson.Gson;

public class DataPacket{
	private Commands command;
	private Game game;
	private List<Game> games;
	private Player player;
	private List<Player> players;
	private String message;
	private String server_message;
	private int number;
	private transient Gson gson;

	public DataPacket(){
		this(null, null, null, null, null, null, null);
	}

	public DataPacket(final Commands command, final Game game, final List<Game> games, final Player player, final List<Player> players,
					  final String message,final String server_message) {
		this.command = command;
		this.game = game;
		this.games = games;
		this.player = player;
		this.players = players;
		this.message = message;
		this.server_message = server_message;
		this.gson = new Gson();
	}

	public Commands getCommand() {
		return command;
	}

	public void setCommand(final Commands command) {
		this.command = command;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(final Game game) {
		this.game = game;
	}

	public List<Game> getGames() {
		return games;
	}

	public void setGames(final List<Game> games) {
		this.games = games;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(final List<Player> players) {
		this.players = players;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public String getServer_message() {
		return server_message;
	}

	public void setServer_message(final String server_message) {
		this.server_message = server_message;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}

	public String toJson(){
		return this.gson.toJson(this, DataPacket.class);
	}

	public DataPacket fromJson(final String json){
		return this.gson.fromJson(json, DataPacket.class);
	}

	@Override
	public String toString() {
		return "DataPacket [command=" + command + ", game=" + game + ", games=" + games + ", player=" + player
				+ ", players=" + players + ", message=" + message + ", server_message=" + server_message + ", number="
				+ number + "]";
	}
}