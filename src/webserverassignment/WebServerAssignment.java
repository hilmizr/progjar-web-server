/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package webserverassignment;

import java.io.*;
import java.net.*;
import java.util.logging.*;

public class WebServerAssignment {

    public static void main(String[] args) {
        String rootDirectory = getRootDirectoryFromUser();
        if (rootDirectory == null) {
            System.out.println("Invalid root directory, exiting...");
            return;
        }

        boolean isRunning = true;
        while (isRunning) {
            try {
                ServerSocket server = new ServerSocket(8080);
                System.out.println("Server started. Waiting for clients...");
                while (true) {
                    Socket client = server.accept();
                    System.out.println("New client connected: " + client.getInetAddress().getHostName());
                    Thread t = new ClientHandler(client, rootDirectory);
                    t.start();
                }
            } catch (IOException e) {
                System.err.println("Exception caught: " + e);
            }

            // Prompt the user to input a command to stop or restart the server
            System.out.println("Type 'stop' to stop the server, or 'restart' to restart the server:");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                String command = in.readLine().trim();
                if (command.equals("stop")) {
                    isRunning = false;
                } else if (!command.equals("restart")) {
                    System.out.println("Invalid command, continuing...");
                }
            } catch (IOException e) {
                System.out.println("Error reading input");
            }
        }
    }

    private static String getRootDirectoryFromUser() {
        System.out.println("---------------------------");
        System.out.println("Please enter the path to the root directory:");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String rootDirectory = in.readLine().trim();
            File rootDirectoryFile = new File(rootDirectory);
            if (!rootDirectoryFile.exists() || !rootDirectoryFile.isDirectory()) {
                System.out.println("Invalid root directory");
                return null;
            }
            return rootDirectory;
        } catch (IOException e) {
            System.out.println("Error reading input");
            return null;
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket client;
        private final String rootDirectory;

        public ClientHandler(Socket client, String rootDirectory) {
            this.client = client;
            this.rootDirectory = rootDirectory;
        }

        @Override
        public void run() {
            System.out.println("---------------------------");
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                OutputStream out = client.getOutputStream();

                // Read the HTTP request from the client
                String request = in.readLine();
                System.out.println("Received request: " + request);

                // Extract the requested file name from the request
                String[] requestParts = request.split(" ");
                String fileName = requestParts[1];

                // Check if the requested file is a text/HTML file or a binary file
                String contentTypeHeader;
                File file = new File(rootDirectory + fileName);
                if (fileName.endsWith(".html") || fileName.endsWith(".txt")) {
                    contentTypeHeader = "Content-Type: text/html\r\n";
                } else {
                    contentTypeHeader = "Content-Type: application/octet-stream\r\n";
                }

                // Construct the HTTP response
                String statusLine;
                byte[] messageBody;
                            if (file.exists()) {
                statusLine = "HTTP/1.1 200 OK\r\n";
                messageBody = getBinaryFileContent(file);
            } else {
                statusLine = "HTTP/1.1 404 Not Found\r\n";
                messageBody = ("<html><body><h1>404 Not Found</h1></body></html>").getBytes();
            }

            // Send the HTTP response to the client
            String responseHeaders = statusLine + contentTypeHeader + "\r\n";
            byte[] response = new byte[responseHeaders.getBytes().length + messageBody.length];
            System.arraycopy(responseHeaders.getBytes(), 0, response, 0, responseHeaders.getBytes().length);
            System.arraycopy(messageBody, 0, response, responseHeaders.getBytes().length, messageBody.length);
            out.write(response);

            // Close the client socket
            client.close();
            System.out.println("Connection closed: " + client.getInetAddress().getHostName());
        } catch (IOException e) {
            System.err.println("Exception caught: " + e);
        }
    }

    private byte[] getBinaryFileContent(File file) throws IOException {
        byte[] content = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(content);
        fis.close();
        return content;
    }
    }
}
