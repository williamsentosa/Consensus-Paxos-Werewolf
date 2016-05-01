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
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import server.Player;

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
    private static final int MAX_RETRY = 10;
    private static final int UDP_BYTE_LENGTH = 1024;
    
    private Scanner scanner;
    private int playerId = -1;
    private int previousAcceptedKpuId = 0;
    private int previousProposerId = -1;
    private static final int MAX_PROPOSER_COUNT = 2;
    
    private ArrayList<String> friends;
    private String role;
    private ArrayList<Player> players;
    
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

    public int getPreviousProposerId() {
        return previousProposerId;
    }

    public void setPreviousProposerId(int previousProposerId) {
        this.previousProposerId = previousProposerId;
    }    
    
    public void createServer(int port, int timeout) {
        this.udpPort = port;
        try {
            clientSocket = new DatagramSocket(this.udpPort);
            clientSocket.setSoTimeout(timeout);
            
            startClientThread();
            
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
            clientSocket.send(sendPacket);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendToClient(String msg, String ipAddr, int port, boolean isUnreliable) {
        try {
            InetAddress ipAddress = InetAddress.getByName("localhost");
            byte[] outputToClient = new byte[UDP_BYTE_LENGTH];;
            outputToClient = msg.getBytes();
            
            DatagramPacket sendPacket = new DatagramPacket(outputToClient, outputToClient.length, ipAddress, port);
            if (isUnreliable) {
                UnreliableSender unreliableSender = new UnreliableSender(clientSocket);
                unreliableSender.send(sendPacket);
            } else {
                clientSocket.send(sendPacket);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public DatagramPacket receiveFromClient() throws SocketTimeoutException, IOException {
        byte[] inputFromClient = new byte[UDP_BYTE_LENGTH];
        DatagramPacket receivePacket = new DatagramPacket(inputFromClient, inputFromClient.length);
        clientSocket.receive(receivePacket);
        return receivePacket;
    }
    
    public DatagramPacket clientSendAndReceive(String msg, String ipAddr, int port) {
        System.out.println("Ip addr : " + ipAddr);
        System.out.println("port : " + port);
        boolean isReceived = false;
        int retryCount = 0;
        while (!isReceived && retryCount < MAX_RETRY) {
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
            retryCount++;
        }
        return null;
    }
    
    
    public DatagramPacket clientSendAndReceive(String msg, String ipAddr, int port, boolean isUnreliable) {
        System.out.println("Ip addr : " + ipAddr);
        System.out.println("port : " + port);
        
        boolean isReceived = false;
        int retryCount = 0;
        
        while (!isReceived && retryCount < MAX_RETRY) {
            sendToClient(msg, ipAddr, port, isUnreliable);
            
            try {
                DatagramPacket dp = receiveFromClient();
                isReceived = true;
                
                return dp;
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, "Packet lost, resending...");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            retryCount++;
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
                default: System.out.println("Invalid command");
            }
        }
    }
    
    private Player findPlayer(int player_id, List<Player> Clients){
        for(Player client : Clients){
            if(client.getPlayerId() == player_id) {
                return client;
            }
        }
        return null;
    }
    
    private List<Player> numberAlive(){
        List<Player> result = new ArrayList<Player>();
        for(Player client : players) {
            if(client.getAlive()==1) 
                result.add(client);
        }
        return result;
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
            this.getListClient();
            System.out.println("Entering day Phase");
            dayPhase();
            System.out.println("Entering night Phase");
            waitResponseFromServer();
            this.getListClient();
            nightPhase();
            waitResponseFromServer();
        }
    }
    
    private void dayPhase() {
        //chooseLeader();
        waitResponseFromServer();
        // waitForVote();
        // --> paxosPrepareProposal
        // --> paxosAcceptProposal
        // clientAcceptProposal  --> called in ClientThread
        // killWerewolfVote      --> called in ClientThread
    }
    
    private void waitResponseFromServer() {
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            responseHandler(response);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void nightPhase() {
        if(role.compareTo("werewolf")  == 0) {
            waitResponseFromServer();
        }
    }
    
    private void responseHandler(JSONObject response){
        String method = response.getString("method");
        switch(method){
            case "change_phase" :
                changePhase(response);
                break;
            case "game_over" :
                gameOver(response);
                break;
            case "vote_now" :
                String phase = response.getString("phase");
                if(phase.equals("day")) {
                    System.out.println("*** Kill Werewolf ***");
                    showAvailablePlayer();
                    System.out.print("Input username yang ingin dibunuh : ");
                    String username = scanner.nextLine();
                    int kpuId = getCurrentLeaderId();
                    killWerewolfVote(kpuId, username);
                } else {
                    System.out.println("*** Kill Civilian ***");
                    System.out.print("Input username yang ingin dibunuh : ");
                    String username = scanner.nextLine();
                    int kpuId = getCurrentLeaderId();
                    killCivilianVote(kpuId, username);
                }
                break;
        }
    }
    
    private void showAvailablePlayer() {
        for(Player p : players) {
            System.out.println(p.getUsername());
        }
    }
    
    private void changePhase(JSONObject response) {
        System.out.println("time : "+ response.getString("time"));
        System.out.println("days : "+ response.getInt("days"));
        System.out.println("description : "+ response.getString("description"));
    }
    
    private void gameOver(JSONObject response){
        System.out.println("winner : "+ response.getString("winner"));
        System.out.println("description : "+ response.getString("description"));
    }
    
    
    
    private void chooseLeader() {
        if (isLeader()) {
            if (paxosPrepareProposal(players) == 1) {
                paxosAcceptProposal(players);
            }
        }
    }
    
    public void killWerewolfVote(int kpuId, String username) {
        int playerId;
        Player client = this.findPlayer(kpuId, players);
        for(Player p : players) {
            if(p.getUsername().compareTo(username) == 0){
                playerId = p.getPlayerId();
            }
        }
        JSONObject request = new JSONObject();
        request.put("method", "vote_werewolf");
        request.put("player_id", client.getPlayerId());
        
        DatagramPacket dp = clientSendAndReceive(request.toString(), client.getUdpAddress(), client.getUdpPort());
        String responseString = new String(dp.getData());
        JSONObject response = new JSONObject(responseString);
        String status = response.getString("status");
        
        switch(status){
            case "ok": 
                break;
            case "fail":
                break;
            case "error":
                break;
        }
    }
    private int majorityVote(HashMap<Integer, Integer> vote){
        int maxId=-1;
        int max=0;
        for(int i=0; i<players.size(); i++){
            if(vote.containsKey(i) && (max < vote.get(i))){
                max = vote.get(i);
                maxId= i;
            }
        }
        return maxId;
    }
    private void infoWerewolfKilled(HashMap<Integer,Integer> vote){
        int kill=-1;
        int playerId = majorityVote(vote);
        List<Player> player = numberAlive();
        int[][] voteResult = new int[players.size()][2];
        for(int i=0; i<players.size(); i++) {
            voteResult[i][0]=i;
            if(vote.containsKey(i)){
                voteResult[i][1]=vote.get(i);
            }else {
                voteResult[i][1]=0;
            }
            
        }
        int quorum = player.size()/2 ;
        if(vote.get(playerId)> quorum)
            kill=1;
        //send to server
        JSONObject request = new JSONObject();
        if(kill==1) {
            request.put("method", "vote_result_werewolf");
            request.put("player_killed",playerId);
            
        }else {
            request.put("method", "vote_result");
        }
        request.put("vote_status", kill);
        request.put("vote_result", voteResult);
        sendToServer(request.toString());
        
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            
            switch(status){
                case "ok":
                    break;
                case "fail":
                    break;
                case "error":
                    break;
            }
            
        }catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int numberWerewolf(List<Player> players){
        int count = 0;
        for(Player player : players){
            if(player.getRole().equalsIgnoreCase("werewolf"))
              count++;  
        }
        return count;
    }
    
    private void infoCivilianKilled(HashMap<Integer,Integer> vote){
        int kill =-1;
        int playerId = majorityVote(vote);
        List<Player> client = numberAlive();
        int[][] voteResult = new int[players.size()][2];
        for(int i=0; i<players.size(); i++) {
            voteResult[i][0]=i;
            if(vote.containsKey(i)){
                voteResult[i][1]=vote.get(i);
            }else {
                voteResult[i][1]=0;
            }
            
        }
        //send to server
        JSONObject request = new JSONObject();
        if(kill==1) {
            request.put("method", "vote_result_civilian");
            request.put("player_killed",99);
            
        }else {
            request.put("method", "vote_result");
        }
        request.put("vote_status", kill);
        request.put("vote_result", voteResult);
        sendToServer(request.toString());
        
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            
            switch(status){
                case "ok":
                    break;
                case "fail":
                    break;
                case "error":
                    break;
            }
            
        }catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void infoKilled() {
        int kill = -1;
        String method="";
        HashMap<Integer, Integer> hmap = new HashMap<Integer, Integer>();
        List<Player> player = numberAlive();
        for(int i=0; i<numberAlive().size(); i++) {
            try {
                DatagramPacket dp = receiveFromClient();
                String responseString = new String(dp.getData());
                JSONObject response = new JSONObject(responseString);
                method = response.getString("method");
                int temp = response.getInt("player_id");
                if(hmap.containsKey(temp))
                    hmap.put(temp, hmap.get(temp)+1);
                else hmap.put(temp,1);
            } catch (SocketTimeoutException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.WARNING, "Packet lost, resending...");
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        switch(method){
            case "vote_werewolf" : 
                infoWerewolfKilled(hmap);
                break;
            case "vote_civilian" : 
                infoCivilianKilled(hmap);
            break;
        }
       
    }
 
    
    private void killCivilianVote(int kpuId, String username) {
        Player client = findPlayer(kpuId, players);
        JSONObject request = new JSONObject();
        request.put("method", "vote_civilian");
        int playerId = 0;
        for(Player p : players) {
            if(p.getUsername().compareTo(username) == 0) {
                playerId = p.getPlayerId();
            }
        }
        request.put("player_id", playerId);
        
        DatagramPacket dp = clientSendAndReceive(request.toString(), client.getUdpAddress(), client.getUdpPort());
        String responseString = new String(dp.getData());
        JSONObject response = new JSONObject(responseString);
        String status = response.getString("status");
        
        switch(status){
            case "ok": 
                System.out.println("Ok, " + response.getString("description") + ".");
                break;
            case "fail":
                System.out.println("Failed, " + response.getString("description") + ".");
                break;
            case "error":
                System.out.println("Error, " + response.getString("description") + ".");
                break;
        }
    }
    
    private void getListClient() {
        players = new ArrayList<>();
        JSONObject request = new JSONObject();
        request.put("method", "client_address");
        sendToServer(request.toString());
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            System.out.println(response.toString());
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println(response.getJSONArray("clients"));
                    JSONArray lineClients = response.getJSONArray("clients");
                    for (Object client : lineClients) {
                        JSONObject jsonLineClient = (JSONObject) client;
                        int playerId = jsonLineClient.getInt("player_id");
                        int isAlive = jsonLineClient.getInt("is_alive");
                        String address = jsonLineClient.getString("address");
                        int port = jsonLineClient.getInt("port");
                        String username = jsonLineClient.getString("username");
                        players.add(new Player(playerId, isAlive, address, port, username));
                    }
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

    
    private boolean isLeader() {
        boolean isLeader;
        int greaterPlayerIdCount = 0;
        int quorumCount = players.size() - 1;
        int c = 0;
        
        /* checks if client is a valid proposer: Yang bisa jadi proposer cuman pid dua terbesar (player ke n dan n-1) */
        for (int i = 0; i < players.size() && greaterPlayerIdCount < MAX_PROPOSER_COUNT; i++) {
            Player player = players.get(i);
            if (player.getPlayerId() != playerId) {
                if (player.getPlayerId() > playerId) {
                    greaterPlayerIdCount++;
                }
                
                c++;
            }
        }
        
        if (greaterPlayerIdCount >= MAX_PROPOSER_COUNT) {
            isLeader = false;
        } else {
            isLeader = true;
        }
        
        return isLeader;
    }
    
    private int paxosPrepareProposal(List<Player> players) {
        // stopClientThread();
        
        int returnValue = -1;
        
        /* send request */
        // TODO: proposal_id is waiting perfect specification
        JSONObject request = new JSONObject();
        JSONArray proposalIdData = new JSONArray();
        proposalIdData.put(++previousAcceptedKpuId);
        proposalIdData.put(playerId);
        request.put("method", "prepare_proposal");
        request.put("proposal_id", proposalIdData);
        
        int latestAcceptedKpuId = -1;
        for (int i = 0; i < players.size(); i++) {
            
            Player client = players.get(i);
            String address = client.getUdpAddress();
            int port = client.getUdpPort();
            
            DatagramPacket dp = clientSendAndReceive(request.toString(), address, port, true);
            String responseString = new String(dp.getData());
            JSONObject response = new JSONObject(responseString);
            String status = response.getString("status");

            switch (status) {
                case "ok":
                    int prevAcceptedKpuId = response.getInt("previous_accepted");
                    System.out.println("Accepted, " + prevAcceptedKpuId);

                    if (prevAcceptedKpuId > latestAcceptedKpuId) {
                        latestAcceptedKpuId = prevAcceptedKpuId;
                    }
                    
                    previousAcceptedKpuId = latestAcceptedKpuId;
                    returnValue = 1;
                    break;
                case "fail":
                    System.out.println("Failed, " + response.get("description"));
                    returnValue = 0;
                    break;
                case "error":
                    System.out.println("Error, " + response.get("description"));
                    returnValue = -1;
                    break;
            }
        }
        
        // startClientThread();
        return returnValue;
    }
    
    private int paxosAcceptProposal(List<Player> players) {
        // stopClientThread();
        
        int returnValue = -1;
        
        /* send request */
        // TODO: proposal_id is waiting perfect specification
        JSONObject request = new JSONObject();
        JSONArray proposalIdData = new JSONArray();
        proposalIdData.put(++previousAcceptedKpuId);
        proposalIdData.put(playerId);
        
        request.put("method", "accept_proposal");
        request.put("proposal_id", proposalIdData);
        request.put("kpu_id", playerId);
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            DatagramPacket dp = clientSendAndReceive(request.toString(), player.getUdpAddress(), player.getUdpPort(), true);
            String ipAddress = dp.getAddress().getHostAddress();
            
            String responseString = new String(dp.getData());
            JSONObject response = new JSONObject(responseString);
            String status = response.getString("status");

            switch (status) {
                case "ok":
                    System.out.println(ipAddress + ": " + "Accepted");
                    returnValue = 1;
                    break;
                case "fail":
                    System.out.println(ipAddress + ": " + "Failed, " + response.get("description"));
                    returnValue = 0;
                    break;
                case "error":
                    System.out.println(ipAddress + ": " + "Error, " + response.get("description"));
                    returnValue = -1;
                    break;
            }
        }
        
        // startClientThread();
        return returnValue;
    }
    
    public void clientAcceptProposal() {
        JSONObject request = new JSONObject();        
        request.put("method", "prepare_proposal");
        request.put("kpu_id", previousProposerId);
        request.put("description", "kpu is selected");
        
        sendToServer(request.toString());
        
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            String status = response.getString("status");
            switch (status) {
                case "ok" : 
                    System.out.println("Leader vote accepted.");
                    break;
                case "fail" :
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error" :
                    System.out.println("Error, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // TODO: broadcast to all players?
    }
    
    private void startClientThread() {
        clientThread = new Thread(new ClientThread(this));
        clientThread.start();
    }
    
    
    private void stopClientThread() {
        clientThread.interrupt();
        while (clientThread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private int getCurrentLeaderId() {
        return players.get(players.size() - 1).getPlayerId();
    }
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String serverIpAddress = "127.0.0.1";
        int serverPort = 8080;
        System.out.print("Input your port : ");
        int myPort = in.nextInt();
        int timeout = 1 * 1000; // 5 seconds
        Client client = new Client(serverIpAddress, serverPort, myPort, timeout);
        client.run();
    }
}