import com.sun.deploy.panel.JSmartTextArea;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame {
    private static final int PORT = 7777;
    private static final int MAX_USERS = 100;

    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket serverSocket;
    private Socket connection;

    public Server() {
        super("Server part");
        userInputText = new JTextField();
        userInputText.setEditable(false);
        userInputText.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage(e.getActionCommand());
                        userInputText.setText("");
                    }
                });
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JSmartTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 600);
        setVisible(true);
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT, MAX_USERS);
            while (true) {
                try {
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                } catch (EOFException eofException) {
                    showMessage("\nServer wrong connection!");
                } finally {
                    closeConnection();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        showMessage("\nWait client connection");
        connection = serverSocket.accept();
        showMessage("\nConnection create, IP=" + connection.getInetAddress().getHostAddress());
    }

    private void setupStreams() throws IOException {
        outputStream = new ObjectOutputStream(connection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream());
        showMessage("\nStream create");
    }

    private void whileChatting() throws IOException {
        String message = "Hi, you are connected";
        sendMessage(message);
        readyToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNorFoundException) {
                showMessage("\nWrong data from user");
            }
        } while (!message.equals("Client - *"));
    }

    private void closeConnection() {
        showMessage("\nConnection was closed");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            connection.close();
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    private void sendMessage(String message){
        try {
            outputStream.writeObject("SERVER - " + message);
            outputStream.flush();
            showMessage("\nSERVER" + message);
        }catch (IOException e){
            chatWindow.append("\nError!!! Dude, I cann't send it");
        }
    }

    private void showMessage(final String text){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append(text);
            }
        });
    }

    private void readyToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userInputText.setEditable(tof);
                    }
                }
        );
    }
}
