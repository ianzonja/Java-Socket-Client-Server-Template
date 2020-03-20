/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientserverassignment.clientserverassignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    private static String loggedUsername = ""; //just one static variable that is being populated when user log in (so it is visible in other parts of program just for output)
    
    //main function of the Server class. Here is whole logic of listening on the port happening
    public static void main(String[] args){
        Map<String, String> userAccounts = new HashMap<String, String>(); //in this map we put username and password from file
        userAccounts = getUserDataFromFile(); //here we call the method that will store username and password from file
        boolean isLoggedIn = false;
        System.out.println("My chat room server. Version one.");
        try {
            //in this loop the socket is being initialized and stats to accept the clients
            while (true) {
                int port = 1218;
                ServerSocket serverSocket = new ServerSocket(port); //initialize the serverSocket object
                /*
                The following line initializes socket when server accepts the client
                This happens when client program is started and client started connecting to the port. 
                While there is no client, the program will just wait on this line until it accepts someone.*/
                Socket socket = serverSocket.accept();
                //streams for input and output on the socket
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                //creating the stringbuffer which will be populated with the content from the inputstream
                StringBuffer sb = new StringBuffer();
                int readByte = 0;
                while (readByte != -1) {
                    /*here the input stream reads the stream character by character. When it is finished with reading,
                    readByte will be -1, and while loop will end. Othervise readByte will get some value 
                    which will be then casted into character and appended to stringbuffer*/
                    readByte = is.read();
                    sb.append((char) readByte);
                }
                /*transforming content from stringbuffer to actual string. Notice that we remove \uffff 
                which is some kind of stream termination character (or sth like that). 
                It came from socket stream and is part of standard.*/
                String clientCommand = sb.toString().replace("\uffff", "");
                String response = "";
                //if user is not logged in first check if user sent login command. It is being performed in checkLoginCommand function.
                if(!isLoggedIn){
                    if(!checkForNewUserCommand(clientCommand).equals("")){
                        response = ManageNewUserCommand(checkForNewUserCommand(clientCommand), userAccounts);
                    }
                    else{
                        response = checkLoginCommand(clientCommand, userAccounts);
                        if(response.contains("joins"))
                        isLoggedIn = true;
                    }
                }else{ //if user is already logged in
                    /*check if function that perfoms send command validation returns some response
                    if is returns something instead of "", then it is a valid send command.
                    */
                    if(!checkForSendCommand(clientCommand).equals("")){
                        response = checkForSendCommand(clientCommand); //so, then grab the response, and save it to return to client
                        System.out.println(response); //print it
                    }
                    else if(clientCommand.equals("logout")){ //else if send command returned "" then we check logout command.
                        response = "Server: "+Server.loggedUsername + " left.";//if command is really logout, make a response for it.
                        System.out.println(loggedUsername + ": logout.");
                        isLoggedIn = false;
                    }else //if command was not valid for any of options, then it is unkown command.
                        response = "Command not found";
                }
                os.write(response.getBytes()); //write response to the socket stream.
                os.flush(); //flush the output stream
                socket.shutdownOutput(); //shut down output
                serverSocket.close(); //close the socket (no worries, on the beggining of the while loop it will be opened again)
            }
        }catch (Exception ex) {
            System.out.print(ex.toString());
        }
    }

    //a function that check regex for login command and if it is okay, and the credentials are good, the user log in. Otherwise nope.
    private static String checkLoginCommand(String clientCommand, Map<String, String> userAccounts) {
        Pattern pattern = Pattern.compile("login (.*) (.*)"); //regex for any login command
        Matcher matcher = pattern.matcher(clientCommand); //matching regex pattern with actual client's command.
        if(matcher.matches()){ //if regex matches, we have valid login command.
            String username = matcher.group(1); //we extract username and password from clientsCommand. group 1: (.*) group2: (.*) - each paranthesis is one group
            String password = matcher.group(2);
            if(userAccounts.containsKey(username)){ //check if map with usernames and passwords contains username
                if(userAccounts.get(username).equals(password)){ //now check if value of that map is same like password provided by client
                    String response = "Server: "+username + " joins"; //if yes, user logged in.
                    System.out.println(username + " login.");
                    Server.loggedUsername = username;
                    return response;
                }else{ //good username provided but wrong password - wrong credentials
                    return "Server: Denied, wrong credentials"; 
                }
            }else{ //username doesnt exists - wrong credentials
                return "Server: Denied, wrong credentials";
            }
        }
        return "Server: Denied. Please login first."; //regex for login command didnt passed while user is not logged in.
    }

    //reads file line by line and stores username and password of every user in map object.
    private static Map<String, String> getUserDataFromFile() {
        BufferedReader reader;
        Map<String, String> userAccounts = new HashMap<String, String>();
        try {
            reader = new BufferedReader(new FileReader("accounts.txt")); //opening file content and storing it in the bufferedreader object
            String line = "";
            boolean keepReading = true;
            while (keepReading) { //while there are lines
                line = reader.readLine();
                if(line!=null){ //if line is not null then read that line
                    String[] userData = line.split(",");
                    userAccounts.put(userData[0], userData[1]);
                }else //otherwise keepReading is false and exit the loop
                    keepReading = false;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userAccounts;
    }

    //check regex for send command and if it matches with client's command, then it is a valid send oommand so return a response accordingly.
    private static String checkForSendCommand(String clientCommand) {
        Pattern pattern = Pattern.compile("send (.*)");
        Matcher matcher = pattern.matcher(clientCommand);
        if(matcher.matches()){
            String message = matcher.group(1);
            return Server.loggedUsername+": "+message; //returns a response.
        }
        return "";
    }
    
    //functions that checks regex of the new user command and if it is valid, returns the userId and password in the form that will be good for entering in the file
    private static String checkForNewUserCommand(String clientCommand){
        Pattern pattern = Pattern.compile("newuser (.*) (.*)");
        Matcher matcher = pattern.matcher(clientCommand);
        if(matcher.matches()){
            String userId = matcher.group(1);
            String password = matcher.group(2);
            return userId+","+password;
        }
        return "";
    }
    
    //functions that stores user id and password in the file if it doesnt already exist in file or userId is not less than 32 and password not between 4 and 8 characters. Also returns response accordingly.
    private static String ManageNewUserCommand(String newUserCommand, Map<String, String> userAccounts) {
        String[] credentials = newUserCommand.split(",");
        if(!userAccounts.containsKey(credentials[0])){
            if(credentials[0].length()<32 && credentials[1].length() > 4 && credentials[1].length() < 8){
                userAccounts.put(credentials[0], credentials[1]);
                try{
                    Files.write(Paths.get("accounts.txt"), (newUserCommand+"\n").getBytes(), StandardOpenOption.APPEND);
                }catch(Exception ex){
                    System.err.println(ex.toString());
                }
                return "Server: New user created. Please login.";
            }else
                return "Server: Denied. UserId length is not below 32 characters, or password legth is not between 4 and 8 characters.";
        }else
            return "Server: Denied. UserId already exists.";
    }
}
