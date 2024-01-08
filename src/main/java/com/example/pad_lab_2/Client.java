package com.example.pad_lab_2;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


@RequiredArgsConstructor
public class Client {
    private final String serverAddress;
    private final int serverPort;
    private String clientName;
    private volatile boolean isRunning = false;
    private Socket socket;

    private void readUsername() throws IOException {
        System.out.print("Enter your username: ");
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        clientName = consoleInput.readLine();
    }

    public void start() {
        try {
            readUsername();

            socket = new Socket(serverAddress, serverPort);

            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

                System.out.println("Connected to the server");

                output.println(clientName);

                new Thread(() -> {
                    String message;
                    try {
                        while ((message = input.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        if (!isRunning) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                String userInput;
                while ((userInput = consoleInput.readLine()) != null) {
                    output.println(userInput);
                    if ("/quit".equals(userInput)) {
                        System.out.println(" You left the chat");
                        isRunning = true;
                        socket.close();
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        isRunning = false;
    }

    public static void main(String[] args) {
        Client client = new Client("localhost", 8080);
        client.start();
    }
}