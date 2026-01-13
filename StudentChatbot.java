import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class StudentChatbot extends JFrame {
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final String reg;
    private final String name;

    public StudentChatbot(String registerNo, String studentName) {
        this.reg = registerNo;
        this.name = studentName;

        setTitle("Class Connect — Chatbot (" + name + ")");
        setSize(580, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scroll = new JScrollPane(chatArea);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        bottom.add(inputField, BorderLayout.CENTER);

        JButton send = new JButton("Send");
        send.setFont(new Font("SansSerif", Font.BOLD, 13));
        bottom.add(send, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        send.addActionListener(e -> process());
        inputField.addActionListener(e -> process());

        appendBot("👋 Hi " + name + "! You can ask things like:\n" +
                "• 'IA1 DBMS' — to know marks\n" +
                "• 'Is my attendance safe?'\n" +
                "• 'Announcements'");
        setVisible(true);
    }

    private void appendBot(String s) {
        chatArea.append("Bot: " + s + "\n\n");
    }

    private void appendUser(String s) {
        chatArea.append("You: " + s + "\n");
    }

    private void process() {
        String q = inputField.getText().trim();
        if (q.isEmpty()) return;

        appendUser(q);
        inputField.setText("");
        String ql = q.toLowerCase();

        try {
            // === ATTENDANCE QUERY ===
            if (ql.contains("attendance") || ql.contains("safe")) {
                DataStore.Attendance a = DataStore.getAttendance(reg);
                if (a == null || a.presentPerSubject.isEmpty()) {
                    appendBot("No attendance record found for you yet.");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                double overallTotal = 0, overallPresent = 0;

                for (Map.Entry<String, Integer> entry : a.presentPerSubject.entrySet()) {
                    String subj = entry.getKey();
                    int present = entry.getValue();
                    int total = a.totalPerSubject.getOrDefault(subj, 0);
                    double pct = total == 0 ? 0.0 : (present * 100.0 / total);
                    sb.append(String.format("%s → %.1f%% (%d/%d)\n", subj, pct, present, total));

                    overallPresent += present;
                    overallTotal += total;
                }

                double overallPct = overallTotal == 0 ? 0.0 : (overallPresent * 100.0 / overallTotal);
                sb.append(String.format("\nOverall Attendance: %.1f%%\n", overallPct));

                String safeMsg = (overallPct >= 75)
                        ? "✅ Your attendance is safe!"
                        : "⚠️ Your attendance is low — try attending more classes.";
                appendBot(sb.toString() + safeMsg);
                return;
            }

            // === INTERNAL ASSESSMENT MARKS QUERY ===
            if (ql.contains("ia")) {
                String ia = ql.contains("ia2") ? "IA2" : "IA1";
                String[] subjects = {"DM", "ESS", "DS", "DBMS", "DPCO", "OOP"};
                String found = null;

                for (String s : subjects) {
                    if (ql.contains(s.toLowerCase())) {
                        found = s;
                        break;
                    }
                }

                if (found == null) {
                    appendBot("Please mention a subject (e.g., 'IA1 DBMS').");
                    return;
                }

                DataStore.Marks m = DataStore.getMarks(reg, ia, found);
                if (m == null)
                    appendBot("No marks found for " + found + " in " + ia + ".");
                else
                    appendBot(String.format("%s — %s %s:\nConcept: %d | CAT: %d | Total: %d\nRemark: %s",
                            name, found, ia, m.concept, m.cat, m.total, DataStore.getRemarkFor(m.total)));
                return;
            }

            // === ANNOUNCEMENTS QUERY ===
            if (ql.contains("announ")) {
                var anns = DataStore.getAnnouncements();
                if (anns.isEmpty())
                    appendBot("No announcements currently.");
                else {
                    StringBuilder sb = new StringBuilder("📢 Announcements:\n");
                    for (String a : anns)
                        sb.append("• ").append(a).append("\n");
                    appendBot(sb.toString());
                }
                return;
            }

            // === GREETINGS ===
            if (ql.matches(".*\\b(hi|hello|hey)\\b.*")) {
                appendBot("Hello " + name + "! Try:\n" +
                        "• 'IA1 DBMS'\n• 'Is my attendance safe?'\n• 'Announcements'");
                return;
            }

            // === DEFAULT RESPONSE ===
            appendBot("Sorry, I didn't get that. Try:\n'IA1 DBMS', 'Is my attendance safe?', or 'Announcements'.");

        } catch (Exception ex) {
            appendBot("⚠️ Error: " + ex.getMessage());
        }
    }
}
