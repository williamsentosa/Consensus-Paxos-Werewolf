/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author William Sentosa
 */
public class Client {
    private Socket socketToServer;
    private String serverIpAddress;
    private int serverPort;
    private DataOutputStream outputToServer;
    private DataInputStream inputFromServer;
    
    private DatagramSocket clientSocket;
    private int udpPort;
    private Thread clientThread;
    private static final int UDP_BYTE_LENGTH = 1024;
    
    private Scanner scanner;
    private int playerId = -1;
    private int previousAcceptedKpuId = -1;
    private static final int MAX_PROPOSER_COUNT = 2;
    private ArrayList<String> friends;
    private String role;
    
    public Client() {
        scanner = new Scanner(System.in);
        role = "";
    }
    
    public Client(String ipAddr, int port, int myPort, int timeout) {
        scanner = new Scanner(System.in);
        role = "";
        this.createServer(myPort, timeout);
        this.connect(ipAddr, port);
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getPreviousAcceptedKpuId() {
        return previousAcceptedKpuId;
    }

    public void setPreviousAcceptedKpuId(int previousAcceptedKpuId) {
        this.previousAcceptedKpuId = previousAcceptedKpuId;
    }
    
    public void createServer(int port, int timeout) {
        this.udpPort = port;
        try {
            clientSocket = new DatagramSocket(this.udpPort);
            clientSocket.setSoTimeout(timeout);
            
            clientThread = new Thread(new ClientThread(this));
            clientThread.start();
            
            System.out.println("Client listening on port " + this.udpPort);
        } catch (SocketException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void connect(String ipAddr, int port) {
        serverIpAddress = ipAddr;
        serverPort = port;
        
        try {
            socketToServer = new Socket(serverIpAddress, serverPort);   
            outputToServer = new DataOutputStream(socketToServer.getOutputStream());
            inputFromServer = new DataInputStream(socketToServer.getInputStream());
            
            System.out.println("Connected to " + serverIpAddress + " port " + serverPort + "...");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToServer(String msg) {
        try {
            outputToServer.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToClient(String msg, String ipAddr, int port) {
        try {
            InetAddress ipAddress = InetAddress.getByName("localhost");
            byte[] outputToClient = new byte[UDP_BYTE_LENGTH];;
            outputToClient = msg.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(outputToClient, outputToClient.length, ipAddress, port);
            UnreliableSender unreliableSender = new UnreliableSender(clientSocket);
            unreliableSender.send(sendPacket);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DatagramPacket receiveFromClient() throws SocketTimeoutException, IOException {
        byte[] inputFromClient = new byte[UDP_BYTE_LENGTH];;
        DatagramPacket receivePacket = new DatagramPacket(inputFromClient, inputFromClient.length);
        clientSocket.receive(receivePacket);
        return receivePacket;
    }
    
    public DatagramPacket clientSendAndReceive(String msg, String ipAddr, int port) {
        boolean isReceived = false;
        while (!isReceived) {
            sendToClient(msg, ipAddr, port);
            try {
                DatagramPacket dp = receiveFromClient();
                isReceived = true;
                
                return dp;
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, "Packet lost, resending...");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
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
                case "prepare proposal" : paxosPrepareProposal(); break;
                default: System.out.println("Invalid command");
            }
        }
    }
    
    private void joinCommand() {
        System.out.print("Input username : ");
        String username = scanner.nextLine();
        JSONObject request = new JSONObject();
        request.put("method", "join");
        request.put("username", username);
        request.put("udp_address", socketToServer.getLocalAddress().getHostAddress());
        request.put("udp_port", udpPort);
        sendToServer(request.toString());
        try {
            String input = inputFromServer.readUTF();
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
        sendToServer(request.toString());
        try {
            String input = inputFromServer.readUTF();
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
        sendToServer(request.toString());
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println(response.getString("description"));
                    input = inputFromServer.readUTF();
                    System.out.println(input);
                    response = new JSONObject(input);
                    role = response.getString("role");
                    startPlaying(convertJSONArrayToList(response.getJSONArray("friend")));
                    break;
                case "error" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private ArrayList<String> convertJSONArrayToList(JSONArray arr)  {
        ArrayList<String> result = new ArrayList<String>();
        for(int i=0; i<arr.length(); i++) {
            result.add(arr.getString(i));
        }
        return result;
    }
    
    private void startPlaying(ArrayList<String> friends) {
        System.out.println("Playing...");
        System.out.println("Your role : " + role);
        if(role.compareTo("werewolf") == 0) {
            System.out.println("Your friend are : ");
            for(String s : friends) {
                System.out.println(s);
            }
        }
        boolean finished = false;
        while(!finished) {
            dayPhase();
            waitChangePhase();
            nightPhase();
            waitChangePhase();
        }
        
    }
    
    private void dayPhase() {
        chooseLeader();
        killWerewolfVote();
    }
    
    private void nightPhase() {
        if(role.compareTo("werewolf")  == 0) {
            killCivilianVote();
        }
    }
    
    private void chooseLeader() {
        //
    }
    
    private void killWerewolfVote() {
        
    }
    
    private void waitChangePhase() {
        
    }
    
    private void killCivilianVote() {
        
    }
    
    JSONArray listClientCommand() {
        JSONObject request = new JSONObject();
        request.put("method", "client_address");
        sendToServer(request.toString());
        
        JSONArray returnValue = new JSONArray();
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println(response.getJSONArray("clients"));
                    return response.getJSONArray("clients");
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
        return returnValue;
    }
    
    private void paxosPrepareProposal() {
        clientThread.interrupt();
        while (clientThread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        int greaterPlayerIdCount = 0;
        JSONArray clients = listClientCommand();
        
        // TODO: Does this quorum count really applies?
        int quorumCount = clients.length() - 1;
        
        String[] addresses = new String[quorumCount];
        int[] ports = new int[quorumCount];
        int c = 0;
        
        /* checks if client is a valid proposer: Yang bisa jadi proposer cuman pid dua terbesar (player ke n dan n-1) */
        for (int i = 0; i < clients.length() && greaterPlayerIdCount < MAX_PROPOSER_COUNT; i++) {
            JSONObject client = clients.getJSONObject(i);
            if (client.getInt("player_id") != playerId) {
                if (client.getInt("player_id") > playerId) {
                    greaterPlayerIdCount++;
                }

                addresses[c] = (client.getString("address"));
                ports[c] = (client.getInt("port"));
                c++;
            }
        }
        
        if (greaterPlayerIdCount >= MAX_PROPOSER_COUNT) {
            System.err.println("You are unable to be a proposer");
            return;
        }
        
        
        /* send request */
        // TODO: proposal_id is waiting perfect specification
        JSONObject request = new JSONObject();
        request.put("method", "prepare_proposal");
        request.put("proposal_id", "(" + (++previousAcceptedKpuId) + ", " + playerId + ")");
        
        int latestAcceptedKpuId = -1;
        for (int i = 0; i < addresses.length; i++) {
            DatagramPacket dp = clientSendAndReceive(request.toString(), addresses[i], ports[i]);
            String responseString = new String(dp.getData());
            JSONObject response = new JSONObject(responseString);
            String status = response.getString("status");

            switch (status) {
                case "ok":
                    int prevAcceptedKpuId = response.getInt("previous_accepted");
                    System.out.println("Accepted, " + prevAcceptedKpuId);

                    if (latestAcceptedKpuId < prevAcceptedKpuId) {
                        prevAcceptedKpuId = latestAcceptedKpuId;
                    }

                    break;
                case "fail":
                    System.out.println("Failed, " + response.get("description"));
                    break;
                case "error":
                    System.out.println("Error, " + response.get("description"));
                    break;
            }
        }
        
        clientThread = new Thread(new ClientThread(this));
        clientThread.start();
    }
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String serverIpAddress = "127.0.0.1";
        int serverPort = 8080;
        System.out.print("Input yout port : ");
        int myPort = in.nextInt();
        int timeout = 1 * 1000; // 5 seconds
        Client client = new Client(serverIpAddress, serverPort, myPort, timeout);
        client.run();
    }
}