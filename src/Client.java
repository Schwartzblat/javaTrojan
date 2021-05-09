import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;


    public Client(String address, int port){
        try{
            socket = new Socket(address, port);
            System.out.println("connected to "+address+" in port "+port);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Scanner in = new Scanner(System.in);
        String line = "";
        while(!line.equals("OVER")){
            try {
                System.out.println("Enter a command: ");
                line = in.nextLine();
                output.writeUTF(line);
                System.out.println(input.readUTF());

            }catch(Exception e){
                e.printStackTrace();
            }
        }
        try{
            output.close();
            socket.close();
            in.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String [] args){
        Scanner in = new Scanner(System.in);
        System.out.print("Enter the IP address: ");
        String ip = in.nextLine();
        System.out.print("Enter the port number: ");
        int port = in.nextInt();
        Client client = new Client(ip, port);
    }


}





