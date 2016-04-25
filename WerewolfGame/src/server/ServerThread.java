/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author William Sentosa
 */
public class ServerThread implements Runnable {
    private Socket clientSocket;
    private ArrayList<Socket> clientSockets;
    private DataOutputStream out;
    private DataInputStream in;
    private ArrayList<Player> players;
    private Player player;
    
    public ServerThread(Socket clientSocket, ArrayList<Socket> clientSockets, ArrayList<Player> players) {
        this.clientSocket = clientSocket;
        this.clientSockets = clientSockets;
        this.players = players;
        try {
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        String request = "";
        try {
            while(true) {
                request = in.readUTF();
                requestHandler(request);
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    private void requestHandler(String in) {
        System.out.println(in);
        JSONObject request = new JSONObject(in);
        try {
            String method = request.getString("method");
            switch (method) {
                case "join" : joinHandler(request.getString("username")); break;
                case "leave" : leaveHandler(); break;
                case "ready" : readyUpHandler(); break;
                case "client_address" : listClientHandler(); break;
            }
        } catch (JSONException ex) {
            errorHandler();
        }
        
    }
    
    private void errorHandler() {
        JSONObject response = new JSONObject();
        response.put("status", "error");
        response.put("description", "wrong request");
        send(response.toString());
    }
    
    private void joinHandler(String username) {
        boolean exist = isPlayerExist(username);
        JSONObject response = new JSONObject();
        if (exist) {
            response.put("status", "fail");
            response.put("description", "user exists");
        } else {
            int id = Server.getCurrentIdPlayer();
            String addr = clientSocket.getInetAddress().getHostAddress();
            int port = clientSocket.getPort();
            player = new Player(id, addr, port, username);
            System.out.println(player);
            Server.incrCurrentIdPlayer();
            players.add(player);
            System.out.println(username + " has joined.");
            response.put("status", "ok");
            response.put("player_id", id);
        }
        send(response.toString());
    }
    
    private boolean isPlayerExist(String username) {
        boolean exist = false;
        for(Player p : players) {
            if(p.isExist(username)) {
                exist = true;
                break;
            }
        }
        return exist;
    }
    
    private void leaveHandler() {
        JSONObject response = new JSONObject();
        if(player == null) {
            response.put("status", "fail");
            response.put("description", "You haven't join the game");
        } else {
            players.remove(player);
            player = null;
            response.put("status", "ok");
        }
        send(response.toString());
    }
    
    private void readyUpHandler() {
        System.out.println("readyup");
    }
    
    private void listClientHandler() {
        System.out.println("list client");
    }
    
    private void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
