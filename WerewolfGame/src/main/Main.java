/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import client.GameFrame;

/**
 *
 * @author William Sentosa
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GameFrame gameFrame = new GameFrame();
        gameFrame.register();
        gameFrame.setVisible(true);
    }
    
}
