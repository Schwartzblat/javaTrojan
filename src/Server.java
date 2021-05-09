import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Server {
    public Socket socket = null;
    public ServerSocket server = null;

    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client...");
            Runnable connectionHandler;
            while (true) {
                socket = server.accept();
                connectionHandler =  new connectionHandler(socket);
                new Thread(connectionHandler).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String [] args){
        Server server = new Server(444);
    }


}
class connectionHandler implements Runnable {
    public Socket socket;
    public DataInputStream output = null;
    public DataOutputStream sender = null;
    public String commandOutput =null;
    public String command = "";
    private int timeout;
    public connectionHandler(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try {
            sender = new DataOutputStream(socket.getOutputStream());
            output = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(Exception e){
            e.printStackTrace();
            return;
        };
        String line = "";
        timeout = 0;
        long start;
        while (!line.equals("OVER")) {
            try {
                line = output.readUTF();
                commandOutput = null;
                command = line.split(" timeout ")[0];
                if (line.split(" timeout ").length > 1) {
                    timeout = Integer.parseInt(line.split("timeout ")[1]);
                } else {
                    timeout = 50;
                }
                start = System.nanoTime();
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        commandOutput = execCmd(command);
                        timeout=0;
                    }
                });
                thread.start();
                while (!thread.isInterrupted() && commandOutput==null) {
                    if ((System.nanoTime() - start) / 1000000000 > timeout) {
                        thread.interrupt();
                        break;
                    }
                    else if(!socket.isConnected()){
                        break;
                    }
                    TimeUnit.SECONDS.sleep(1);
                }
                if (commandOutput != null) {
                    sender.writeUTF(commandOutput);
                } else {
                    sender.writeUTF("done");
                }

            } catch (Exception e) {
                try {
                    sender.writeUTF(e.toString());
                }catch (Exception ignored){}
            }
        }
        try {
            System.out.println("closing connection");
            socket.close();
            output.close();
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public String execCmd(String cmd) {
        cmd = "cmd.exe /c "+cmd;
        String result = null;
        try (InputStream inputStream = Runtime.getRuntime().exec(cmd).getInputStream();
             Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            result = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            return e.toString();
        }
        return result;
    }
}




