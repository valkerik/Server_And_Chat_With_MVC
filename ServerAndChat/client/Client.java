package ServerAndChat.client;

import java.io.IOException;
import java.net.Socket;

import ServerAndChat.Connection;
import ServerAndChat.ConsoleHelper;
import ServerAndChat.Message;
import ServerAndChat.MessageType;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите ваше имя:");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        try {
            synchronized (this) {

                wait();
            }

        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Произошла ошибка");
            return;
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected) {
            String s = ConsoleHelper.readString();
            if (s.equalsIgnoreCase("exit")) {
                break;
            }
            if (shouldSendTextFromConsole()) {
                sendTextMessage(s);
            }
        }
    }


    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }


    public class SocketThread extends Thread {

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник с именем " + userName + " присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("Участник с именем " + userName + " покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{

            while (true){
                Message message =  connection.receive();

                if(message.getType() == MessageType.NAME_REQUEST){
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME,userName));
                }
                else if(message.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();

                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                }
                else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        @Override
        public void run() {
            try {
                connection = new Connection(new Socket(getServerAddress(),getServerPort()));
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }



        }
    }
}
