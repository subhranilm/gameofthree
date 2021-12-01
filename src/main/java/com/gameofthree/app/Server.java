package com.gameofthree.app;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Random;

public class Server extends WebSocketServer
{

    private static final String COMPLETED;
    private static final String HOST;
    private static final int PORT;
    private static final Random random;
    private static final int RANDOM_MAX;
    private static final int SUCCESS;
    private static final int FAILURE;

    static
    {
        random = new Random();
        RANDOM_MAX = 999999;
        SUCCESS = 0;
        FAILURE = 1;
        HOST = "localhost";
        PORT = 8887;
        COMPLETED = "COMPLETED";
    }

    public static void main(String[] args)
    {
        WebSocketServer server = new Server(new InetSocketAddress(HOST, PORT));
        server.run();
    }

    public Server(InetSocketAddress address)
    {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        int randomNumber = random.nextInt(RANDOM_MAX) + 1;
        System.out.println("Starting number: " + randomNumber);

        if (randomNumber == 1)
        {
            System.out.println("Winner!!");
            cleanupAndShutdown(conn, SUCCESS);
        }

        conn.send(String.valueOf(randomNumber));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {

        System.exit(SUCCESS);
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        if (message.equals(COMPLETED))
        {
            System.out.println("Game over. Better luck next time.");
            cleanupAndShutdown(conn, SUCCESS);
        }
        int current = 0;
        if (Objects.nonNull(message))
        {
            current = Integer.parseInt(message);
        }
        else
        {
            cleanupAndShutdown(conn, FAILURE);
        }

        System.out.println("Received from opponent: " + current);

        if (current == 1)
        {
            System.out.println("Congratulations, you have won!!");
            conn.send(COMPLETED);
            cleanupAndShutdown(conn, SUCCESS);
        }
        else if (current <= 0)
        {
            System.out.println("Got a negative number.");
            cleanupAndShutdown(conn, FAILURE);
        }

        String next = Long.toString(Math.round(((double) current) / 3));

        System.out.println("Sending number: " + next);

        conn.send(next);
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        System.err.println("Error with connection " + conn.getRemoteSocketAddress() + ":" + ex);
        cleanupAndShutdown(conn, FAILURE);
    }

    @Override
    public void onStart()
    {
        System.out.println("Server started successfully");
    }

    private void cleanupAndShutdown(WebSocket conn, int exitStatus)
    {
        conn.close();
        System.exit(exitStatus);
    }


}
