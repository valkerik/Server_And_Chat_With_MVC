package ServerAndChat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message){
        for (Map.Entry<String, Connection> arr: connectionMap.entrySet()) {
            try {
                arr.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не смогли оправить сообщение");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт сервера");
        int port = ConsoleHelper.readInt();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Произошла ошибка при запуске или работе сервера.");
        }
    }


    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            while (true){
                connection.send(new Message(MessageType.NAME_REQUEST,"Введите имя пользователя"));

                Message message = connection.receive();

                if(message.getType() != MessageType.USER_NAME){
                    ConsoleHelper.writeMessage("Полученно сообщение от :" + socket.getRemoteSocketAddress() + ". Тип сообщения не соответствует протоколу." );
                    continue;
                }
                String userName = message.getData();

                if(userName.isEmpty()){
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с пустым именем от " + socket.getRemoteSocketAddress());
                    continue;
                }

                if(connectionMap.containsKey(userName)){
                    ConsoleHelper.writeMessage("Попытка подключения к серверу с уже используемым именем от " + socket.getRemoteSocketAddress());
                    continue;
                }
                connectionMap.put(userName,connection);

                connection.send(new Message(MessageType.NAME_ACCEPTED, "Подключение выполненно " + userName ));
                return userName;
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            String user = null;
            for (Map.Entry<String, Connection> arr : connectionMap.entrySet()) {
                user = arr.getKey();
                if(!(userName.equals(user))){
                    connection.send(new Message(MessageType.USER_ADDED, user));
                }

            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message message =  connection.receive();
                if(message.getType() == MessageType.TEXT){
                    String data = message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
                } else {
                    ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + ". Тип сообщения не соответствует протоколу.");

                }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("Было установленно соединение с " + socket.getRemoteSocketAddress());
            String userName = null;

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                notifyUsers(connection, userName);

                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом.");
            }


            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто.");
        }
    }
}
