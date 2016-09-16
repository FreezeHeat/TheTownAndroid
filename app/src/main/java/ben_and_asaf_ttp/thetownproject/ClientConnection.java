package ben_and_asaf_ttp.thetownproject;

import android.util.Log;

import java.io.*;
import java.net.Socket;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;

/**
 * Created by user on 12/09/2016.
 */
public class ClientConnection {

    /**
     * Output stream for data
     */
    private ObjectOutputStream out;

    /**
     * Input stream for data
     */
    private ObjectInputStream in;

    /**
     * States if the user is online or not
     * <ul>
     * <li>true = online</li>
     * <li>false = offline</li>
     * </ul>
     */
    private Boolean online = false;

    /**
     * {@code Socket} used for Input and Output streams
     */
    private Socket connection;

    /**
     * Specifies the hostname to connect to (Server IP)
     */

    //10.0.2.2 - is the IP for the external localhost, because the VIRTUAL MACHINE of the android device
    //already is the 127.0.0.1 - the external IP to the "real" localhost is 10.0.2.2
    private String hostname = "10.0.2.2";

    /**
     * Port number, used with hostname <i>e.g. (127.0.0.1:5555)</i>
     */
    private int port = 55555;

    /**
     * Create a <b>Singleton</b> connection of the class
     */
    private static ClientConnection client = new ClientConnection();

    /**
     * Create a singleton connection
     */
    private ClientConnection() {
    }

    /**
     * Get the singleton connection of the socket
     * @return The singleton connection of the socket
     */
    public static ClientConnection getConnection(){
        return client;
    }

    public ObjectOutputStream getOutput(){
        return this.out;
    }

    public ObjectInputStream getInput(){
        return this.in;
    }
    /**
     * Get the status of the socket, online or not (true / false)
     * @return The status of the socket(true/false -- online/offline)
     */
    public boolean isOnline(){
        return this.online;
    }

    /**
     * Retry the connection to the server
     * @throws java.io.IOException
     */
    public void startConnection(){
        if (online == false) {
            try {
                connection = new Socket(hostname, port);
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                online = true;
                Log.i(this.getClass().getName(), "Socket connection successful " +
                        this.hostname+ ":" + this.port);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(this.getClass().getName(), "Failed to start connection with the socket");
            }
        }
    }

    public void sendDataPacket(DataPacket dp){
        try {
            this.getOutput().writeObject(dp.toJson());
            Log.i(this.getClass().getName(), "DataPacket sent: " + dp.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().getName(), "writeObject failed with this DataPacket: " + dp.toString());
        }
    }

    public DataPacket receiveDataPacket(){
        DataPacket dp = new DataPacket();
        try {
            dp = dp.fromJson((String)this.getInput().readObject());
            Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(this.getClass().getName(), "readObject failed - class not found exception");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.getClass().getName(), "readObject failed - IO exception - probably socket");
        }
        return dp;
    }

    /**
     * Close all connections and alert the server of it
     */
    public void exit() {
        if (online == true) {
            try {
                DataPacket dp = new DataPacket();
                dp.setCommand(Commands.DISCONNECT);
                out.writeObject(dp.toJson());
                online = false;
                out.close();
                in.close();
                connection.close();
                Log.i(this.getClass().getName(), "Socket connection was closed");
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(this.getClass().getName(), "Socket disconnect has failed");
            }
        }
    }

    /**
     * Finalize the object and also exit from the server;
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        exit();
    }
}
