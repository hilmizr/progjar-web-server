/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package webserverassignment;

import java.io.*;
import java.net.*;

/**
 *
 * @author Hp
 */
public class WebServerAssignment {

    public static void main(String[] args) {
        // Ask user for the config file path
        ConfigHandler configHandler = new ConfigHandler();
        String configFilePath = configHandler.getConfigFilePathFromUser();
        Config config = configHandler.readConfigFromFile(configFilePath);
        if (configFilePath == null) {
            System.out.println("Invalid configuration file path, exiting...");
            return;
        }

        boolean isRunning = true;
        while (isRunning) {
            try {
                System.out.println("---------------------------");
                ServerSocket server = new ServerSocket(config.getPort(), 0, InetAddress.getByName(config.getIpAddress()));
                System.out.println("Server started. Waiting for clients...");
                while (true) {
                    Socket client = server.accept();
                    System.out.println("New client connected: " + client.getInetAddress().getHostName());
                    Thread t = new ClientHandler(client, config.getRootDirectory());
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

}
