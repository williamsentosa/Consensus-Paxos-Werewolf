/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author William Sentosa
 */
public class ServerThread implements Runnable {
    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;
    private ArrayList<Player> players;
    private Player player;
    private Server parent;
    
    public ServerThread(Socket clientSocket, ArrayList<Player> players, Server server) {
        this.clientSocket = clientSocket;
        this.players = players;
        this.parent = server;
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
            if (player != null) {
                System.out.println(player.getUsername() + " has been disconnected");
                players.remove(player);
                player = null;
                if(Server.getNumberPlayerReadied() >= Server.MIN_PLAYERS && players.size() == Server.getNumberPlayerReadied()) {
                    startGame();
                }
            }
        }   
    }
    
    private void requestHandler(String in) {
        System.out.println(in);
        JSONObject request = new JSONObject(in);
        try {
            String method = request.getString("method");
            switch (method) {
                case "join" : joinHandler(request.getString("username"), request.getString("udp_address"), request.getInt("udp_port")); break;
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
    
    private void joinHandler(String username, String udpAddr, int udpPort) {
        boolean exist = isPlayerExist(username);
        JSONObject response = new JSONObject();
        if (Server.isGameStarted()) {
            response.put("status", "fail");
            response.put("description", "please wait, game is currently running");
        } else {
            if (exist) {
                response.put("status", "fail");
                response.put("description", "user exists");
            } else {
                int id = Server.getCurrentIdPlayer();
                String addr = clientSocket.getInetAddress().getHostAddress();
                int port = udpPort;
                player = new Player(id, addr, port, username, clientSocket, udpAddr, udpPort);
                System.out.println(player);
                Server.incrCurrentIdPlayer();
                players.add(player);
                System.out.println(username + " has joined.");
                response.put("status", "ok");
                response.put("player_id", id);
            }
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
        if(Server.getNumberPlayerReadied() >= Server.MIN_PLAYERS && players.size() == Server.getNumberPlayerReadied()) {
            startGame();
        }
    }
    
    private void readyUpHandler() {
        JSONObject response = new JSONObject();
        if(player == null) {
            response.put("status", "fail");
            response.put("description", "You haven't join the game");
        } else {
            Server.incrNumberPlayerReadied();
            response.put("status", "ok");
            response.put("description", "waiting for other player to start");
        }
        send(response.toString());
        if(Server.getNumberPlayerReadied() >= Server.MIN_PLAYERS && players.size() == Server.getNumberPlayerReadied()) {
            startGame();
        }
    }
    
    private void startGame() {
        Random random = new Random();
        int firstWolf = 0 + (int)(Math.random() * players.size()); 
        int secondWolf = 0 + (int)(Math.random() * players.size());
        while (secondWolf == firstWolf) {
            secondWolf = 0 + (int)(Math.random() * players.size());
        }
        for(int i=0; i<players.size(); i++) {
            players.get(i).setAlive();
            JSONObject response = new JSONObject();
            response.put("method", "start");
            response.put("time", "day");
            if(i != firstWolf && i != secondWolf) {
                response.put("role", "civilian");
                JSONArray arr = new JSONArray();
                response.put("friend", arr);
                players.get(i).setRole("civilian");
            } else {
                response.put("role", "werewolf");
                JSONArray arr = new JSONArray();
                arr.put(players.get(firstWolf).getUsername());
                arr.put(players.get(secondWolf).getUsername());
                response.put("friend", arr);
                players.get(i).setRole("werewolf");
            }
            response.put("description", "game has started");
            send(players.get(i).getSocket(), response.toString());
        }
    }
    
    private void listClientHandler() {
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        JSONArray arr = new JSONArray();
        for(Player p : players) {
            JSONObject obj = new JSONObject();
            obj.put("player_id", p.getPlayerId());
            obj.put("is_alive", p.getAlive());
            obj.put("address", p.getUdpAddress());
            obj.put("port", p.getUdpPort());
            obj.put("username", p.getUsername());
            if(p.getAlive() == 0) {
                obj.put("role", p.getRole());
            }
            arr.put(obj);
        }
        response.put("clients", arr);
        response.put("description", "list of clients retrieved");
        send(response.toString());
    }
    
    private void clientAcceptProposalHandler(int leaderId) {
        parent.setCurrentLeaderId(leaderId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("description", "");
    }
    
    private void send(Socket socket, String msg) {
        try {
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void gameOver(String winner) {
        Server.changeGameStarted(false);
        for(Player p : players) {
            JSONObject response = new JSONObject();
            response.put("method", "game_over");
            response.put("winner", winner);
            response.put("description", "game has been won by " + winner);
            send(p.getSocket(), response.toString());
        }
    }
}
