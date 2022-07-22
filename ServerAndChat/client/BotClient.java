package ServerAndChat.client;

import ServerAndChat.ConsoleHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client{
    @Override
    protected String getUserName() {
        String userName = "date_bot_" + (int)  (Math.random()*100);
        return userName;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();

    }

    public class BotSocketThread extends SocketThread{
        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            String [] arr = message.split(": ");
            if (arr.length != 2) return;


            Calendar date = new GregorianCalendar();

            if(arr[1].equalsIgnoreCase("дата")){
                DateFormat  df = new SimpleDateFormat("d.MM.YYYY");
                sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("день")){
                DateFormat  df = new SimpleDateFormat("d");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("месяц")){
                DateFormat  df = new SimpleDateFormat("MMMM");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("год")){
                DateFormat  df = new SimpleDateFormat("YYYY");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("время")){
                DateFormat  df = new SimpleDateFormat("H:mm:ss");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("час")){
                DateFormat  df = new SimpleDateFormat("H");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("минуты")){
                DateFormat  df = new SimpleDateFormat("m");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
            if(arr[1].equalsIgnoreCase("секунды")){
                DateFormat  df = new SimpleDateFormat("s");
                BotClient.this.sendTextMessage("Информация для " + arr[0] + ": " + df.format(date.getTime()));
            }
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
    }
}
