/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Ian
 */
public class Client {
    public static void main(String[] args){
        String ipAdress = "127.0.0.1"; //localhost ip (server is on our localhost)
        int port = 1218;
        String command = "";
        Socket socket = null;
        System.out.println("My chat room client. Version one.");
        while(!command.equals("logout")){ //this client connects to the server while he doesnt send logout command.
            try {
                socket = new Socket(ipAdress, port); //initialize the socket
                InputStream is = socket.getInputStream(); //and input stream 
                OutputStream os = socket.getOutputStream(); //and outputstream
                Scanner sc = new Scanner(System.in);
                command = sc.nextLine(); //asks a user to enter the command
                os.write(command.getBytes()); //write the command to the output stream
                os.flush(); //and then flush it
                socket.shutdownOutput(); //and shut down it.
                StringBuffer sb = new StringBuffer(); //create stringbuffer that will be populated with server's response
                int readByte = 0;
                while (readByte != -1) { //this is same like in server side code. 
                    readByte = is.read();//read inputstream while theree are more bytes on the stream.
                    sb.append((char) readByte); //and populate stringbuffer with casted byte into character
                }
                System.out.println(sb.toString().replace("\uffff", ""));
                socket.close();
                is.close();
                os.close();
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
        }
    }
}
