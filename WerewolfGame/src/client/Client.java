/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author William Sentosa
 */
public class Client {
    private Socket socket;
    private DataOutputStream out;
    private String ipAddr;
    private int port;
    
    public Client() {
        // do nothing;
    }
    
    public Client(String ipAddr, int port) {
        this.connect(ipAddr, port);
    }
    
    public void connect(String ipAddr, int port) {
        this.ipAddr = ipAddr;
        this.port = port;
        try {
            socket = new Socket(ipAddr, port);   
            out = new DataOutputStream(socket.getOutputStream());
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
    
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        String ipAddr = "";
        int port = 0;
        System.out.print("Input server ip address : ");
        ipAddr = scanner.nextLine();
        System.out.print("Input server port : ");
        port = scanner.nextInt();
        Client client = new Client(ipAddr, port);
        System.out.println("Connected to " + ipAddr + " port " + port);
        scanner.next();
        String msg = "";
        while(true) {
            msg = scanner.nextLine();
            client.send(msg);
        }
        
    }
}
