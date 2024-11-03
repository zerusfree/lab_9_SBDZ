import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginUI extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JPanel loginPanel;

    public LoginUI() {
        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Створення панелі входу
        loginPanel = new JPanel();
        add(loginPanel);
        loginPanel.setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 30, 80, 25);
        loginPanel.add(usernameLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(140, 30, 160, 25);
        loginPanel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 70, 80, 25);
        loginPanel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(140, 70, 160, 25);
        loginPanel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(140, 110, 160, 25);
        loginPanel.add(loginButton);

        // Додавання слухача на кнопку "Login"
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });
    }

    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        Connection connection = DatabaseConnection.connect();
        if (connection != null) {
            try {
                // Запит для перевірки наявності користувача та його ролі
                String query = "SELECT role FROM Users WHERE username = ? AND password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String role = resultSet.getString("role");
                    JOptionPane.showMessageDialog(this, "Login successful! Role: " + role);
                    this.dispose();

                    // Відкриваємо головне вікно на основі ролі
                    if ("admin".equals(role)) {
                        DatabaseUI adminUI = new DatabaseUI("admin");
                        adminUI.setVisible(true);
                    } else {
                        DatabaseUI userUI = new DatabaseUI("user");
                        userUI.setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during login: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginUI loginUI = new LoginUI();
            loginUI.setVisible(true);
        });
    }
}
