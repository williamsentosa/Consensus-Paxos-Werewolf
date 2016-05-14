/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import server.Player;


/**
 *
 * @author FiqieUlya
 */
public class GameFrame extends javax.swing.JFrame implements Observer {
    private  Client client;
    private Observable observable = null;
    private GuiThread guiThread = null;
    private DefaultTableModel users = new DefaultTableModel(new Object[]{"ID Pemain", "Nama Pemain", "Status", "Role"},0);
    private static String[] phaseList =  { 
        "/image/day.png","/image/night.png"
    };
    private static String[] roleList= {
        "/image/profilecivilian.png", "/image/profilewerewolf.png"
    };
    public String playerChoosen;
    /**
     * Creates new form Register
     */
    public GameFrame() { 
        initComponents();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        VoteNow = new javax.swing.JDialog();
        jPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        listPlayerAlive = new javax.swing.JComboBox();
        voteButton = new javax.swing.JButton();
        Menu = new javax.swing.JPanel();
        Game = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ListPlayer = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        gameUserName = new javax.swing.JLabel();
        role = new javax.swing.JLabel();
        bg = new javax.swing.JLabel();
        Register = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        inputClientPort = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        inputServerAddress = new javax.swing.JTextField();
        inputServerPort = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        connectButton = new javax.swing.JButton();
        Join = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        inputUser = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        joinButton = new javax.swing.JButton();
        Waiting = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(153, 153, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(600, 600));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel10.setText("VOTE NOW !");

        jLabel11.setText("Username");

        listPlayerAlive.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        voteButton.setText("Vote!");
        voteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(156, 156, 156)
                        .addComponent(voteButton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(listPlayerAlive, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(listPlayerAlive, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addComponent(voteButton)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout VoteNowLayout = new javax.swing.GroupLayout(VoteNow.getContentPane());
        VoteNow.getContentPane().setLayout(VoteNowLayout);
        VoteNowLayout.setHorizontalGroup(
            VoteNowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
        );
        VoteNowLayout.setVerticalGroup(
            VoteNowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Menu.setLayout(new java.awt.CardLayout());

        Game.setBackground(new java.awt.Color(255, 255, 255));
        Game.setLayout(null);

        jLabel8.setBackground(new java.awt.Color(255, 255, 255));
        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 0, 0));
        jLabel8.setText("ROLE   :");
        Game.add(jLabel8);
        jLabel8.setBounds(58, 50, 67, 22);

        ListPlayer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(ListPlayer);

        Game.add(jScrollPane1);
        jScrollPane1.setBounds(58, 265, 452, 356);

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 0, 51));
        jLabel9.setText("Player:");
        Game.add(jLabel9);
        jLabel9.setBounds(58, 237, 53, 22);

        gameUserName.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        gameUserName.setText("USERNAME");
        Game.add(gameUserName);
        gameUserName.setBounds(300, 70, 137, 30);

        role.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/profilecivilian.png"))); // NOI18N
        Game.add(role);
        role.setBounds(135, 11, 333, 172);

        bg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/day.png"))); // NOI18N
        Game.add(bg);
        bg.setBounds(0, 0, 980, 690);

        Menu.add(Game, "game");

        Register.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/werewolf.png"))); // NOI18N

        inputClientPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputClientPortActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel2.setText("Your Port");

        inputServerAddress.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputServerAddressActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel3.setText("Server Address");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel4.setText("Server Port");

        connectButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        connectButton.setText("Connect");
        connectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                connectButtonMouseClicked(evt);
            }
        });
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RegisterLayout = new javax.swing.GroupLayout(Register);
        Register.setLayout(RegisterLayout);
        RegisterLayout.setHorizontalGroup(
            RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(RegisterLayout.createSequentialGroup()
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(inputServerPort, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                            .addComponent(inputServerAddress)
                            .addComponent(inputClientPort, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        RegisterLayout.setVerticalGroup(
            RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RegisterLayout.createSequentialGroup()
                .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
                    .addGroup(RegisterLayout.createSequentialGroup()
                        .addGap(281, 281, 281)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(inputClientPort, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(inputServerAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(18, 18, 18)
                        .addGroup(RegisterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(inputServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        Menu.add(Register, "register");
        Register.getAccessibleContext().setAccessibleName("");

        Join.setBackground(new java.awt.Color(255, 255, 255));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/joingame.png"))); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jLabel6.setText("Username");

        joinButton.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        joinButton.setText("Ready");
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout JoinLayout = new javax.swing.GroupLayout(Join);
        Join.setLayout(JoinLayout);
        JoinLayout.setHorizontalGroup(
            JoinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JoinLayout.createSequentialGroup()
                .addGap(276, 276, 276)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(290, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, JoinLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(56, 56, 56)
                .addGroup(JoinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(joinButton)
                    .addComponent(inputUser, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(312, 312, 312))
        );
        JoinLayout.setVerticalGroup(
            JoinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(JoinLayout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addComponent(jLabel5)
                .addGap(53, 53, 53)
                .addGroup(JoinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(49, 49, 49)
                .addComponent(joinButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Menu.add(Join, "join");
        Join.getAccessibleContext().setAccessibleName("");

        Waiting.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Image/waiting.png"))); // NOI18N

        javax.swing.GroupLayout WaitingLayout = new javax.swing.GroupLayout(Waiting);
        Waiting.setLayout(WaitingLayout);
        WaitingLayout.setHorizontalGroup(
            WaitingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WaitingLayout.createSequentialGroup()
                .addGap(214, 214, 214)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(312, Short.MAX_VALUE))
        );
        WaitingLayout.setVerticalGroup(
            WaitingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(WaitingLayout.createSequentialGroup()
                .addGap(241, 241, 241)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(259, Short.MAX_VALUE))
        );

        Menu.add(Waiting, "waiting");
        Waiting.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Menu, javax.swing.GroupLayout.PREFERRED_SIZE, 978, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Menu, javax.swing.GroupLayout.PREFERRED_SIZE, 688, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void inputClientPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputClientPortActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputClientPortActionPerformed

    private void inputServerAddressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputServerAddressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputServerAddressActionPerformed

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        String serverAddress = inputServerAddress.getText();
        int serverPort = Integer.parseInt(inputServerPort.getText());
        int myPort = Integer.parseInt(inputClientPort.getText());
        int timeout = 1 * 1000; // 5 seconds
        client = new Client(serverAddress, serverPort, myPort, timeout);
        client.setGameFrame(this);
        observable = client.getObservable();
        joinGame();
    }//GEN-LAST:event_connectButtonActionPerformed

    private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
        String username = this.inputUser.getText();
        String result = client.joinCommand(username);
        System.out.println(result);
        if(result.compareTo("success") == 0) {
            guiThread = new GuiThread(client,this);
            new Thread(guiThread).start();
            guiThread.changeCommand("ready");
            waiting();
        } else {
            JOptionPane.showMessageDialog(this, "Username has been used, try another name.");
        }
    }//GEN-LAST:event_joinButtonActionPerformed

    private void connectButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectButtonMouseClicked
        //Create client
    }//GEN-LAST:event_connectButtonMouseClicked

    private void voteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voteButtonActionPerformed
        playerChoosen = listPlayerAlive.getSelectedItem().toString();
        int kpuId = client.getCurrentLeaderId();
        client.killWerewolfVote(kpuId, playerChoosen);
        VoteNow.setVisible(false);
        waiting();
    }//GEN-LAST:event_voteButtonActionPerformed

    
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(GameFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//        //</editor-fold>
//        //</editor-fold>
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new GameFrame().setVisible(true);
//            }
//        });
//    }
    public void changePanel(String name){
        ((java.awt.CardLayout)Menu.getLayout()).show(Menu, name);
    }
    public void joinGame(){
        changePanel("join");
    }
    public void waiting(){
        changePanel("waiting");
    }
    public void game(){
        changePanel("game");
    }
    public void register(){
        changePanel("register");
    }
    public void changeDay(String phase){
        int temp=0;
        if(phase.equalsIgnoreCase("day")){
            temp=0;
        }else temp=1;
        ImageIcon image = new ImageIcon(getClass().getResource(phaseList[temp]));
        bg.setIcon(image);
    }
    public void changeRole(String Role){
        int temp=0;
        if(Role.equalsIgnoreCase("werewolf")){
            temp=1;
        }else temp=0;
        ImageIcon image = new ImageIcon(getClass().getResource(roleList[temp]));
        role.setIcon(image);
    }
    
    public void setUsername(String username) {
        gameUserName.setText(username);
    }
    
    public DefaultTableModel getTableModel() {
        return users;
    }
    
    public void updateModel(ArrayList<Player> players) {
        users.setRowCount(0);
        for(Player player: players){
            Object[] o = new Object[4];
            o[0]=player.getPlayerId();
            o[1]=player.getUsername();
            o[2]=player.getAlive();
            o[3]=player.getRole();
            users.addRow(o); 
        }
    }    
    
    public void initPlayerTable(DefaultTableModel model) {
        ListPlayer.setModel(model);
        ListPlayer.getColumnModel().getColumn(0).setPreferredWidth(100);
        ListPlayer.getColumnModel().getColumn(1).setPreferredWidth(200);
        ListPlayer.getColumnModel().getColumn(2).setPreferredWidth(100);
        ListPlayer.getColumnModel().getColumn(3).setPreferredWidth(150);
    }

         
    public void initComboPlayer(ArrayList<Player> players){
        listPlayerAlive.removeAllItems();
        for(Player player: players){
            if(player.getAlive()==1)
                listPlayerAlive.addItem(player.getUsername());
        }
    }
    
    public void voteNow(ArrayList<Player> players){
        initComboPlayer(players);
        VoteNow.setSize(450,250);
        VoteNow.setLocationRelativeTo(null);
        VoteNow.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Game;
    private javax.swing.JPanel Join;
    private javax.swing.JTable ListPlayer;
    private javax.swing.JPanel Menu;
    private javax.swing.JPanel Register;
    private javax.swing.JDialog VoteNow;
    private javax.swing.JPanel Waiting;
    private javax.swing.JLabel bg;
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel gameUserName;
    private javax.swing.JTextField inputClientPort;
    private javax.swing.JTextField inputServerAddress;
    private javax.swing.JTextField inputServerPort;
    private javax.swing.JTextField inputUser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton joinButton;
    private javax.swing.JComboBox listPlayerAlive;
    private javax.swing.JLabel role;
    private javax.swing.JButton voteButton;
    // End of variables declaration//GEN-END:variables

    public static void main(String args[]) {
        GameFrame gameFrame = new GameFrame();
        //cara menggunakan ganti hari sama ganti role
        //gameFrame.changeDay("night");
        //gameFrame.changeRole("werewolf");
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
        Player player = new Player();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        gameFrame.updateModel(players);
        gameFrame.initPlayerTable(gameFrame.getTableModel());
        gameFrame.voteNow(players);
    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("halo");
    }
}
