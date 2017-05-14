package touchyou;

import com.lloseng.ocsf.server.AbstractServer;
import com.lloseng.ocsf.server.ConnectionToClient;

import touchyou.util.Controller;

/**
 * A TCP/IP socket server for communication with Android device.
 * 
 * @author Kongpon Charanwattanakit
 *
 */
public class TCPServer extends AbstractServer {

    /**
     * Initialize the server with a given port.
     * 
     * @param port
     *            is the port to listen to
     */
    public TCPServer(int port) {
	super(port);
    }

    @Override
    protected void clientConnected(ConnectionToClient client) {
	super.clientConnected(client);
	System.out.println("Client connected.");
    }

    @Override
    protected synchronized void clientException(ConnectionToClient client, Throwable exception) {
	super.clientException(client, exception);
	exception.printStackTrace();
    }

    @Override
    protected synchronized void clientDisconnected(ConnectionToClient client) {
	super.clientDisconnected(client);
	System.out.println("Client disconnected.");
    }

    @Override
    protected void handleMessageFromClient(Object o, ConnectionToClient client) {
	System.out.println("Client: " + o);
	String msg = (String) o;
	if (msg.contains("SYNC_REQUEST")) {
	    String[] size = msg.split("=")[1].split(";");
	    Controller.getInstance().sync(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
	}
    }

}
