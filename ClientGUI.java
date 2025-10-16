/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package Network_Project;

import javax.swing.*;
import javax.swing.SpinnerNumberModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author hetaf
 */
public class ClientGUI extends JFrame {

    private static final String SERVER_IP   = "localhost"; // change to server IP when testing across laptops
    private static final int    SERVER_PORT = 9090;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private static final String F_HOME    = "home";
    private static final String F_AUTH    = "auth";
    private static final String F_DATE    = "date";
    private static final String F_TYPE    = "type";
    private static final String F_CONFIRM = "confirm";

    private String currentUser = null;
    private String selectedStartISO = null;
    private int    selectedDays = 1;

    private final JTextField     usernameField = new JTextField(18);
    private final JPasswordField passField  = new JPasswordField(18);

    private final JComboBox<String> startDateBox = new JComboBox<>();
    private final JSpinner          daysSpinner  = new JSpinner(new SpinnerNumberModel(1, 1, 7, 1));

    private final JRadioButton rbManual = new JRadioButton("Manual");
    private final JRadioButton rbAuto   = new JRadioButton("Automatic", true);
    private final JComboBox<String> seatsBox = new JComboBox<>(new String[]{"2", "5", "8"});
    private final DefaultComboBoxModel<String> availModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> availBox = new JComboBox<>(availModel);
    private final JButton reserveBtn = new JButton("Reserve");

    private final JLabel confirmLabel = new JLabel("Your reservation is confirmed.", SwingConstants.CENTER);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    public ClientGUI() {
        super("Car Rental System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 420);
        setLocationRelativeTo(null);

        root.add(buildHome(),    F_HOME);
        root.add(buildAuth(),    F_AUTH);
        root.add(buildDate(),    F_DATE);
        root.add(buildType(),    F_TYPE);
        root.add(buildConfirm(), F_CONFIRM);
        setContentPane(root);

        seedDateChoices();
        connectToServer();

        cards.show(root, F_HOME);
        setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            String greet = in.readLine();
            System.out.println("[Server] " + greet);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to connect to server.\n" + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Home =====
    private JPanel buildHome() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JLabel title = bigLabel("Welcome to Car Rental");
        c.gridx = 0; c.gridy = 0; p.add(title, c);

        JButton startBtn = new JButton("Start Booking");
        c.gridy = 1; p.add(startBtn, c);

        startBtn.addActionListener(e -> {
            usernameField.setText("");
            passField.setText("");
            cards.show(root, F_AUTH);
        });

        return p;
    }

    // ===== Register =====
    private JPanel buildAuth() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JLabel title = bigLabel("Create Account");
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(title, c);
        c.gridwidth = 1;

        c.gridy = 1; c.gridx = 0; p.add(new JLabel("Username :"), c);
        c.gridx = 1; p.add(usernameField, c);

        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Password:"), c);
        c.gridx = 1; p.add(passField, c);

        JButton done = new JButton("Register");
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; p.add(done, c);

        done.addActionListener(this::handleRegister);
        return p;
    }

    private void handleRegister(ActionEvent e) {
        String user = usernameField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            alert("Please enter both username and password.");
            return;
        }

        try {
            send("REGISTER " + user + " " + pass);
            String resp = read();
            if (resp != null && resp.toLowerCase().contains("successful")) {
                currentUser = user;
                info(resp);
                cards.show(root, F_DATE);
            } else {
                error(resp == null ? "No response from server." : resp);
            }
        } catch (IOException ex) {
            error("Network error: " + ex.getMessage());
        }
    }

    // ===== Date =====
    private JPanel buildDate() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JLabel title = bigLabel("Choose Date & Duration");
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(title, c);
        c.gridwidth = 1;

        c.gridy = 1; c.gridx = 0; p.add(new JLabel("Start Date:"), c);
        c.gridx = 1; p.add(startDateBox, c);

        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Number of Days:"), c);
        c.gridx = 1; p.add(daysSpinner, c);

        JButton next = new JButton("Next");
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2; p.add(next, c);

        next.addActionListener(ev -> {
            selectedDays = (Integer) daysSpinner.getValue();
            if (selectedDays < 1 || selectedDays > 7) {
                alert("Days must be between 1 and 7.");
                return;
            }
            selectedStartISO = startDateBox.getSelectedItem().toString(); // already ISO
            cards.show(root, F_TYPE);
        });

        return p;
    }

    private void seedDateChoices() {
        startDateBox.removeAllItems();
        ArrayList<String> dates = new ArrayList<>();
        dates.add("2025-01-01");
        dates.add("2025-01-02");
        dates.add("2025-01-03");
        dates.add("2025-01-04");
        dates.add("2025-01-05");
        dates.add("2025-01-06");
        dates.add("2025-01-07");
        for (String d : dates) startDateBox.addItem(d);
        startDateBox.setSelectedIndex(0);
    }

    // ===== Type / Search / Reserve =====
    private JPanel buildType() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        JLabel title = bigLabel("Select Car Type");
        c.gridx = 0; c.gridy = 0; c.gridwidth = 3; p.add(title, c);
        c.gridwidth = 1;

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbManual);
        bg.add(rbAuto);

        c.gridy = 1; c.gridx = 0; p.add(rbManual, c);
        c.gridx = 1; p.add(rbAuto, c);

        c.gridy = 2; c.gridx = 0; p.add(new JLabel("Seats:"), c);
        c.gridx = 1; p.add(seatsBox, c);

        JButton showBtn = new JButton("Show Available Cars");
        c.gridy = 3; c.gridx = 0; c.gridwidth = 3; p.add(showBtn, c);
        c.gridwidth = 1;

        JPanel listRow = new JPanel();
        listRow.add(new JLabel("Available cars:"));
        availBox.setPrototypeDisplayValue("MMMM");
        listRow.add(availBox);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 3; p.add(listRow, c);
        c.gridwidth = 1;

        reserveBtn.setEnabled(false);
        JButton backBtn = new JButton("Back");
        JPanel actions = new JPanel();
        actions.add(backBtn);
        actions.add(reserveBtn);
        c.gridy = 5; c.gridx = 0; c.gridwidth = 3; p.add(actions, c);

        showBtn.addActionListener(ev -> doSearch());
        availBox.addActionListener(ev ->
                reserveBtn.setEnabled(availModel.getSize() > 0 && availBox.getSelectedItem() != null));
        reserveBtn.addActionListener(ev -> doReserve());
        backBtn.addActionListener(ev -> cards.show(root, F_DATE));

        return p;
    }

    private void doSearch() {
        String type  = rbAuto.isSelected() ? "AUTO" : "MANUAL";
        String seats = (String) seatsBox.getSelectedItem();

        String cmd = "SEARCH type=" + type +
                     " seats=" + seats +
                     " start=" + selectedStartISO +
                     " days=" + selectedDays;

        try {
            send(cmd);
            String resp = read();

            SwingUtilities.invokeLater(() -> {
                availModel.removeAllElements();
                if (resp == null) { error("No response from server."); return; }

                String lower = resp.toLowerCase();
                if (lower.startsWith("no cars")) {
                    info(resp);
                    reserveBtn.setEnabled(false);
                    return;
                }

                if (lower.startsWith("available cars:")) {
                    int idx = resp.indexOf(':');
                    String list = idx >= 0 ? resp.substring(idx + 1).trim() : "";
                    for (String id : list.split(",")) {
                        String s = id.trim();
                        if (!s.isEmpty()) availModel.addElement(s);
                    }
                    reserveBtn.setEnabled(availModel.getSize() > 0);
                } else {
                    error(resp); // validation messages from server
                    reserveBtn.setEnabled(false);
                }
            });

        } catch (IOException ex) {
            error("Network error: " + ex.getMessage());
        }
    }

    private void doReserve() {
        String carId = (String) availBox.getSelectedItem();
        if (carId == null) { alert("Please select a car first."); return; }

        String cmd = "RESERVE car=" + carId +
                     " start=" + selectedStartISO +
                     " days=" + selectedDays;

        try {
            send(cmd);
            String resp = read();

            if (resp != null && resp.toLowerCase().startsWith("booking confirmed")) {
                SwingUtilities.invokeLater(() -> {
                    confirmLabel.setText(resp);
                    cards.show(root, F_CONFIRM);
                });
            } else {
                error(resp == null ? "No response from server." : resp);
            }

        } catch (IOException ex) {
            error("Network error: " + ex.getMessage());
        }
    }

    // ===== Confirmation =====
    private JPanel buildConfirm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = gbc();

        confirmLabel.setFont(confirmLabel.getFont().deriveFont(Font.BOLD, 16f));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(confirmLabel, c);
        c.gridwidth = 1;

        JButton done = new JButton("Done");
        c.gridy = 1; c.gridx = 0; c.gridwidth = 2; p.add(done, c);

        done.addActionListener(ev -> {
            availModel.removeAllElements();
            reserveBtn.setEnabled(false);
            cards.show(root, F_HOME);
        });

        return p;
    }

    // ===== utils =====
    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private JLabel bigLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 18f));
        return l;
    }

    private void send(String s) throws IOException {
        if (out == null) throw new IOException("Not connected to server.");
        out.println(s);
    }

    private String read() throws IOException {
        if (in == null) throw new IOException("Not connected to server.");
        return in.readLine();
    }

    private void alert(String msg) { JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE); }
    private void info (String msg)  { JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
    private void error(String msg)  { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE); }
}
