package ServerAndChat;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException{
        synchronized (out){
            out.writeObject(message);
        }
    }

    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }

    public Message receive() throws IOException, ClassNotFoundException{
        Message message = null;
        synchronized (in){
            return message = (Message) in.readObject();
        }

    }

    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
