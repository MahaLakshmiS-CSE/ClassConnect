import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class StudentDashboard extends JFrame {
    private final String registerNo;
    private final String studentName;

    public StudentDashboard(String registerNo, String studentName) {
        this.registerNo = registerNo;
        this.studentName = studentName;
        initUI();
    }

    private void initUI() {
        setTitle("Class Connect - " + studentName);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Background setup
        JLabel bgLabel = null;
        try {
            bgLabel = new JLabel(new ImageIcon("college_bg.jpeg"));
            bgLabel.setLayout(new BorderLayout());
            setContentPane(bgLabel);
        } catch (Exception ignored) {}

        // === Header Panel ===
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 50, 15, 50)); // spacing

        // Left: college logo
        JLabel logoLabel = new JLabel();
        try {
            logoLabel.setIcon(new ImageIcon("college_logo.png"));
        } catch (Exception ignored) {}
        topPanel.add(logoLabel, BorderLayout.WEST);

        // Center: title text
        JLabel titleLabel = new JLabel(
                "<html><div style='text-align:center; font-size:20px; font-weight:bold;'>"
                        + "Class Connect — Student Portal (" + studentName + ")"
                        + "</div></html>",
                SwingConstants.CENTER);
        titleLabel.setForeground(Color.BLACK);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        // Right: spacing
        topPanel.add(Box.createHorizontalStrut(100), BorderLayout.EAST);

        if (bgLabel != null)
            bgLabel.add(topPanel, BorderLayout.NORTH);
        else
            add(topPanel, BorderLayout.NORTH);

        // === Tabs ===
        JTabbedPane tabs = new JTabbedPane();

        // ---------- Marks Tab ----------
        JTextArea marksArea = new JTextArea();
        marksArea.setEditable(false);
        marksArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        StringBuilder sbMarks = new StringBuilder();
        DataStore.Student sObj = DataStore.getStudentByRegister(registerNo);
        sbMarks.append("Student: ").append(sObj != null ? sObj.name : studentName)
                .append("    Reg: ").append(registerNo).append("\n\n");

        Map<String, DataStore.Marks> ia1 = DataStore.getMarksForIA(registerNo, "IA1");
        String[] prim = {"DM", "ESS", "DS", "DBMS", "DPCO", "OOP"};
        if (ia1 == null) sbMarks.append("No IA1 marks found.\n");
        else {
            for (String sub : prim) {
                DataStore.Marks m = ia1.get(sub);
                if (m != null)
                    sbMarks.append(String.format("%-6s -> %3d    (%2d CT + %2d CAT)    Remark: %s\n",
                            sub, m.total, m.concept, m.cat, DataStore.getRemarkFor(m.total)));
                else sbMarks.append(String.format("%-6s -> N/A\n", sub));
            }
        }
        marksArea.setText(sbMarks.toString());
        tabs.add("Internal Marks (IA1)", new JScrollPane(marksArea));

        // ---------- Attendance Tab ----------
        JTextArea attArea = new JTextArea();
        attArea.setEditable(false);
        attArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        DataStore.Attendance a = DataStore.getAttendance(registerNo);
        if (a == null || a.presentPerSubject == null || a.presentPerSubject.isEmpty()) {
            attArea.setText("No attendance record found.");
        } else {
            StringBuilder sb = new StringBuilder();
            double overallTotal = 0, overallPresent = 0;

            for (String subj : a.presentPerSubject.keySet()) {
                int present = a.presentPerSubject.getOrDefault(subj, 0);
                int total = a.totalPerSubject.getOrDefault(subj, 0);
                double pct = total == 0 ? 0.0 : (present * 100.0 / total);

                sb.append(String.format("%s:\nPresent: %d  |  Total: %d  |  Attendance: %.1f%%\n\n",
                        subj, present, total, pct));

                overallPresent += present;
                overallTotal += total;
            }

            double overallPct = overallTotal == 0 ? 0.0 : (overallPresent * 100.0 / overallTotal);
            sb.append(String.format("Overall Attendance: %.1f%%\n%s",
                    overallPct,
                    (overallPct >= 75 ? "✅ Your attendance is safe."
                            : "⚠️ Your attendance is not safe — attend more classes.")));

            attArea.setText(sb.toString());
        }
        tabs.add("Attendance", new JScrollPane(attArea));

        // ---------- Announcements Tab ----------
        JTextArea annArea = new JTextArea();
        annArea.setEditable(false);
        annArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        var anns = DataStore.getAnnouncements();

        if (anns.isEmpty())
            annArea.setText("No announcements currently.");
        else {
            StringBuilder asb = new StringBuilder();
            for (String an : anns)
                asb.append("• ").append(an).append("\n\n");
            annArea.setText(asb.toString());
        }
        tabs.add("Announcements", new JScrollPane(annArea));

        // ---------- Chatbot Tab ----------
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setOpaque(false);
        JButton chatBtn = new JButton("Open Chatbot");
        chatBtn.addActionListener(e -> new StudentChatbot(registerNo, studentName));

        chatPanel.add(new JLabel("Chatbot for quick queries (attendance, IA marks, announcements)"),
                BorderLayout.NORTH);
        chatPanel.add(chatBtn, BorderLayout.CENTER);
        tabs.add("Chatbot", chatPanel);

        // Add everything to frame
        if (bgLabel != null)
            bgLabel.add(tabs, BorderLayout.CENTER);
        else
            add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }
}
