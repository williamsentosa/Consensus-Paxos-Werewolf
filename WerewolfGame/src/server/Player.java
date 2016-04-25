/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.Socket;

/**
 *
 * @author William Sentosa
 */
public class Player {
    private int playerId;
    private int isAlive;
    private String address;
    private int port;
    private String username;
    private Socket socket;
    
    public Player() {
        playerId = -1;
        isAlive = 0;
        address = "";
        port = 0;
        username = "";
    }
    
    public Player(int playerId, String address, int port, String username, Socket socket) {
        this.playerId = playerId;
        this.address = address;
        this.port = port;
        this.username = username;
        this.socket = socket;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isAlive() {
        return isAlive == 1;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
    
    public void setAlive() {
        isAlive = 1;
    }
    
    public void setNotAlive() {
        isAlive = 0;
    }
    
    public int getAlive() {
        return isAlive;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public boolean isExist(String username) {
        return this.username.compareTo(username) == 0;
    }
    
    @Override
    public String toString() {
        String result = "";
        result = result + playerId + " " + isAlive + " " + address + " " + port + " " + username;
        return result;
    }
}
