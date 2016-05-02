/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.*;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author natanelia
 */
public class ClientThread extends Observable implements Runnable {
    private Client parent;
    private String lastResponse = "";
    private String lastRequest = "";
    
    public ClientThread(Client parent) {
        super();
        this.parent = parent;
    }
    
    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket dp = parent.receiveFromClient();
                requestHandler(dp);
            } catch (SocketTimeoutException ex) {
                /* do nothing */
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(Client.class.getName()).log(Level.FINE, "Client thread exited.");
    }
    
    public void requestHandler(DatagramPacket dp) {
        String ipAddress = dp.getAddress().getHostAddress();
        int port = dp.getPort();
        String in = new String(dp.getData());
        System.out.println("requestHandler():" + in);
        
        JSONObject request = new JSONObject(in);
        if (request.has("method")) {
            if (!lastRequest.equals(in)
                    || (!request.getString("method").equals("accept_proposal")
                    && !request.getString("method").equals("prepare_proposal"))) {
                lastRequest = in;
                // it is really a request
                try {
                    String method = request.getString("method");
                    switch (method) {
                        case "prepare_proposal":
                            prepareProposalHandler(request.getJSONArray("proposal_id"), ipAddress, port);
                            break;
                        case "accept_proposal":
                            acceptProposalHandler(request.getJSONArray("proposal_id"), ipAddress, port);
                            parent.clientAcceptProposal();
                            break;
                        case "vote_werewolf":
                            voteWerewolfHandler(request.getInt("player_id"), ipAddress, port);
                            break;
                        case "vote_civilian":
                            voteCivilianHandler(request.getInt("player_id"), ipAddress, port);
                            break;
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.WARNING, "requestHandler(): ERROR " + request.toString(), ex);
                    errorHandler(ipAddress, port);
                }
            } else {
                /* resend packet */
                Logger.getLogger(Client.class.getName()).log(Level.INFO, "requestHandler(): " + "Resending...");
                parent.sendToClient(lastResponse, ipAddress, port);
            }
        } else if (request.has("status")) {
            /* it is actually a response */
            parent.exemptResponseFrom(ipAddress ,port);
            if (parent.receiveResponseFromClient(request) != 1) {
                parent.forceProposerCease();
            }
        }
    }

    private void errorHandler(String ipAddress, int port) {
        JSONObject response = new JSONObject();
        response.put("status", "error");
        response.put("description", "wrong client request");
        parent.sendToClient(response.toString(), ipAddress, port);
    }

    private void prepareProposalHandler(JSONArray proposalIdData, String ipAddress, int port) {
        int proposalId = proposalIdData.getInt(0);
        int proposerId = proposalIdData.getInt(1);
        
        JSONObject response = new JSONObject();
        if (parent.getPreviousAcceptedKpuId() <= proposalId && parent.getPreviousProposerId() <= proposerId) {
            response.put("status", "ok");
            response.put("description", "accepted");
            if (parent.getPreviousAcceptedKpuId() > 0) {
                response.put("previous_accepted", parent.getPreviousAcceptedKpuId());
                if (proposerId > parent.getPlayerId()) {
                    parent.forceProposerCease();
                }
            }
            parent.setPreviousAcceptedKpuId(proposalId);
            parent.setPreviousProposerId(proposerId);
        } else {
            response.put("status", "fail");
            response.put("description", "rejected");
        }
        
        lastResponse = response.toString();
        parent.sendToClient(lastResponse, ipAddress, port);
    }
    
    private void acceptProposalHandler(JSONArray proposalIdData, String ipAddress, int port) {
        int proposalId = proposalIdData.getInt(0);
        int proposerId = proposalIdData.getInt(1);
        
        JSONObject response = new JSONObject();
        if (parent.getPreviousAcceptedKpuId() <= proposalId && parent.getPreviousProposerId() <= proposerId) {
            response.put("status", "ok");
            response.put("description", "accepted");
            parent.setPreviousAcceptedKpuId(proposalId);
            parent.setPreviousProposerId(proposerId);
        } else {
            response.put("status", "fail");
            response.put("description", "rejected");
        }
        
        lastResponse = response.toString();
        parent.sendToClient(lastResponse, ipAddress, port);
    }
    
    private void voteWerewolfHandler(int playerId, String ipAddress, int port) {
        parent.voteWerewolf(playerId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("description", "");
        lastResponse = response.toString();
        parent.sendToClient(lastResponse, ipAddress, port);
    }
    
    private void voteCivilianHandler(int playerId, String ipAddress, int port) {
        parent.voteCivilian(playerId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("description", "");
        lastResponse = response.toString();
        parent.sendToClient(lastResponse, ipAddress, port);
    }
}