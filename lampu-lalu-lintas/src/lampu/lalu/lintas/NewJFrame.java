/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package lampu.lalu.lintas;
import java.awt.Color;
import javax.swing.Timer;
/**
 *
 * @author zakif
 */
public class NewJFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NewJFrame.class.getName());
    private Timer trafficTimer;
    private int phaseIndex = 0;
private int remainingSeconds = 1;

private final String[] directions = {
    "North", "North", "North",
    "East", "East", "East",
    "South", "South", "South",
    "West", "West", "West"
};

private final Color[] phaseColors = {
    Color.YELLOW, Color.GREEN, Color.YELLOW,
    Color.YELLOW, Color.GREEN, Color.YELLOW,
    Color.YELLOW, Color.GREEN, Color.YELLOW,
    Color.YELLOW, Color.GREEN, Color.YELLOW
};

private final int[] phaseDurations = {
    1, 20, 1,
    1, 20, 1,
    1, 20, 1,
    1, 20, 1
};
    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        initComponents();
         initializeTimer();
         resetSimulation();
    }
    
    private void updateTrafficLights() {

    // Set all directions to red
    lblNorth.setForeground(Color.RED);
    lblEast.setForeground(Color.RED);
    lblSouth.setForeground(Color.RED);
    lblWest.setForeground(Color.RED);

    // Change the active direction
    switch (directions[phaseIndex]) {

        case "North":
            lblNorth.setForeground(phaseColors[phaseIndex]);
            break;

        case "East":
            lblEast.setForeground(phaseColors[phaseIndex]);
            break;

        case "South":
            lblSouth.setForeground(phaseColors[phaseIndex]);
            break;

        case "West":
            lblWest.setForeground(phaseColors[phaseIndex]);
            break;
    }
}
private void initializeTimer() {

    trafficTimer = new Timer(1000, e -> {

        remainingSeconds--;

        if (remainingSeconds <= 0) {

            phaseIndex++;

            if (phaseIndex >= phaseDurations.length) {
                phaseIndex = 0;
            }

            remainingSeconds = phaseDurations[phaseIndex];

            updateTrafficLights();
        }

        updateCountdownFields();
    });
}
private void startSimulation() {

    if (trafficTimer == null) {
        initializeTimer();
    }

    if (!trafficTimer.isRunning()) {

        updateTrafficLights();
        updateCountdownFields();

        trafficTimer.start();
    }
}

private void stopSimulation() {

    if (trafficTimer != null) {
        trafficTimer.stop();
    }
}

private void resetSimulation() {

    if (trafficTimer != null) {
        trafficTimer.stop();
    }

    phaseIndex = 0;
    remainingSeconds = 1;

    lblNorth.setForeground(Color.RED);
    lblEast.setForeground(Color.RED);
    lblSouth.setForeground(Color.RED);
    lblWest.setForeground(Color.RED);

    txtNorthRed.setText("--");
    txtNorthGreen.setText("--");
    txtNorthYellow.setText("--");

    txtEastRed.setText("--");
    txtEastGreen.setText("--");
    txtEastYellow.setText("--");

    txtSouthRed.setText("--");
    txtSouthGreen.setText("--");
    txtSouthYellow.setText("--");

    txtWestRed.setText("--");
    txtWestGreen.setText("--");
    txtWestYellow.setText("--");
}
  private void updateCountdownFields() {

    javax.swing.JTextField[][] fields = {
        {txtNorthRed, txtNorthGreen, txtNorthYellow},
        {txtEastRed, txtEastGreen, txtEastYellow},
        {txtSouthRed, txtSouthGreen, txtSouthYellow},
        {txtWestRed, txtWestGreen, txtWestYellow}
    };

    String activeDirection = directions[phaseIndex];
    Color activeColor = phaseColors[phaseIndex];

    String[] allDirections = {
        "North", "East", "South", "West"
    };

    for (int i = 0; i < 4; i++) {

        String direction = allDirections[i];

        // Clear the three fields
        fields[i][0].setText("--");
        fields[i][1].setText("--");
        fields[i][2].setText("--");

        if (direction.equals(activeDirection)) {

            // Display active color countdown
            if (activeColor.equals(Color.RED)) {
                fields[i][0].setText(remainingSeconds + " s");
            }
            else if (activeColor.equals(Color.GREEN)) {
                fields[i][1].setText(remainingSeconds + " s");
            }
            else if (activeColor.equals(Color.YELLOW)) {
                fields[i][2].setText(remainingSeconds + " s");
            }

        } else {

            // Other directions are red.
            int redTime = getTimeUntilYellow(direction);

            fields[i][0].setText(redTime + " s");
        }
    }
}   
  private int getTimeUntilYellow(String direction) {

    int seconds = remainingSeconds;

    int nextPhase = (phaseIndex + 1)
            % phaseDurations.length;

    while (true) {

        if (directions[nextPhase].equals(direction)
                && phaseColors[nextPhase].equals(Color.YELLOW)) {

            return seconds;
        }

        seconds += phaseDurations[nextPhase];

        nextPhase = (nextPhase + 1)
                % phaseDurations.length;
    }
}
   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lblNorth = new javax.swing.JLabel();
        lblEast = new javax.swing.JLabel();
        lblSouth = new javax.swing.JLabel();
        lblWest = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtNorthRed = new javax.swing.JTextField();
        lblWest1 = new javax.swing.JLabel();
        lblWest2 = new javax.swing.JLabel();
        txtNorthGreen = new javax.swing.JTextField();
        lblWest3 = new javax.swing.JLabel();
        txtNorthYellow = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        lblWest4 = new javax.swing.JLabel();
        txtSouthRed = new javax.swing.JTextField();
        txtEastGreen = new javax.swing.JTextField();
        lblWest6 = new javax.swing.JLabel();
        txtEastYellow = new javax.swing.JTextField();
        lblWest7 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblWest8 = new javax.swing.JLabel();
        txtEastRed = new javax.swing.JTextField();
        txtWestGreen = new javax.swing.JTextField();
        lblWest9 = new javax.swing.JLabel();
        txtSouthYellow = new javax.swing.JTextField();
        lblWest10 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtWestRed = new javax.swing.JTextField();
        lblWest11 = new javax.swing.JLabel();
        txtSouthGreen = new javax.swing.JTextField();
        txtWestYellow = new javax.swing.JTextField();
        lblWest12 = new javax.swing.JLabel();
        lblWest13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 255));

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 422, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 189, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(51, 51, 51));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 189, Short.MAX_VALUE)
        );

        lblNorth.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblNorth.setForeground(new java.awt.Color(255, 51, 51));
        lblNorth.setText("●");

        lblEast.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblEast.setForeground(new java.awt.Color(255, 51, 51));
        lblEast.setText("●");

        lblSouth.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblSouth.setForeground(new java.awt.Color(255, 51, 51));
        lblSouth.setText("●");

        lblWest.setFont(new java.awt.Font("Segoe UI", 1, 48)); // NOI18N
        lblWest.setForeground(new java.awt.Color(255, 51, 51));
        lblWest.setText("●");

        jButton1.setText("Start");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setText("Stop");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setText("Reset");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jLabel1.setText("North:");

        txtNorthRed.addActionListener(this::txtNorthRedActionPerformed);

        lblWest1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest1.setForeground(new java.awt.Color(255, 51, 51));
        lblWest1.setText("●");

        lblWest2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest2.setForeground(new java.awt.Color(0, 255, 102));
        lblWest2.setText("●");

        txtNorthGreen.addActionListener(this::txtNorthGreenActionPerformed);

        lblWest3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest3.setForeground(new java.awt.Color(255, 255, 0));
        lblWest3.setText("●");

        txtNorthYellow.addActionListener(this::txtNorthYellowActionPerformed);

        jLabel2.setText("East:");

        lblWest4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest4.setForeground(new java.awt.Color(255, 51, 51));
        lblWest4.setText("●");

        txtSouthRed.addActionListener(this::txtSouthRedActionPerformed);

        txtEastGreen.addActionListener(this::txtEastGreenActionPerformed);

        lblWest6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest6.setForeground(new java.awt.Color(0, 255, 102));
        lblWest6.setText("●");

        txtEastYellow.addActionListener(this::txtEastYellowActionPerformed);

        lblWest7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest7.setForeground(new java.awt.Color(255, 255, 0));
        lblWest7.setText("●");

        jLabel3.setText("South:");

        lblWest8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest8.setForeground(new java.awt.Color(255, 51, 51));
        lblWest8.setText("●");

        txtEastRed.addActionListener(this::txtEastRedActionPerformed);

        txtWestGreen.addActionListener(this::txtWestGreenActionPerformed);

        lblWest9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest9.setForeground(new java.awt.Color(0, 255, 102));
        lblWest9.setText("●");

        txtSouthYellow.addActionListener(this::txtSouthYellowActionPerformed);

        lblWest10.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest10.setForeground(new java.awt.Color(255, 255, 0));
        lblWest10.setText("●");

        jLabel4.setText("West:");

        txtWestRed.addActionListener(this::txtWestRedActionPerformed);

        lblWest11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest11.setForeground(new java.awt.Color(0, 255, 102));
        lblWest11.setText("●");

        txtSouthGreen.addActionListener(this::txtSouthGreenActionPerformed);

        txtWestYellow.addActionListener(this::txtWestYellowActionPerformed);

        lblWest12.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest12.setForeground(new java.awt.Color(255, 255, 0));
        lblWest12.setText("●");

        lblWest13.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblWest13.setForeground(new java.awt.Color(255, 51, 51));
        lblWest13.setText("●");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(lblNorth)
                .addGap(98, 98, 98)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77)
                .addComponent(lblEast)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addGap(44, 44, 44))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblWest2)
                            .addComponent(lblWest1)
                            .addComponent(lblWest3))))
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWest)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtNorthRed, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                        .addComponent(txtNorthGreen)
                        .addComponent(txtNorthYellow)))
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(84, 84, 84)
                        .addComponent(lblSouth)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblWest6)
                                            .addComponent(lblWest7))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtEastYellow, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                            .addComponent(txtEastGreen))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(lblWest9)
                                                .addGap(18, 18, 18)
                                                .addComponent(txtSouthGreen, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(lblWest10)
                                                .addGap(18, 18, 18)
                                                .addComponent(txtSouthYellow, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblWest4)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtEastRed, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(145, 145, 145)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblWest8)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtSouthRed, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel3))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(105, 105, 105))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblWest13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtWestRed, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblWest12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtWestYellow, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblWest11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtWestGreen, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(20, 20, 20))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(lblNorth, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(lblEast, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jButton1)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jButton2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton3)
                            .addComponent(lblSouth, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(82, 82, 82)
                                .addComponent(lblWest, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(37, 37, 37)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblWest1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNorthRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtNorthGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblWest2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtEastGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblWest6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblWest9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtSouthGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtSouthRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblWest8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblWest4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtEastRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblWest13, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtWestRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtWestGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWest11, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNorthYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWest3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEastYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWest7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSouthYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblWest10, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(txtWestYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblWest12, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(88, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
                        stopSimulation();        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
                  resetSimulation();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
                    startSimulation();             // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtNorthRedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNorthRedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNorthRedActionPerformed

    private void txtNorthGreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNorthGreenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNorthGreenActionPerformed

    private void txtNorthYellowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNorthYellowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNorthYellowActionPerformed

    private void txtSouthRedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSouthRedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSouthRedActionPerformed

    private void txtEastGreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEastGreenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEastGreenActionPerformed

    private void txtEastYellowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEastYellowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEastYellowActionPerformed

    private void txtEastRedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEastRedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEastRedActionPerformed

    private void txtWestGreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtWestGreenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtWestGreenActionPerformed

    private void txtSouthYellowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSouthYellowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSouthYellowActionPerformed

    private void txtWestRedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtWestRedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtWestRedActionPerformed

    private void txtSouthGreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSouthGreenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSouthGreenActionPerformed

    private void txtWestYellowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtWestYellowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtWestYellowActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new NewJFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel lblEast;
    private javax.swing.JLabel lblNorth;
    private javax.swing.JLabel lblSouth;
    private javax.swing.JLabel lblWest;
    private javax.swing.JLabel lblWest1;
    private javax.swing.JLabel lblWest10;
    private javax.swing.JLabel lblWest11;
    private javax.swing.JLabel lblWest12;
    private javax.swing.JLabel lblWest13;
    private javax.swing.JLabel lblWest2;
    private javax.swing.JLabel lblWest3;
    private javax.swing.JLabel lblWest4;
    private javax.swing.JLabel lblWest6;
    private javax.swing.JLabel lblWest7;
    private javax.swing.JLabel lblWest8;
    private javax.swing.JLabel lblWest9;
    private javax.swing.JTextField txtEastGreen;
    private javax.swing.JTextField txtEastRed;
    private javax.swing.JTextField txtEastYellow;
    private javax.swing.JTextField txtNorthGreen;
    private javax.swing.JTextField txtNorthRed;
    private javax.swing.JTextField txtNorthYellow;
    private javax.swing.JTextField txtSouthGreen;
    private javax.swing.JTextField txtSouthRed;
    private javax.swing.JTextField txtSouthYellow;
    private javax.swing.JTextField txtWestGreen;
    private javax.swing.JTextField txtWestRed;
    private javax.swing.JTextField txtWestYellow;
    // End of variables declaration//GEN-END:variables
}
