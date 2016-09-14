package ben_and_asaf_ttp.thetownproject;

import java.io.*;
import java.net.Socket;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;

/**
 * Created by user on 12/09/2016.
 */
public class ClientConnection {

    /**
     * Output stream for data
     */
    public ObjectOutputStream out;

    /**
     * Input stream for data
     */
    public ObjectInputStream in;

    /**
     * States if the user is online or not
     * <ul>
     * <li>true = online</li>
     * <li>false = offline</li>
     * </ul>
     */
    public Boolean online = false;

    /**
     * {@code Socket} used for Input and Output streams
     */
    private Socket connection;

    /**
     * Specifies the hostname to connect to (Server IP)
     */
    private String hostname = "5.102.197.111";

    /**
     * Port number, used with hostname <i>e.g. (127.0.0.1:5555)</i>
     */
    private int port = 55555;

    /**
     * Create a <b>Singleton</b> connection of the class
     */
    public static ClientConnection client = new ClientConnection();

    /**
     * Create a singleton connection
     */
    private ClientConnection() {
    }

    /**
     * Retry the connection to the server
     * @throws java.io.IOException
     */
    public void startConnection() throws IOException {
        if (online == false) {
            connection = new Socket(hostname, port);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            online = true;
        }
    }

    /**
     * Close all connections and alert the server of it
     */
    public void exit() {
        if (online == true) {
            try {
                out.writeObject(Commands.DISCONNECT);
                online = false;
                out.close();
                in.close();
                connection.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Finalize the object and also exit from the server;
     */
    @Override
    protected void finalize() {
        exit();
    }
}
