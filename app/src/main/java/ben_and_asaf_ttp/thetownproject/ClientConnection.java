package ben_and_asaf_ttp.thetownproject;

import android.util.Log;

import java.io.*;
import java.net.Socket;

import ben_and_asaf_ttp.thetownproject.shared_resources.Commands;
import ben_and_asaf_ttp.thetownproject.shared_resources.DataPacket;

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
     * {@code Socket} used for Input and Output streams
     */
    private Socket connection;

    /**
     * Specifies the hostname to connect to (Server IP)
     */

    //10.0.2.2 - is the IP for the external localhost, because the VIRTUAL MACHINE of the android device
    //already is the 127.0.0.1 - the external IP to the "real" localhost is 10.0.2.2
    //For GENYMOTION - use 10.0.3.2
    private String hostname = "10.0.3.2";

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
     * Retry the connection to the server
     * @throws java.io.IOException
     */
    public void startConnection() throws IOException {
        connection = new Socket(hostname, port);

        //wait 30 seconds for the connection to respond
        connection.setSoTimeout(2500);
        out = new ObjectOutputStream(connection.getOutputStream());
        in = new ObjectInputStream(connection.getInputStream());
        Log.i(this.getClass().getName(), "Socket connection successful " +
                this.hostname+ ":" + this.port);
        connection.setSoTimeout(0);
    }

    public void sendDataPacket(DataPacket dp) throws IOException {
        this.getOutput().writeObject(dp.toJson());
        Log.i(this.getClass().getName(), "DataPacket sent: " + dp.toString());
    }

    public DataPacket receiveDataPacket() throws IOException, ClassNotFoundException {
        DataPacket dp = new DataPacket();
        dp = dp.fromJson((String)this.getInput().readObject());
        Log.i(this.getClass().getName(), "DataPacket received: " + dp.toString());
        return dp;
    }

    /**
     * Close all connections and alert the server of it
     */
    public void exit() throws IOException {
        DataPacket dp = new DataPacket();
        dp.setCommand(Commands.DISCONNECT);
        out.writeObject(dp.toJson());
        out.close();
        in.close();
        connection.close();
        Log.i(this.getClass().getName(), "Socket connection was closed");
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