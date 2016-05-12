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
    private String role;
    private String udp_addr;
    private int udp_port;
    
    public Player() {
        playerId = -1;
        isAlive = 0;
        address = "";
        port = 0;
        username = "";
        role = "";
        isAlive = 0;
    }
    
    
    public Player(int playerId, String address, int port, String username, Socket socket, String udp_addr, int udp_port) {
        this.playerId = playerId;
        this.address = address;
        this.port = port;
        this.username = username;
        this.socket = socket;
        this.udp_addr = udp_addr;
        this.udp_port = udp_port;
        isAlive = 0;
        role = "";
    }
    
    public Player(int playerId, int isAlive, String udp_addr, int udp_port, String username, String role) {
        this.playerId = playerId;
        this.isAlive = isAlive;
        this.udp_addr = udp_addr;
        this.udp_port = udp_port;
        this.username = username;
        this.role = role;
    }
    
    public Player copy() {
        Player p = this;
        Player result = new Player(p.playerId, p.address, p.port, p.username, p.socket, p.udp_addr, p.udp_port);
        result.isAlive = p.isAlive;
        result.role = p.role;
        return result;
    }
    
    public Socket getSocket() {
        return socket;
    }
    
    public String getRole() {
        return role;
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
    
    public int getUdpPort() {
        return this.udp_port;
    }
    
    public String getUdpAddress() {
        return this.udp_addr;
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
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isExist(String username) {
        return this.username.compareTo(username) == 0;
    }
    
    public void reset() {
        playerId = -1;
        isAlive = 0;
        port = 0;
        username = "";
        role = "";
        isAlive = 0;
    }
    
    @Override
    public String toString() {
        return playerId + " " + role + " " + isAlive + " " + address + " " + port + " " + username + " " + udp_addr + " " + udp_port;
    }
}
