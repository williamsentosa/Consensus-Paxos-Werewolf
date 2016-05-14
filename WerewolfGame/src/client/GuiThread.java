/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.Observable;

/**
 *
 * @author William Sentosa
 */
public class GuiThread implements Runnable {
    
    private String command = "";
    private Client client;
    private GameFrame gameFrame;
    
    public GuiThread(Client client, GameFrame gameFrame) {
        this.client = client;
        this.gameFrame = gameFrame;
    }
    
    public void changeCommand(String newCommand) {
        this.command = newCommand;
    }
    
    @Override
    public void run() {
        while (true) {
            switch (command) {
                case "ready" :
                    client.readyUpCommand();
                    gameFrame.game();
                    command = ""; // reset
                    break;
                case "vote" :
                    String playerSelected = gameFrame.playerChoosen;
                    int kpuId = client.getCurrentLeaderId();
                    client.killWerewolfVote(kpuId, playerSelected);
                    command = "";
                    break;
            }
        }
    }
    
}
