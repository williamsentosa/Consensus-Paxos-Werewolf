/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author William Sentosa
 */
public class Client {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String ipAddr;
    private int port;
    private Scanner scanner;
    private int playerId = -1;
    
    public Client() {
        scanner = new Scanner(System.in);
    }
    
    public Client(String ipAddr, int port) {
        scanner = new Scanner(System.in);
        this.connect(ipAddr, port);
    }
    
    public void connect(String ipAddr, int port) {
        this.ipAddr = ipAddr;
        this.port = port;
        try {
            socket = new Socket(ipAddr, port);   
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void run() {
        String request = "";
        String command = "";
        while (true) {
            System.out.print("Input Command : ");
            command = scanner.nextLine();
            switch (command) {
                case "join game" : joinCommand(); break;
                case "leave game" : leaveCommand(); break;
                case "ready up" : readyUpCommand(); break;
                case "list client" : listClientCommand(); break;
            }
        }
    }
    
    private void joinCommand() {
        System.out.print("Input username : ");
        String username = scanner.nextLine();
        JSONObject request = new JSONObject();
        request.put("method", "join");
        request.put("username", username);
        send(request.toString());
        try {
            String input = in.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    playerId = response.getInt("player_id");
                    System.out.println("Your player id is " + playerId + ".");
                    break;
                case "fail" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void leaveCommand() {
        JSONObject request = new JSONObject();
        request.put("method", "leave");
        send(request.toString());
        try {
            String input = in.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println("You have successfully leave the game.");
                    break;
                case "fail" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readyUpCommand() {
        JSONObject request = new JSONObject();
        request.put("method", "ready");
        send(request.toString());
        try {
            String input = in.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println(response.getString("description"));
                    input = in.readUTF();
                    System.out.println(input);
                    startPlaying();
                    break;
                case "error" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void startPlaying() {
        System.out.println("Playing...");
        // Isi dengan perintah bermain
    }
    
    private void listClientCommand() {
        JSONObject request = new JSONObject();
        request.put("method", "client_address");
        send(request.toString());
        try {
            String input = in.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println(response.get("clients"));
                    break;
                case "fail" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[]) {
        String ipAddr = "127.0.0.1";
        int port = 8080;
        Client client = new Client(ipAddr, port);
        System.out.println("Connected to " + ipAddr + " port " + port + "...");
        client.run();
    }
}