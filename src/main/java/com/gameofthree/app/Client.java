package com.gameofthree.app;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Objects;

public class Client extends WebSocketClient
{

    private static final String COMPLETED;
    private static final String ADDRESS;
    private static final int WAITING_TIME_MILLIS;
    private static final Draft draft;
    private static final int SUCCESS;
    private static final int FAILURE;

    static
    {
        SUCCESS = 0;
        FAILURE = 1;
        ADDRESS = "ws://localhost:8887";
        WAITING_TIME_MILLIS = 3000;
        draft = new Draft_6455();
        COMPLETED = "COMPLETED";
    }

    public static void main(String[] args) throws Exception
    {
        while (true)
        {
            WebSocketClient client = new Client(new URI(ADDRESS), draft);
            synchronized (client)
            {
                if (client.connectBlocking())
                {
                    break;
                }
                else
                {
                    System.out.println("Connection unavailable, waiting for  " + WAITING_TIME_MILLIS + " milliseconds");
                    client.wait(WAITING_TIME_MILLIS);
                }
            }
        }
    }

    public Client(URI serverUri, Draft draft)
    {
        super(serverUri, draft, null, 999);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata)
    {
        System.out.println("Connected...\n");
    }

    @Override
    public void onClose(int code, String reason, boolean remote)
    {
    }

    @Override
    public void onMessage(String message)
    {
        if (message.equals(COMPLETED))
        {
            System.out.println("Game over. Better luck next time.");
            cleanupAndShutdown(SUCCESS);
        }

        int num = 0;
        if (Objects.nonNull(message))
        {
            num = Integer.parseInt(message);
        }
        else
        {
            cleanupAndShutdown(FAILURE);
        }

        System.out.println("Number from other player: " + num);

        checkGameStatus(num);

        String next = Long.toString(Math.round(((double) num) / 3));

        System.out.println("Sending number: " + next);

        this.send(next);
    }

    private void checkGameStatus(int num)
    {
        if (num == 1)
        {
            System.out.println("Congratulations!! You have won!!!");
            this.send(COMPLETED);
            cleanupAndShutdown(SUCCESS);
        }
        else if (num <= 0)
        {
            System.out.println("Got a negative number.");
            cleanupAndShutdown(FAILURE);
        }
    }

    @Override
    public void onError(Exception ex)
    {
        if (!(ex instanceof java.net.ConnectException))
        {
            this.close();
            System.exit(FAILURE);
        }
    }

    private void cleanupAndShutdown(int exitStatus)
    {
        this.close();
        System.exit(exitStatus);
    }
}