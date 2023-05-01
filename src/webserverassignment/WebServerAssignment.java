/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package webserverassignment;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

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

            // Construct the HTTP response
            String statusLine;
            byte[] messageBody;
            File file = new File(rootDirectory + fileName);
            if (file.isDirectory()) {
                File indexFile = new File(file, "index.html");
                if (indexFile.exists()) {
                    file = indexFile;
                    statusLine = "HTTP/1.1 200 OK\r\n";
                    messageBody = getBinaryFileContent(file);
                    contentTypeHeader = "Content-Type: text/html\r\n";
                } else {
                    statusLine = "HTTP/1.1 200 OK\r\n";
                    messageBody = generateDirectoryListing(file, fileName).getBytes();
                    contentTypeHeader = "Content-Type: text/html\r\n";
                }
            } else if (file.exists())
                {
                    statusLine = "HTTP/1.1 200 OK\r\n";
                    messageBody = getBinaryFileContent(file);
                    if (fileName.endsWith(".html") || fileName.endsWith(".txt")) {
                        contentTypeHeader = "Content-Type: text/html\r\n";
                    } else {
                        contentTypeHeader = "Content-Type: application/octet-stream\r\n";
                    }
                } else {
                    statusLine = "HTTP/1.1 404 Not Found\r\n";
                    messageBody = ("<html><body><h1>404 Not Found</h1></body></html>").getBytes();
                    contentTypeHeader = "Content-Type: text/html\r\n";
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

        private String generateDirectoryListing(File directory, String fileName) {
            StringBuilder html = new StringBuilder("<html><body><h1>Directory Listing</h1><table><tr><th>Name</th><th>Last Modified</th><th>Size</th></tr>");
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    String lastModified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
                    String size = file.isDirectory() ? "-" : Long.toString(file.length());
                    String href = fileName + (fileName.endsWith("/") ? "" : "/") + name;
                    html.append("<tr><td><a href=\"").append(href).append("\">").append(name).append("</a></td><td>").append(lastModified).append("</td><td>").append(size).append("</td></tr>");
                }
            }
            html.append("</table></body></html>");
            return html.toString();
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


