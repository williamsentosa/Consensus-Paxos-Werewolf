/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author William Sentosa
 */
public class Server {
    private int port;
    private int timeout;
    private ServerSocket serverSocket;
    
    private static int currentIdPlayer = 0;
    private ArrayList<Player> players;
    private static int numberPlayerReadied = 0;
    public static final int MIN_PLAYERS = 4;
    private static boolean isGameStarted = false;
    private int currentLeaderId = -1;
    private ArrayList<Integer> leaderVotes;
    private static String currentPhase = "day";
    private static int days = 0;
    
    public Server(int port, int timeout) {
        this.port = port;
        this.timeout = timeout;
        leaderVotes = new ArrayList<>();
        players = new ArrayList<>();
    }
    
    public static int getCurrentIdPlayer() {
        return currentIdPlayer;
    }
    
    public static void resetCurrentIdPlayer() {
        currentIdPlayer = 0;
    }
    
    public static void incrCurrentIdPlayer() {
        currentIdPlayer++;
    }
    
    public static void incrNumberPlayerReadied() {
        numberPlayerReadied++;
    }
    
    public static void resetNumberPlayerReadied() {
        numberPlayerReadied = 0;
    }
    
    public static int getNumberPlayerReadied() {
        return numberPlayerReadied;
    }
    
    public static boolean isGameStarted() {
        return isGameStarted;
    }
    
    public static void changeGameStarted(boolean value) {
        isGameStarted = value;
    }
    
    public static String getCurrentPhase() {
        return currentPhase;
    }
    
    public static void changeCurrentPhase(String phase) {
        currentPhase = phase;
    }
    
    public static void incrDays() {
        days++;
    }
    
    public static int getDays() {
        return days;
    }
    
    public int getCurrentLeaderId() {
        return currentLeaderId;
    }
    
    public void setCurrentLeaderId(int currentLeaderId) {
        this.currentLeaderId = currentLeaderId;
    }
    
    public ArrayList<Integer> getLeaderVotes() {
        return leaderVotes;
    }
    
    public void setLeaderVotes(ArrayList<Integer> leaderVotes) {
        this.leaderVotes = leaderVotes;
    }
            
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(timeout);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ServerThread(clientSocket, players, this)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[]) {
//        Scanner sc = new Scanner(System.in);
        System.out.println("Starting server at port 8080");
        int port = 8080;
        int timeout = 20 * 60 * 1000; // 20 minutes
        Server server = new Server(port, timeout);
        System.out.println("Running server at port " + port + " ...");
        server.run();
    }
}