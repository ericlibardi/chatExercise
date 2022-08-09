package org.academiadecodigo.unbitables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private Socket clientSocket;
    private String hostName;
    private int portNum;
    private BufferedReader cInput;
    private PrintWriter output;

    public void init() {

        this.hostName = "localhost";
        this.portNum = 6060;

        try {

            clientSocket = new Socket(hostName, portNum);
            cInput = new BufferedReader(new InputStreamReader(System.in));
            output = new PrintWriter(clientSocket.getOutputStream(), true);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void clientHandler() {

        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.submit(new Chat());

        while (!clientSocket.isClosed()) {
            String clientMsg = clientInput();

            if (clientMsg.equals("exit")) {
                try {
                    clientOutput(clientMsg);
                    clientSocket.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                clientOutput(clientMsg);
            }

        }

        try {
            cInput.close();
            singleExecutor.shutdown();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private String clientInput() {

        String clientMsg;

        try {
            clientMsg = cInput.readLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return clientMsg;
    }

    private void clientOutput(String msg) {

            output.write(msg + "\n");
            output.flush();

    }

    private void setUsername() {
        String username;

        System.out.println("Set your username: ");
        //cInput = new BufferedReader(new InputStreamReader(System.in));

        try {

            username = cInput.readLine();

            //output = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        output.write(username + "\n");
        output.flush();

    }


    private class Chat implements Runnable {


        @Override
        public void run() {

            BufferedReader cInput = null;

            try {

                while (!clientSocket.isClosed()) {

                    cInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String line;

                    while ((line = cInput.readLine()) != null) {
                        System.out.println(line);
                    }

                }

                cInput.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
