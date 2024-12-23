package com.example.javafinals;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class ChatServer extends WebSocketServer {

    // Mapping of usernames to their WebSocket connections
    private static Map<String, WebSocket> userConnections = new HashMap<>();

    public ChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection from " + conn.getRemoteSocketAddress());
        // Expect the client to send a "register" message with username
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String disconnectedUser = null;
        for (Map.Entry<String, WebSocket> entry : userConnections.entrySet()) {
            if (entry.getValue().equals(conn)) {
                disconnectedUser = entry.getKey();
                break;
            }
        }
        if (disconnectedUser != null) {
            userConnections.remove(disconnectedUser);
            System.out.println(disconnectedUser + " disconnected.");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "register":
                    String username = json.getString("username");
                    userConnections.put(username, conn);
                    System.out.println(username + " registered.");
                    break;

                case "chat":
                    String sender = json.getString("sender");
                    String receiver = json.getString("receiver");
                    String chatMessage = json.getString("message");

                    // Store the message in the database
                    DatabaseController.storeMessage(sender, receiver, chatMessage);

                    // Send the message to the receiver if online
                    WebSocket receiverConn = userConnections.get(receiver);
                    if (receiverConn != null && receiverConn.isOpen()) {
                        JSONObject chatJson = new JSONObject();
                        chatJson.put("type", "chat");
                        chatJson.put("sender", sender);
                        chatJson.put("message", chatMessage);
                        receiverConn.send(chatJson.toString());
                    }
                    break;

                case "history":
                    String user1 = json.getString("user1");
                    String user2 = json.getString("user2");

                    ResultSet rs = DatabaseController.getChatHistory(user1, user2);
                    JSONObject historyJson = new JSONObject();
                    historyJson.put("type", "history");
                    historyJson.put("user1", user1);
                    historyJson.put("user2", user2);

                    // Fetch messages
                    while (rs.next()) {
                        JSONObject msg = new JSONObject();
                        msg.put("sender", rs.getString("sender_username"));
                        msg.put("receiver", rs.getString("receiver_username"));
                        msg.put("message", rs.getString("message"));
                        msg.put("timestamp", rs.getTimestamp("timestamp").toString());
                        historyJson.append("messages", msg);
                    }

                    conn.send(historyJson.toString());
                    break;

                default:
                    System.out.println("Unknown message type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject errorJson = new JSONObject();
            errorJson.put("type", "error");
            errorJson.put("message", "Invalid message format.");
            conn.send(errorJson.toString());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if(conn != null){
            // Handle specific connection errors if needed
        }
    }

    @Override
    public void onStart() {
        System.out.println("Chat WebSocket Server started!");
    }

    public static void main(String[] args) {
        int port = 8887; // Choose an appropriate port
        ChatServer server = new ChatServer(port);
        server.start();
        System.out.println("Chat Server started on port: " + server.getPort());
    }
}
