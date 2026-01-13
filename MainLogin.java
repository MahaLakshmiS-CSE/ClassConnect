import java.awt.*;
import javax.swing.*;

/**
 * Single login window for Student / Teacher.
 * Student: Email (must end with @licet.ac.in) + RegisterNo + password (last 4 digits)
 * Teacher: Email (must end with @licet.ac.in) + password teacher123
 */
public class MainLogin extends JFrame {
    public MainLogin() {
        setTitle("Class Connect - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // background panel with image (if present)
        JPanel background = new JPanel() {
            Image bg = new ImageIcon("college_bg.jpeg").getImage();
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(new Color(0,0,0,160));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);

        // logo
        try {
            JLabel logo = new JLabel(new ImageIcon("college_logo.png"));
            c.gridx=0; c.gridy=0; c.gridwidth=2;
            main.add(logo, c);
            c.gridwidth=1;
        } catch (Exception ignored) {}

        JRadioButton rbStudent = new JRadioButton("Student", true);
        JRadioButton rbTeacher = new JRadioButton("Teacher");
        ButtonGroup g = new ButtonGroup(); g.add(rbStudent); g.add(rbTeacher);
        rbStudent.setForeground(Color.WHITE); rbTeacher.setForeground(Color.WHITE);
        rbStudent.setOpaque(false); rbTeacher.setOpaque(false);
        c.gridx=0; c.gridy=1; main.add(rbStudent,c); c.gridx=1; main.add(rbTeacher,c);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setForeground(Color.WHITE);
        JTextField tfEmail = new JTextField(20);
        c.gridx=0; c.gridy=2; main.add(lblEmail,c); c.gridx=1; main.add(tfEmail,c);

        JLabel lblReg = new JLabel("Register No:");
        lblReg.setForeground(Color.WHITE);
        JTextField tfReg = new JTextField(20);
        c.gridx=0; c.gridy=3; main.add(lblReg,c); c.gridx=1; main.add(tfReg,c);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setForeground(Color.WHITE);
        JPasswordField pf = new JPasswordField(20);
        c.gridx=0; c.gridy=4; main.add(lblPass,c); c.gridx=1; main.add(pf,c);

        JButton btnLogin = new JButton("Login");
        c.gridx=1; c.gridy=5; main.add(btnLogin,c);

        // toggle register visibility
        rbStudent.addActionListener(a -> { lblReg.setVisible(true); tfReg.setVisible(true); });
        rbTeacher.addActionListener(a -> { lblReg.setVisible(false); tfReg.setVisible(false); });

        // login action: uses DataStore.verifyStudent / verifyTeacher
        btnLogin.addActionListener(e -> {
            String email = tfEmail.getText().trim();
            String reg = tfReg.getText().trim();
            String pass = new String(pf.getPassword()).trim();
            boolean isStudent = rbStudent.isSelected();

            if (email.isEmpty() || pass.isEmpty() || (isStudent && reg.isEmpty())) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }
            if (!email.toLowerCase().endsWith("@licet.ac.in")) {
                JOptionPane.showMessageDialog(this, "Use your LICET institutional email only (@licet.ac.in).");
                return;
            }


            if (isStudent) {
                if (DataStore.verifyStudent(reg, pass)) {
                    DataStore.Student s = DataStore.getStudentByRegister(reg);
                    String name = s != null ? s.name : reg;
                    JOptionPane.showMessageDialog(this, "Welcome " + name + "!");
                    dispose();
                    SwingUtilities.invokeLater(() -> new StudentDashboard(reg, name));
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid student credentials. Password is last 4 digits of RegisterNo.");
                }
            } else {
                if (DataStore.verifyTeacher(email, pass)) {
                    JOptionPane.showMessageDialog(this, "Teacher login successful!");
                    dispose();
                    SwingUtilities.invokeLater(() -> new AttendanceManager());
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid teacher credentials. Use your @licet.ac.in and password teacher123.");
                }
            }
        });

        background.add(main);
        setVisible(true);
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(MainLogin::new); }
}
