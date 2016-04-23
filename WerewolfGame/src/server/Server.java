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
    private ArrayList<Socket> clientSockets;
    
    public Server(int port, int timeout) {
        this.port = port;
        this.timeout = timeout;
        clientSockets = new ArrayList<>();
    }
    
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(timeout);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                new Thread(new ServerThread(clientSocket, clientSockets)).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Input port : ");
        int port = sc.nextInt();
        int timeout = 20 * 60 * 1000; // 20 minutes
        Server server = new Server(port, timeout);
        System.out.println("Running server ...");
        server.run();
    }
}
