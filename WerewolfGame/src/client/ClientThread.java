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
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author natanelia
 */
public class ClientThread implements Runnable {
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
    }
    
    public void requestHandler(DatagramPacket dp) {
        String ipAddress = dp.getAddress().getHostAddress();
        int port = dp.getPort();
        String in = new String(dp.getData());
        System.out.println(in);
        
        if (!lastRequest.equalsIgnoreCase(in)) {
            lastRequest = in;
            JSONObject request = new JSONObject(in);
            try {
                String method = request.getString("method");
                switch (method) {
                    case "prepare_proposal":
                        prepareProposalHandler(request.getString("proposal_id"), ipAddress, port);
                        break;
                }
            } catch (JSONException ex) {
                errorHandler(ipAddress, port);
            }
        } else {
            /* resend packet */
            parent.sendToClient(lastResponse, ipAddress, port);
        }
    }

    private void errorHandler(String ipAddress, int port) {
        JSONObject response = new JSONObject();
        response.put("status", "error");
        response.put("description", "wrong client request");
        parent.sendToClient(response.toString(), ipAddress, port);
    }

    private void prepareProposalHandler(String in, String ipAddress, int port) {
        in = in.substring(1, in.length() - 1);
        String[] data = in.split(", ");
        int proposalId = Integer.parseInt(data[0]);
        int playerId = Integer.parseInt(data[1]);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("description", "accepted");
        response.put("previous_accepted", parent.getPreviousAcceptedKpuId());
        parent.setPreviousAcceptedKpuId(proposalId);
        
        lastResponse = response.toString();
        parent.sendToClient(lastResponse, ipAddress, port);
    }
    
    
}
