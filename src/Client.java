import com.sun.corba.se.impl.io.IIOPInputStream;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.awt.*;

public class Client extends JFrame{
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;
    private String message = "";
    private String serverIP;

    public Client(String host){
        super("Client");
        serverIP = host;
        userInputText = new JTextField();
        userInputText.setEditable(false);
        userInputText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                userInputText.setText("");
            }
        });
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        chatWindow.setBackground(Color.LIGHT_GRAY);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300, 600);
        setVisible(true);
    }

    public void startClient(){
        try{
           connectToServer();
           setupStreams();
           whileChatting();
        }catch (EOFException eofException){
            showMessage("\nClient broke  connect");
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            closeConnection();
        }
    }

    private void connectToServer() throws IOException{
        showMessage("\nTry connect, dude");
        socket = new Socket(InetAddress.getByName(serverIP), 7777);
        showMessage("\nConnect was create, port is " + socket.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException{
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("\nStreams was create");
    }

    private void whileChatting(){
        readyToType(true);
        do {
            try {
                message = (String) inputStream.readObject();
                showMessage("\n " + message);
            } catch (ClassNotFoundException e){
                showMessage("\nWrong message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }while (!message.equals("SERVER - *"));
    }

    private void closeConnection() {
        showMessage("\nConnections are closing");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void sendMessage(String message){
        try {
            outputStream.writeObject("Client - " + message);
            outputStream.flush();
            showMessage("\nClient - " + message) ;
        }catch (IOException e){
            chatWindow.append("\nWrong data with try to send message");
        }
    }

    private void showMessage(final String message){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatWindow.append("\n" + message);
            }
        });
    }

    private void readyToType(final boolean tOF){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                userInputText.setEditable(tOF);
            }
        });
    }

}
