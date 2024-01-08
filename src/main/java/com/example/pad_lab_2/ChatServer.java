package com.example.pad_lab_2;

import lombok.RequiredArgsConstructor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

@RequiredArgsConstructor
public class ChatServer {
    private final int port;
    private final List<ClientParticipant> clients = new ArrayList<>();
    private final Map<ClientParticipant, List<String>> clientMessages = new HashMap<>();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ServerBroker listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                String ip = Arrays.toString(serverSocket.getInetAddress().getAddress());

                ClientParticipant clientParticipant = new ClientParticipant(clientSocket, this, ip, port);
                clients.add(clientParticipant);
                new Thread(clientParticipant).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message, ClientParticipant sender) {
        for (ClientParticipant client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }

        clientMessages.computeIfAbsent(sender, k -> new ArrayList<>()).add(message);
    }

    public void removeClient(ClientParticipant clientParticipant) {
        clients.remove(clientParticipant);
    }

    public String getOnlineClients() {
        StringBuilder onlineClients = new StringBuilder("Online clients:\n");
        for (ClientParticipant client : clients) {
            onlineClients.append(client.getClientName()).append("\n");
        }
        return onlineClients.toString();
    }

    public void sendPrivateMessage(String senderName, String recipientName, String message) {
        for (ClientParticipant client : clients) {
            if (client.getClientName().equals(recipientName)) {
                client.sendMessage("Private message from " + senderName + ": " + message);
                return;
            }
        }
        getClientByName(senderName).sendMessage("Recipient " + recipientName + " not found or offline.");
    }

    private ClientParticipant getClientByName(String clientName) {
        for (ClientParticipant client : clients) {
            if (client.getClientName().equals(clientName)) {
                return client;
            }
        }
        return null;
    }

    public void loadClientDataFromXML() {
        try {
            File file = new File("client_data.xml");

            if (file.exists()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(UserChatDataCollection.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                UserChatDataCollection userChatDataCollection = (UserChatDataCollection) jaxbUnmarshaller.unmarshal(file);

                if (userChatDataCollection.getUserChatDataList() != null) {
                    for (UserChatData userChatData : userChatDataCollection.getUserChatDataList()) {
                        System.out.println("Loaded client data:");
                        System.out.println("Client Name: " + userChatData.getClientName());
                        System.out.println("IP Address: " + userChatData.getIpAddress());
                        System.out.println("Port: " + userChatData.getPort());
                        System.out.println("Messages: " + userChatData.getLastMessage());
                        System.out.println("-----------------------");
                    }
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void saveClientDataToXML() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UserChatDataCollection.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            List<UserChatData> userChatDataList = new ArrayList<>();
            for (ClientParticipant clientParticipant : clients) {
                UserChatData userChatData = new UserChatData();
                userChatData.setClientName(clientParticipant.getClientName());
                userChatData.setIpAddress(clientParticipant.getClientAddress());
                userChatData.setPort(clientParticipant.getClientPort());
                userChatData.setMessages(clientMessages.getOrDefault(clientParticipant, new ArrayList<>()));
                userChatDataList.add(userChatData);
            }

            UserChatDataCollection data = new UserChatDataCollection();
            data.setUserChatDataList(userChatDataList);

            File file = new File("client_data.xml");
            jaxbMarshaller.marshal(data, file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(8080);
        server.loadClientDataFromXML();
        server.start();
    }
}
