package com.example.pad_lab_2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClientParticipant implements Runnable {
    private final Socket clientSocket;
    private final ChatServer server;
    private PrintWriter output;
    private String clientName;
    private final List<String> messages = new ArrayList<>();
    private final String clientAddress;
    private final int clientPort;

    @Override
    public void run() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            this.output = output;
            this.clientName = input.readLine();
            System.out.println(clientName + " connected");
            server.broadcastMessage(clientName + " joined the chat", this);
            String message;

            while ((message = input.readLine()) != null) {

                if (message.equals("/quit")) {
                    server.broadcastMessage(clientName + " left the chat", this);
                    System.out.println(clientName + " left the chat");

                } else if (message.startsWith("/private")) {
                    String[] parts = message.split(" ", 3);

                    if (parts.length == 3) {
                        String recipientName = parts[1];
                        String privateMessage = parts[2];
                        server.sendPrivateMessage(clientName, recipientName, privateMessage);
                    } else {
                        sendMessage("Invalid private message format. Usage: /private recipientName messageBody");
                    }

                } else if (message.equals("/online")) {
                    String onlineClients = server.getOnlineClients();
                    sendMessage(onlineClients);
                } else {
                    server.broadcastMessage(clientName + ": " + message, this);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String timestampedMessage = "[" + dateFormat.format(new Date()) + "] " + message;
                    messages.add(timestampedMessage);
                    server.saveClientDataToXML();
                }
                server.loadClientDataFromXML();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.removeClient(this);
            server.saveClientDataToXML();
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }
}

