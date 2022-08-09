package org.academiadecodigo.unbitables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int portNum;
    private ServerSocket serverSocket;
    private LinkedList<User> clientList;

    public void init() {

        portNum = 6060;
        clientList = new LinkedList<>();

        try {
            serverSocket = new ServerSocket(portNum);

            ExecutorService fixedPool = Executors.newFixedThreadPool(10);

            while (!serverSocket.isClosed()) {

                Socket clientSocket = serverSocket.accept();

                fixedPool.submit(new ServerRunnable(clientSocket));

            }

            fixedPool.shutdown();
            serverSocket.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private class ServerRunnable implements Runnable {

        private Socket clientSocket;
        private User currentUser;
        private BufferedReader input;

        public ServerRunnable(Socket clientSocket) {
            this.clientSocket = clientSocket;

        }

        @Override
        public void run() {

            try {
                sendGenericMsg(HTMLMessage.WELCOME);
                sendGenericMsg(HTMLMessage.SETUSERNAME);

                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                currentUser = new User(clientSocket, input.readLine());

                clientList.add(currentUser);


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            while (!clientSocket.isClosed()) {
                String message = listenMsg();
                sendMsg(message);
            }

        }

        private String listenMsg() {

            String msg;

            try {

                msg = input.readLine();

                if (msg.equals("exit")) {
                    clientSocket.close();

                    for (User client : clientList) {

                        if (client.getClientSocket().equals(clientSocket)) {
                            clientList.remove(client);
                            break;
                        }

                    }


                    msg = "";
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return msg;
        }

        private void sendMsg(String msg) {

            if (msg.equals("")) {
                return;
            }

            try {

                for (User client : clientList) {

                    if (client.getClientSocket().equals(this.clientSocket)) {
                        continue;
                    }

                    PrintWriter output = new PrintWriter(client.getClientSocket().getOutputStream(), true);

                    output.write(currentUser.getName() + ": " + msg + "\n");
                    output.flush();

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        private void sendGenericMsg(String msg) {
            if (msg.equals("")) {
                return;
            }

            String[] lines = msg.split("\n");

            try {

                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                for (String line : lines) {
                    output.println(line);
                }
                output.flush();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        }

        private String setUsername() {

            String username;
            try {
                username = input.readLine();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return username;

        }
    }

    private class User {
        private Socket clientSocket;
        private String name;

        public User(Socket clientSocket, String name) {
            this.clientSocket = clientSocket;
            this.name = name;
        }

        public Socket getClientSocket() {
            return clientSocket;
        }

        public String getName() {
            return name;
        }
    }

}
