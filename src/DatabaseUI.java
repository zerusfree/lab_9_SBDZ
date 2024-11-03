import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.sql.*;

public class DatabaseUI extends JFrame {

    private final String userRole;
    private JTable table;
    private JComboBox<String> tableSelector;
    private JComboBox<String> actionSelector;
    private JComboBox<String> filterFieldSelector;
    private JTextField filterValueField;
    private JPanel controlPanel;
    private JPanel imagesPanel;

    private JPanel navigationPanel;
    private int currentRow = -1;

    public DatabaseUI(String role) {
        this.userRole = role;
        setTitle("Database Management with Images");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Створення таблиці для відображення даних
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Панель для вибору таблиці і дій
        controlPanel = new JPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Вибір таблиці
        tableSelector = new JComboBox<>(new String[]{"Users"});
        controlPanel.add(new JLabel("Select Table:"));
        controlPanel.add(tableSelector);

        // Вибір дії
        actionSelector = new JComboBox<>(new String[]{"Select", "Insert", "Delete"});
        controlPanel.add(new JLabel("Select Action:"));
        controlPanel.add(actionSelector);

        // Вибір поля для фільтрації
        filterFieldSelector = new JComboBox<>();
        controlPanel.add(new JLabel("Filter Field:"));
        controlPanel.add(filterFieldSelector);

        // Поле для введення значення фільтра
        filterValueField = new JTextField(15);
        controlPanel.add(new JLabel("Filter Value:"));
        controlPanel.add(filterValueField);

        // Кнопка для виконання дії
        JButton executeButton = new JButton("Execute");
        controlPanel.add(executeButton);

        // Панель для відображення фото
        imagesPanel = new JPanel();
        imagesPanel.setLayout(new FlowLayout());
        JScrollPane imageScrollPane = new JScrollPane(imagesPanel);
        imageScrollPane.setPreferredSize(new Dimension(800, 150));
        add(imageScrollPane, BorderLayout.SOUTH);

        // Панель навігації з кнопками
        navigationPanel = new JPanel();
        add(navigationPanel, BorderLayout.SOUTH);

        // Кнопки навігації
        JButton searchButton = new JButton("Search");
        JButton addButton = new JButton("Add");
        JButton deleteButton = new JButton("Delete");
        JButton nextButton = new JButton("Next");
        JButton prevButton = new JButton("Previous");
        JButton deleteByProcedureButton = new JButton("Delete (Procedure)");

        Dimension buttonSize = new Dimension(100, 30);
        searchButton.setPreferredSize(buttonSize);
        addButton.setPreferredSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);
        nextButton.setPreferredSize(buttonSize);
        prevButton.setPreferredSize(buttonSize);
        deleteByProcedureButton.setPreferredSize(buttonSize);

        navigationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navigationPanel.add(searchButton);
        navigationPanel.add(addButton);
        navigationPanel.add(deleteButton);
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(deleteByProcedureButton);


        // Основна панель для зображень і навігації
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        southPanel.add(imageScrollPane, BorderLayout.NORTH); // Додаємо imagesPanel зверху
        southPanel.add(navigationPanel, BorderLayout.SOUTH); // Додаємо навігаційну панель знизу

        add(southPanel, BorderLayout.SOUTH); // Додаємо основну панель у південну частину

        if (!"admin".equals(userRole)) {
            addButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        // Додавання слухача подій на кнопку
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tableName = (String) tableSelector.getSelectedItem();
                String action = (String) actionSelector.getSelectedItem();
                if ("Select".equals(action)) {
                    loadData(tableName);
                } else if ("Insert".equals(action)) {
                    insertData(tableName);
                } else if ("Delete".equals(action)) {
                    deleteData(tableName);
                }
            }
        });

        deleteByProcedureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userIdStr = JOptionPane.showInputDialog("Enter User ID to Delete (Procedure):");
                if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                    try {
                        int userId = Integer.parseInt(userIdStr.trim());
                        deleteUserProcedure(userId);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(DatabaseUI.this, "Invalid User ID format. Please enter a valid number.");
                    }
                }
            }
        });

        // Слухач для вибору рядка в таблиці
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                currentRow = table.getSelectedRow();
                displayUserImage();
            }
        });

        // Оновлюємо поля для фільтрування відповідно до вибраної таблиці
        tableSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilterFields((String) tableSelector.getSelectedItem());
            }
        });

        // Оновлюємо поля для фільтрування при першому запуску
        updateFilterFields((String) tableSelector.getSelectedItem());

        // Дії для кнопок навігації
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData((String) tableSelector.getSelectedItem());
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertData((String) tableSelector.getSelectedItem());
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData((String) tableSelector.getSelectedItem());
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRow < table.getRowCount() - 1) {
                    currentRow++;
                    table.setRowSelectionInterval(currentRow, currentRow);
                    scrollToRow(currentRow);
                }
            }
        });

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRow > 0) {
                    currentRow--;
                    table.setRowSelectionInterval(currentRow, currentRow);
                    scrollToRow(currentRow);
                }
            }
        });
    }

    private void updateFilterFields(String tableName) {
        filterFieldSelector.removeAllItems();
        if ("Users".equals(tableName)) {
            filterFieldSelector.addItem("user_id");
            filterFieldSelector.addItem("first_name");
            filterFieldSelector.addItem("last_name");
            filterFieldSelector.addItem("email");
            filterFieldSelector.addItem("phone");
            filterFieldSelector.addItem("username");
            filterFieldSelector.addItem("role");
            filterFieldSelector.addItem("photo_url");
        }
    }

    private void loadData(String tableName) {
        Connection connection = DatabaseConnection.connect();
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                String filterField = (String) filterFieldSelector.getSelectedItem();
                String filterValue = filterValueField.getText().trim();
                String query = "SELECT * FROM " + tableName;
                if (!filterValue.isEmpty()) {
                    query += " WHERE " + filterField + " = '" + filterValue + "'";
                }
                ResultSet resultSet = statement.executeQuery(query);

                // Переміщуємо курсор до першого запису
                if (!resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "No records found.");
                    return;
                }

                int columnCount = resultSet.getMetaData().getColumnCount();
                String[] columnNames = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i - 1] = resultSet.getMetaData().getColumnName(i);
                }
                DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

                // Додаємо перший рядок до моделі
                Object[] firstRow = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    firstRow[i - 1] = resultSet.getObject(i);
                }
                tableModel.addRow(firstRow);

                // Додаємо інші рядки до таблиці
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = resultSet.getObject(i);
                    }
                    tableModel.addRow(row);
                }

                table.setModel(tableModel);
                if (table.getRowCount() > 0) {
                    currentRow = 0;
                    table.setRowSelectionInterval(currentRow, currentRow);
                    displayUserImage();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
            }
        }
    }


    private void insertData(String tableName) {
        if ("admin".equals(userRole) && "Users".equals(tableName)) {
            String firstName = JOptionPane.showInputDialog("Enter First Name:");
            String lastName = JOptionPane.showInputDialog("Enter Last Name:");
            String email = JOptionPane.showInputDialog("Enter Email:");
            String phone = JOptionPane.showInputDialog("Enter Phone:");
            String username = JOptionPane.showInputDialog("Enter Username:");
            String password = JOptionPane.showInputDialog("Enter Password:");
            String role = JOptionPane.showInputDialog("Enter Role:");

            // Використовуємо метод вибору файлу для отримання шляху до зображення
            String photoUrl = chooseImageFile();
            if (photoUrl != null) {
                addUserProcedure(firstName, lastName, email, phone, username, password, role, photoUrl);
            } else {
                JOptionPane.showMessageDialog(this, "No image selected.");
            }
        } else if ("Users".equals(tableName)) {
            JOptionPane.showMessageDialog(this, "You don't have permission to add new users!");
        }
    }
    // Додаємо метод для вибору файлу через провідник
    private String chooseImageFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    private void deleteData(String tableName) {
        Connection connection = DatabaseConnection.connect(); // Підключення до бази даних
        if (connection != null) {
            try {
                // Отримуємо вибране поле для фільтра і значення фільтра
                String filterField = (String) filterFieldSelector.getSelectedItem();
                String filterValue = filterValueField.getText().trim();

                if (filterValue.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please provide a filter value to delete records.");
                    return;
                }

                // Підтвердження перед видаленням
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete records where " + filterField + " = '" + filterValue + "'?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return; // Якщо користувач натиснув "Ні", виходимо з методу
                }

                // Створення SQL-запиту на видалення
                String query = "DELETE FROM " + tableName + " WHERE " + filterField + " = ?";

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, filterValue);

                // Виконання запиту на видалення
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Record(s) deleted successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "No records found to delete.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error while deleting data: " + e.getMessage());
            } finally {
                try {
                    connection.close(); // Закриваємо з'єднання
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.");
        }
    }
    private void deleteUserProcedure(int userId) {
        Connection connection = DatabaseConnection.connect();
        if (connection != null) {
            try {
                CallableStatement callableStatement = connection.prepareCall("CALL DeleteUser(?)");
                callableStatement.setInt(1, userId);

                int rowsAffected = callableStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "No user found with the provided ID.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error during deletion: " + e.getMessage());
            }
        }
    }
    private void addUserProcedure(String firstName, String lastName, String email, String phone, String username, String password, String role, String photoUrl) {
        if ("admin".equals(userRole)) {
            Connection connection = DatabaseConnection.connect();
            if (connection != null) {
                try {
                    CallableStatement callableStatement = connection.prepareCall("CALL AddUser(?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    callableStatement.setString(1, firstName);
                    callableStatement.setString(2, lastName);
                    callableStatement.setString(3, email);
                    callableStatement.setString(4, phone);
                    callableStatement.setString(5, username);
                    callableStatement.setString(6, password);
                    callableStatement.setString(7, role);
                    callableStatement.setString(8, photoUrl);
                    callableStatement.registerOutParameter(9, Types.INTEGER);

                    callableStatement.executeUpdate();
                    int newUserId = callableStatement.getInt(9);
                    JOptionPane.showMessageDialog(this, "User added successfully! New User ID: " + newUserId);
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error during user insertion: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Only Admin can add new users!");
        }
    }

    private void scrollToRow(int row) {
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
    }

    private void displayUserImage() {
        imagesPanel.removeAll();
        if (currentRow != -1) {
            Object photoUrlObj = table.getValueAt(currentRow, table.getColumnModel().getColumnIndex("photo_url"));
            if (photoUrlObj != null) {
                String photoUrl = photoUrlObj.toString();
                if (!photoUrl.startsWith("http://") && !photoUrl.startsWith("https://")) {
                    // Формуємо правильний шлях для локальних файлів
                    photoUrl = new File(photoUrl).toURI().toString();
                }
                try {
                    URL url = new URL(photoUrl);
                    Image image = ImageIO.read(url).getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    JLabel imageLabel = new JLabel(new ImageIcon(image));
                    imagesPanel.add(imageLabel);
                } catch (Exception e) {
                    e.printStackTrace();
                    JLabel errorLabel = new JLabel("Failed to load image");
                    imagesPanel.add(errorLabel);
                }
            }
        }
        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    // Метод для відображення зображення
    private void addImageToPanel(String imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath);
        Image scaledImage = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);  // Зменшуємо розмір зображень
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imagesPanel.add(imageLabel);
        imagesPanel.revalidate();
        imagesPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DatabaseUI ui = new DatabaseUI("admin");  // Replace "admin" with the actual role of the logged-in user.
                ui.setVisible(true);
            }
        });
    }
}
