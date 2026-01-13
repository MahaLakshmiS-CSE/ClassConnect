import java.awt.*;
import java.util.*;
import javax.swing.*;

public class AttendanceManager extends JFrame {
    private final DefaultListModel<String> regModel = new DefaultListModel<>();
    private final JList<String> regList = new JList<>(regModel);
    private final JTextArea announceBox = new JTextArea(4, 40);

    // subjects and fixed total classes
    private final String[] subjects = {"DM", "DS", "DBMS", "DPCO", "OOP", "ESS"};
    private final Map<String, Integer> totalClasses = Map.of(
            "DM", 5, "DS", 4, "DBMS", 7, "DPCO", 6, "OOP", 7, "ESS", 4
    );

    // per-subject attendance fields
    private final Map<String, JTextField> presentFields = new HashMap<>();

    public AttendanceManager() {
        setTitle("Teacher Dashboard - Attendance & Announcements");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Background panel with image
        JPanel background = new JPanel() {
            Image bg = new ImageIcon("college_bg.jpeg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        // Main panel (centered)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0, 0, 0, 170)); // semi-transparent dark background
        mainPanel.setOpaque(true);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        // Left: student list
        for (String reg : DataStore.getAllRegisterNos()) regModel.addElement(reg);
        regList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(new JScrollPane(regList), BorderLayout.CENTER);
        left.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Students (Register No)", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        // Right: attendance fields
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        int row = 0;
        for (String subj : subjects) {
            JLabel lbl = new JLabel(subj + " (Present / " + totalClasses.get(subj) + "):");
            lbl.setForeground(Color.WHITE);
            JTextField tf = new JTextField(5);
            presentFields.put(subj, tf);
            c.gridx = 0; c.gridy = row; right.add(lbl, c);
            c.gridx = 1; right.add(tf, c);
            row++;
        }

        JButton loadBtn = new JButton("Load Selected");
        JButton saveBtn = new JButton("Save Attendance");
        JButton calcBtn = new JButton("Show Attendance %");

        c.gridx = 0; c.gridy = row; right.add(loadBtn, c);
        c.gridx = 1; right.add(saveBtn, c);
        c.gridx = 0; c.gridy = row + 1; right.add(calcBtn, c);

        // Announcement panel
        JPanel ann = new JPanel(new BorderLayout());
        ann.setOpaque(false);
        ann.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE), "Post Announcement", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        ann.add(new JScrollPane(announceBox), BorderLayout.CENTER);
        JButton postAnn = new JButton("Post Announcement");
        ann.add(postAnn, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.add(right, BorderLayout.NORTH);
        rightPanel.add(ann, BorderLayout.CENTER);

        mainPanel.add(left);
        mainPanel.add(rightPanel);

        background.add(mainPanel); // centered panel

        // --- Action listeners ---
        loadBtn.addActionListener(e -> {
            String reg = regList.getSelectedValue();
            if (reg == null) { JOptionPane.showMessageDialog(this, "Select a student first."); return; }
            DataStore.Attendance a = DataStore.getAttendance(reg);
            if (a != null) {
                for (String subj : subjects) {
                    presentFields.get(subj).setText(String.valueOf(a.presentPerSubject.getOrDefault(subj, 0)));
                }
            } else {
                for (String subj : subjects) presentFields.get(subj).setText("0");
            }
        });

        saveBtn.addActionListener(e -> {
            String reg = regList.getSelectedValue();
            if (reg == null) { JOptionPane.showMessageDialog(this, "Select a student first."); return; }
            DataStore.Attendance a = new DataStore.Attendance();
            try {
                for (String subj : subjects) {
                    int val = Integer.parseInt(presentFields.get(subj).getText().trim());
                    if (val < 0 || val > totalClasses.get(subj)) throw new NumberFormatException();
                    a.presentPerSubject.put(subj, val);
                }
DataStore.updateAttendance(reg, a.presentPerSubject, a.totalPerSubject);
                JOptionPane.showMessageDialog(this, "Attendance updated successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid numbers within total classes for each subject.");
            }
        });

        calcBtn.addActionListener(e -> {
            StringBuilder out = new StringBuilder();
            for (String reg : DataStore.getAllRegisterNos()) {
                DataStore.Attendance a = DataStore.getAttendance(reg);
                out.append(reg).append(":\n");
                for (String subj : subjects) {
                    int present = a != null ? a.presentPerSubject.getOrDefault(subj, 0) : 0;
                    int total = totalClasses.get(subj);
                    double pct = total == 0 ? 0 : present * 100.0 / total;
                    out.append(String.format(" %s: %d/%d → %.1f%%\n", subj, present, total, pct));
                }
                out.append("\n");
            }
            JTextArea ta = new JTextArea(out.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Attendance Summary", JOptionPane.INFORMATION_MESSAGE);
        });

        postAnn.addActionListener(e -> {
            String text = announceBox.getText().trim();
            if (text.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter announcement text."); return; }
            DataStore.addAnnouncement(text);
            announceBox.setText("");
            JOptionPane.showMessageDialog(this, "Announcement posted.");
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AttendanceManager::new);
    }
}
