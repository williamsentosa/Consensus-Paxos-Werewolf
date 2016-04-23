/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author William Sentosa
 */
public class ServerThread implements Runnable {
    private Socket clientSocket;
    private ArrayList<Socket> clientSockets;

    public ServerThread(Socket clientSocket, ArrayList<Socket> clientSockets) {
        this.clientSocket = clientSocket;
        this.clientSockets = clientSockets;
    }
    
    @Override
    public void run() {
        String request = "";
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            while(true) {
                request = in.readUTF();
                System.out.println(request);
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
}
