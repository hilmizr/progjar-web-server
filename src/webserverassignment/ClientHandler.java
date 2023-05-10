/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webserverassignment;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Hp
 */
public class ClientHandler extends Thread {

    private final Socket client;
    private final String rootDirectory;
    boolean keepAlive = false;

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
            String request;

            // Continue reading requests until the client closes the connection
            while ((request = in.readLine()) != null) {
                System.out.println("Received request: " + request);

                // Check for the Connection header
                if (request.startsWith("Connection:")) {
                    String connectionHeader = request.substring(12);
                    keepAlive = connectionHeader.equalsIgnoreCase("Keep-Alive");
                }

                // Handle the request
                if (request.startsWith("GET")) {
                    handleRequest(request, out);
                }

                // If the connection is not keep-alive or the client closed the connection, break
                if (!keepAlive || in.ready()) {
                    break;
                }
            }

            // Close the client socket
            client.close();
            System.out.println("Connection closed: " + client.getInetAddress().getHostName());

        } catch (IOException e) {
            System.err.println("Exception caught: " + e);
        }
    }

    private void handleRequest(String request, OutputStream out) throws IOException {
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
        } else if (file.exists()) {
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
        String responseHeaders = statusLine + contentTypeHeader;
        if (keepAlive) {
            responseHeaders += "Connection: keep-alive\r\n";
        } else {
            responseHeaders += "Connection: close\r\n";
        }
        responseHeaders += "\r\n";

        byte[] response = new byte[responseHeaders.getBytes().length + messageBody.length];
        System.arraycopy(responseHeaders.getBytes(), 0, response, 0, responseHeaders.getBytes().length);
        System.arraycopy(messageBody, 0, response, responseHeaders.getBytes().length, messageBody.length);
        out.write(response);
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
