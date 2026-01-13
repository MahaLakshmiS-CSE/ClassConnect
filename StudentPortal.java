
import java.awt.*;
import java.io.*;
import javax.swing.*;

public class StudentPortal extends JFrame {

    private JTextArea announcementsArea;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JLabel attendanceLabel;
    private String studentName;

    public StudentPortal(String name) {
        this.studentName = name;
        setTitle("Student Portal - " + name);
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Announcements tab
        announcementsArea = new JTextArea(loadAnnouncements());
        announcementsArea.setEditable(false);

        // Chatbot tab
        JPanel chatPanel = createChatBotPanel();


        // Attendance info
        double percent = getAttendancePercentage(studentName);
        attendanceLabel = new JLabel("Your Attendance: " + String.format("%.2f", percent) + "%", SwingConstants.CENTER);
        attendanceLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📢 Announcements", new JScrollPane(announcementsArea));
        tabs.addTab("💬 ChatBot", chatPanel);

        add(attendanceLabel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createChatBotPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatInput = new JTextField();

        chatInput.addActionListener(e -> processChat());

        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
JButton sendButton = new JButton("Send");
sendButton.addActionListener(e -> processChat());
JPanel inputPanel = new JPanel(new BorderLayout());
inputPanel.add(chatInput, BorderLayout.CENTER);
inputPanel.add(sendButton, BorderLayout.EAST);
panel.add(inputPanel, BorderLayout.SOUTH);

        chatArea.append("🤖 Bot: Hi " + studentName + "! You can ask me about your attendance or announcements.\n");
        return panel;
    }

    private void processChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty()) {
            return;
        }

        chatArea.append("👩‍🎓 You: " + msg + "\n");
        chatInput.setText("");

        String reply;
        String lower = msg.toLowerCase();

        if (lower.contains("hi") || lower.contains("hello")) {
            reply = "🤖 Bot: Hello " + studentName + "! How’s your day?"; 
        }else if (lower.contains("attendance")) {
            reply = "🤖 Bot: You have " + String.format("%.2f", getAttendancePercentage(studentName)) + "% attendance."; 
        }else if (lower.contains("announcement")) {
            reply = "🤖 Bot: You can check the latest announcements in the first tab!"; 
        }else if (lower.contains("thanks")) {
            reply = "🤖 Bot: You’re welcome, " + studentName + "! 😊"; 
        }else {
            reply = "🤖 Bot: Hmm… I didn’t get that. Try asking about attendance or announcements.";
        }

        chatArea.append(reply + "\n\n");
    }

    private String loadAnnouncements() {
        StringBuilder sb = new StringBuilder();
        File file = new File("announcements.txt");
        if (!file.exists()) {
            return "No announcements yet.";
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Error loading announcements.";
        }
        return sb.toString();
    }

private double getAttendancePercentage(String studentName) {
    File file = new File("attendance.txt");
    if (!file.exists()) {
        return 0.0;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length >= 3 && data[0].trim().equalsIgnoreCase(studentName.trim())) {
                int attended = Integer.parseInt(data[1].trim());
                int total = Integer.parseInt(data[2].trim());
                return total == 0 ? 0.0 : (attended * 100.0 / total);
            }
        }
    } catch (IOException | NumberFormatException e) {
        return 0.0;
    }
    return 0.0;
}
}