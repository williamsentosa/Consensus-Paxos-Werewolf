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
import org.json.JSONException;
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
    private static final int MAX_RETRY = 4;
    private static final int UDP_BYTE_LENGTH = 1024;

    private Scanner scanner;
    private int playerId = -1;
    private int previousAcceptedKpuId = 0;
    private int previousProposerId = -1;
    private static final int MAX_PROPOSER_COUNT = 2;

    private ArrayList<String> friends;
    private String role;
    private ArrayList<Player> players;
    private int currentLeaderId = -1;
    private static boolean isGameOver = false;
    
    
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
            System.out.println("sendToServer(): " + msg);
            outputToServer.writeUTF(msg);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendToClient(String msg, String ipAddr, int port) {
        System.out.println("sendToClient(): " + msg + " to " + ipAddr + ":" + port);
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
                case "join game":
                    joinCommand();
                    break;
                case "leave game":
                    leaveCommand();
                    break;
                case "ready up":
                    readyUpCommand();
                    break;
                default:
                    System.out.println("Invalid command");
            }
        }
    }

    private Player findPlayer(int player_id, List<Player> Clients) {
        for (Player client : Clients) {
            if (client.getPlayerId() == player_id) {
                return client;
            }
        }
        return null;
    }

    private List<Player> getAlivePlayers() {
        List<Player> result = new ArrayList<>();
        for (Player client : players) {
            if (client.getAlive() == 1) {
                result.add(client);
                // System.out.println("getAlivePlayers(): " + client + " is Alive.");
            }
        }
        return result;
    }
    
    private List<Player> getDiedPlayers() {
        List<Player> result = new ArrayList<>();
        for (Player client : players) {
            if (client.getAlive() == 0) {
                result.add(client);
                // System.out.println("getDiedPlayers(): " + client + " is Died.");
            }
        }
        // System.out.println("getDiedPlayers(): " + result.size());
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
                case "ok":
                    playerId = response.getInt("player_id");
                    System.out.println("Your player id is " + playerId + ".");
                    break;
                case "fail":
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error":
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
                case "ok":
                    System.out.println("You have successfully leave the game.");
                    break;
                case "fail":
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error":
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
                case "ok":
                    System.out.println(response.getString("description"));
                    input = inputFromServer.readUTF();
                    System.out.println(input);
                    response = new JSONObject(input);
                    role = response.getString("role");
                    startPlaying(convertJSONArrayToList(response.getJSONArray("friend")));
                    break;
                case "error":
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ArrayList<String> convertJSONArrayToList(JSONArray arr) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < arr.length(); i++) {
            result.add(arr.getString(i));
        }
        return result;
    }

    private void startPlaying(ArrayList<String> friends) {
        System.out.println("Playing...");
        System.out.println("Your role : " + role);
        if (role.compareTo("werewolf") == 0) {
            System.out.println("Your friend are : ");
            for (String s : friends) {
                System.out.println(s);
            }
        }
        isGameOver = false;
        dayPhase();
        while (!isGameOver) {
            waitResponseFromServer();
//            System.out.println("Entering day Phase");
//            dayPhase();
//            waitResponseFromServer();
//            this.getListClient();
//            nightPhase();
//            waitResponseFromServer();
        }

    }

    private void dayPhase() {
        System.out.println("*** Entering day phase ***");
        getListClient();
        chooseLeader();
//        waitResponseFromServer();
        // waitForVote();
        // --> paxosPrepareProposal
        // --> paxosAcceptProposal
        // clientAcceptProposal  --> called in ClientThread
        // killWerewolfVote      --> called in ClientThread
    }

    private void waitResponseFromServer() {
        System.out.println("waitResponseFromServer()");
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            if (response.has("method")) {
                responseHandler(response);
            } else if (response.has("status")) {
                receiveAcceptedKpuId(response);
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void nightPhase() {
        System.out.println("*** Entering night phase ***");
        getListClient();
    }

    private void responseHandler(JSONObject response) {
        try {
            System.out.println("responseHandler() : " + response.toString());
            String method = response.getString("method");
            switch (method) {
                case "change_phase":
                    changePhase(response);
                    break;
                case "game_over":
                    gameOver(response);
                    break;
                case "kpu_selected":
                    setCurrentLeaderId(response.getInt("kpu_id"));
                    waitResponseFromServer();
                    break;
                case "vote_now":
                    String phase = response.getString("phase");
                    if (phase.equals("day")) {
                        System.out.println("*** Kill Werewolf ***");
                        showAvailablePlayer();
                        System.out.print("Input username yang ingin dibunuh : ");
                        String username = scanner.nextLine();
                        int kpuId = getCurrentLeaderId();
//                        if (kpuId == playerId) {
//                            initWaitForResponses("killWerewolfVote", copyListPlayer(players), "");
//                        }
                        
                        killWerewolfVote(kpuId, username);
                        
//                        if (kpuId == playerId) {
//                            waitForResponses();
//                        }
                    } else if(phase.compareTo("night") == 0){
                        System.out.println("*** Kill Civilian ***");
                        System.out.print("Input username yang ingin dibunuh : ");
                        String username = scanner.nextLine();
                        int kpuId = getCurrentLeaderId();
                        killCivilianVote(kpuId, username);
                    }
                    break;
            }
        } catch (JSONException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, "responseHandler(): " + response.toString(), ex);
        }
    }
    
    private void setCurrentLeaderId(int kpuId) {
        currentLeaderId = kpuId;
        System.out.println("Leader PID " + kpuId);
    }

    private void showAvailablePlayer() {
        for (Player p : players) {
            if(p.getAlive()==1)
                System.out.println(p.getUsername());
            else if (p.getAlive()==0)
                System.out.println(p.getUsername()+" die, role: "+ p.getRole());
        }
    }

    private void changePhase(JSONObject response) {
        System.out.println("time : " + response.getString("time"));
        System.out.println("days : " + response.getInt("days"));
        System.out.println("description : " + response.getString("description"));
        if(response.getString("time").compareTo("day") == 0) {
            dayPhase();
        } else if(response.getString("time").compareTo("night") == 0) {
            nightPhase();
        }
    }

    private void gameOver(JSONObject response) {
        String winner = response.getString("winner");
        if(role.compareTo(winner) == 0) {
            System.out.println("*** Congratulation, you've won the game ***");
        } else {
            System.out.println("*** Game Over ***");
            System.out.println(response.getString("description"));
        }
        isGameOver = true;
        resetGame();
    }
    
    private void resetGame() {
        this.players = new ArrayList<>();
        this.playerId = -1;
        this.role = "";
    }
    
    private void chooseLeader() {
        if (isLeader()) {
            paxosPrepareProposal(copyListPlayer(players));
            if (!forceProposerCease) {
                System.out.println("PHASE 2: ACCEPT PROPOSAL");
                paxosAcceptProposal(copyListPlayer(players));
            }
        }
    }

    public void killWerewolfVote(int kpuId, String username) {
        int playerId = -1;
        Player client = this.findPlayer(kpuId, players);
        for (Player p : players) {
            if (p.getUsername().compareTo(username) == 0) {
                playerId = p.getPlayerId();
            }
        }
        JSONObject request = new JSONObject();
        request.put("method", "vote_werewolf");
        request.put("player_id", playerId);

        sendToClient(request.toString(), client.getUdpAddress(), client.getUdpPort());
    }

    private void infoWerewolfKilled(HashMap<Integer, Integer> voteMap) {
        int kill = -1;
        List<Player> player = getAlivePlayers();
        
        System.out.println("infoWerewolfKilled(): " + voteMap.toString());
        
        int majority = majorityVote(voteMap);
        int[][] voteResult = new int[players.size()][2];
        for(int j=0; j<players.size(); j++) {
            int playerId = players.get(j).getPlayerId();
            voteResult[j][0] = playerId;
            if (voteMap.containsKey(playerId)) {
                voteResult[j][1] = voteMap.get(playerId);
            } else {
                voteResult[j][1] = 0;
            }
        }
        int quorum = player.size()/2 ;
        if(voteMap.get(majority) > quorum)
            kill=1;
        //send to server
        JSONObject request = new JSONObject();
        if(kill==1) {
            request.put("method", "vote_result_werewolf");
            request.put("player_killed", majority);
        }else {
            request.put("method", "vote_result");
        }
        request.put("vote_status", kill);
        request.put("vote_result", voteResult);
        sendToServer(request.toString());
    }
    
    private int numberWerewolf() {
        int count = 2;
        try {
            List<Player> clients = getDiedPlayers();
            System.out.println(clients.toString());
            for(Player p : clients) {
                if(p.getRole().equalsIgnoreCase("werewolf")){
                    count--;
                }
            }
        } catch (NullPointerException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }
    
    private void infoCivilianKilled(HashMap<Integer, Integer> voteMap) {
        int kill = -1;
        List<Player> player = getAlivePlayers();
        
        System.out.println("infoCivilianKilled(): " + voteMap.toString());
        
        int majority = majorityVote(voteMap);
        int[][] voteResult = new int[players.size()][2];
        for(int j=0; j<players.size(); j++) {
            int playerId = players.get(j).getPlayerId();
            voteResult[j][0] = playerId;
            if (voteMap.containsKey(playerId)) {
                voteResult[j][1] = voteMap.get(playerId);
            } else {
                voteResult[j][1] = 0;
            }
        }
        int quorum = numberWerewolf()/2 ;
        if(voteMap.get(majority) > quorum)
            kill=1;
        //send to server
        JSONObject request = new JSONObject();
        if(kill==1) {
            request.put("method", "vote_result_civilian");
            request.put("player_killed", majority);
        }else {
            request.put("method", "vote_result");
        }
        request.put("vote_status", kill);
        request.put("vote_result", voteResult);
        sendToServer(request.toString());
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

    private void killCivilianVote(int kpuId, String username) {
        Player client = findPlayer(kpuId, players);
        JSONObject request = new JSONObject();
        request.put("method", "vote_civilian");
        int playerId = 0;
        for (Player p : players) {
            if (p.getUsername().compareTo(username) == 0) {
                playerId = p.getPlayerId();
            }
        }
        request.put("player_id", playerId);

        sendToClient(request.toString(), client.getUdpAddress(), client.getUdpPort());
    }

    private void getListClient() {
        players = new ArrayList<>();
        JSONObject request = new JSONObject();
        request.put("method", "client_address");
        sendToServer(request.toString());
        try {
            String input = inputFromServer.readUTF();
            JSONObject response = new JSONObject(input);
            System.out.println("getListClient() : " + response.toString());
            String status = response.getString("status");
            switch (status) {
                case "ok":
                    System.out.println(response.getJSONArray("clients"));
                    JSONArray lineClients = response.getJSONArray("clients");
                    for (Object client : lineClients) {
                        JSONObject jsonLineClient = (JSONObject) client;
                        int playerId = jsonLineClient.getInt("player_id");
                        int isAlive = jsonLineClient.getInt("is_alive");
                        String address = jsonLineClient.getString("address");
                        int port = jsonLineClient.getInt("port");
                        String username = jsonLineClient.getString("username");
                        String role = "";
                        if (jsonLineClient.has("role")) {
                            role = jsonLineClient.getString("role");
                            System.out.println(username + " die, role: " + role);
                        }
                        players.add(new Player(playerId, isAlive, address, port, username, role));
                    }
                    break;
                case "fail":
                    System.out.println("Failed, " + response.getString("description") + ".");
                    break;
                case "error":
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

        /* checks if client is a valid proposer: Yang bisa jadi proposer cuman pid dua terbesar (player ke n dan n-1) */
        for (int i = 0; i < players.size() && greaterPlayerIdCount < MAX_PROPOSER_COUNT; i++) {
            Player player = players.get(i);
            if (player.getPlayerId() != playerId) {
                if (player.getPlayerId() > playerId) {
                    greaterPlayerIdCount++;
                }
            }
        }

        if (greaterPlayerIdCount >= MAX_PROPOSER_COUNT) {
            isLeader = false;
        } else {
            isLeader = true;
        }

        System.out.println("isLeader(): " + isLeader + " " + greaterPlayerIdCount);
        return isLeader;
    }

    private String functionWaitingResponse = "";
    private List<Player> responseClients;
    private List<Player> waitingExemptionResponseClients;
    private String responseRequest;
    private boolean forceProposerCease;

    public int receiveResponseFromClient(JSONObject response) {
        int returnValue = -2;
        switch (functionWaitingResponse) {
            case "paxosPrepareProposal":
                returnValue = receiveResponsePaxosPrepareProposal(response);
                break;
            case "paxosAcceptProposal":
                returnValue = receiveResponsePaxosAcceptProposal(response);
                break;
        }
        return returnValue;
    }
    
    public void exemptResponseFrom(String ipAddress, int port) {
        try {
            System.out.println("exemptResponseFrom(): " + ipAddress + ":" + port);
            for (int i = 0; i < responseClients.size(); i++) {
                Player client = responseClients.get(i);
                if (client.getUdpAddress().equals(ipAddress) && client.getUdpPort() == port) {
                    waitingExemptionResponseClients.add(client);
                }
            }
        } catch (NullPointerException ex) {
            /* do nothing */
            System.out.println("exemptResponseFrom(): " + "Has not been initialized.");
        }
    }

    private int receiveResponsePaxosPrepareProposal(JSONObject response) {
        int returnValue = -2;
        try {
            String status = response.getString("status");

            int latestAcceptedKpuId = -1;
            switch (status) {
                case "ok":
                    if (response.has("previous_accepted")) {
                        int prevAcceptedKpuId = response.getInt("previous_accepted");
                        System.out.println("Accepted, " + prevAcceptedKpuId);

                        if (prevAcceptedKpuId > latestAcceptedKpuId) {
                            latestAcceptedKpuId = prevAcceptedKpuId;
                        }

                        previousAcceptedKpuId = latestAcceptedKpuId;
                    }
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
        } catch (NullPointerException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.WARNING, "Max send retries touched.");
        }
        return returnValue;
    }
    
    private int receiveResponsePaxosAcceptProposal(JSONObject response) {
        String status = response.getString("status");

        int returnValue = -2;
        switch (status) {
            case "ok":
                System.out.println("Accepted");
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
        return returnValue;
    }

    private void initWaitForResponses(String functionName, List<Player> players, String request) {
        this.functionWaitingResponse = functionName;
        this.responseClients = players;
        this.waitingExemptionResponseClients = new ArrayList<>();
        this.responseRequest = request;
        this.forceProposerCease = false;
    }
    
    public void forceProposerCease() {
        this.forceProposerCease = true;
    }
    
    private ArrayList<Player> copyListPlayer(ArrayList<Player> players) {
        ArrayList<Player> result = new ArrayList<>();
        for(Player p : players) {
            result.add(p.copy());
        }
        return result;
    }
    
    private void waitForResponses() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int retryCount = 0;
        while (players.size() > 0 && retryCount < MAX_RETRY) {
            for (Player player : waitingExemptionResponseClients) {
                responseClients.remove(player);
            }
            
            for (Player player : responseClients) {
                Logger.getLogger(Client.class.getName()).log(Level.INFO, "Resending packet to {0}:{1}...", new Object[]{player.getUdpAddress(), player.getUdpPort()});
                sendToClient(responseRequest, player.getUdpAddress(), player.getUdpPort());
            }
            retryCount++;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("waitForResponses(): Ended.");
    }

    private void paxosPrepareProposal(List<Player> players) {
        // stopClientThread();

        /* send request */
        // TODO: proposal_id is waiting perfect specification
        JSONObject request = new JSONObject();
        JSONArray proposalIdData = new JSONArray();
        proposalIdData.put(++previousAcceptedKpuId);
        proposalIdData.put(playerId);
        request.put("method", "prepare_proposal");
        request.put("proposal_id", proposalIdData);

        
        initWaitForResponses("paxosPrepareProposal", players, request.toString());
        for (int i = 0; i < players.size(); i++) {
            Player client = players.get(i);
            String address = client.getUdpAddress();
            int port = client.getUdpPort();

            sendToClient(request.toString(), address, port, true);
        }
        waitForResponses();

        // startClientThread();
    }

    private void paxosAcceptProposal(List<Player> players) {
        // stopClientThread();
        
        /* send request */
        // TODO: proposal_id is waiting perfect specification
        JSONObject request = new JSONObject();
        JSONArray proposalIdData = new JSONArray();
        proposalIdData.put(++previousAcceptedKpuId);
        proposalIdData.put(playerId);

        request.put("method", "accept_proposal");
        request.put("proposal_id", proposalIdData);
        request.put("kpu_id", playerId);

        initWaitForResponses("paxosAcceptProposal", players, request.toString());
        System.out.println("paxosAcceptProposal(): Size = " + players.size());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            sendToClient(request.toString(), player.getUdpAddress(), player.getUdpPort());
        }
        waitForResponses();

        // startClientThread();
    }

    public void clientAcceptProposal() {
        JSONObject request = new JSONObject();
        request.put("method", "accepted_proposal");
        request.put("kpu_id", previousProposerId);
        request.put("description", "kpu is selected");

        System.out.println("clientAcceptProposal()");
        sendToServer(request.toString());

    }
    
    private void receiveAcceptedKpuId(JSONObject response) {
        String status = response.getString("status");
        switch (status) {
            case "ok":
                System.out.println("Leader vote accepted.");
                break;
            case "fail":
                System.out.println("Failed, " + response.getString("description") + ".");
                break;
            case "error":
                System.out.println("Error, " + response.getString("description") + ".");
                break;
        }
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
        return currentLeaderId;
    }
    
    private int numberAlive(){
        int count=0;
        for(Player p : players){
            if(p.getAlive()==1)
                count++;
        }
        return count;
    }
    private int voteWerewolfCount = 0;
    private HashMap<Integer, Integer> votedWerewolves = new HashMap<>();
    
    public void voteWerewolf(int playerId) {
        if (getVoteWerewolfCount() >= numberAlive()) {
            votedWerewolves.clear();
            setVoteWerewolfCount(0);
        }
        
        // bila kosong
        if (getVotedWerewolves().containsKey(playerId)) {
            getVotedWerewolves().put(playerId, getVotedWerewolves().get(playerId)+1);
        } else {
            getVotedWerewolves().put(playerId,1);
        }
        setVoteWerewolfCount(getVoteWerewolfCount() + 1);
        
        System.out.println("voteWerewolf(): " + getVoteWerewolfCount() + " " + players.size());
        if (getVoteWerewolfCount() >= numberAlive()) {
            // lakukan sesuatu untuk memproses voting
            infoWerewolfKilled(getVotedWerewolves());
        }
        
    }

    private int voteCivilianCount = 0;
    private HashMap<Integer, Integer> votedCivilians = new HashMap<>();
    
    public void voteCivilian(int playerId) {
        if (voteCivilianCount >= numberWerewolf()) {
            votedCivilians.clear();
            voteCivilianCount = 0;
        }
        
        // bila kosong
        if (votedCivilians.containsKey(playerId)) {
            votedCivilians.put(playerId, votedCivilians.get(playerId)+1);
        } else {
            votedCivilians.put(playerId,1);
        }
        voteCivilianCount++;
        
        System.out.println("voteCivilian(): " + voteCivilianCount + " " + numberWerewolf());
        if (voteCivilianCount >= numberWerewolf()) {
            // lakukan sesuatu untuk memproses voting
            infoCivilianKilled(votedCivilians);
        }
        
    }
    
    /**
     * @return the voteWerewolfCount
     */
    public int getVoteWerewolfCount() {
        return voteWerewolfCount;
    }

    /**
     * @param voteWerewolfCount the voteWerewolfCount to set
     */
    public void setVoteWerewolfCount(int voteWerewolfCount) {
        this.voteWerewolfCount = voteWerewolfCount;
    }

    /**
     * @return the votedWerewolves
     */
    public HashMap<Integer, Integer> getVotedWerewolves() {
        return votedWerewolves;
    }

    /**
     * @param votedWerewolves the votedWerewolves to set
     */
    public void setVotedWerewolves(HashMap<Integer, Integer> votedWerewolves) {
        this.votedWerewolves = votedWerewolves;
    }
    
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String serverIpAddress = "127.0.0.1";
        int serverPort = 8080;
//        System.out.print("Input your port : ");
//        int myPort = in.nextInt();
        int myPort = (int)(Math.random() * 9999);
        System.out.println("Your Port is " + myPort);
        int timeout = 1 * 1000; // 5 seconds
        Client client = new Client(serverIpAddress, serverPort, myPort, timeout);
        client.run();
    }

}