/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webserverassignment;

/**
 *
 * @author Hp
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigHandler {

    public Config readConfigFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            String ipAddress = null;
            int port = 0;
            Config config = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue; // Skip comments and empty lines
                }

                String[] parts = line.split("=");
                if (parts.length != 2) {
                    System.out.println("Invalid configuration line: " + line);
                    return null;
                }

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.equalsIgnoreCase("IP_ADDRESS")) {
                    ipAddress = value;
                } else if (key.equalsIgnoreCase("PORT")) {
                    try {
                        port = Integer.parseInt(value);
                        config = new Config(ipAddress, port);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number: " + value);
                        return null;
                    }
                } else if (key.toUpperCase().endsWith("_ROOT_DIRECTORY")) {
                    String domain = key.substring(0, key.indexOf("_ROOT_DIRECTORY")).toLowerCase();
                    config.addDomain(domain, value);
                } else {
                    System.out.println("Unknown configuration key: " + key);
                    return null;
                }
            }

            if (ipAddress == null || port == 0 || config == null) {
                System.out.println("Incomplete configuration");
                return null;
            }

            return config;
        } catch (IOException e) {
            System.out.println("Error reading configuration file: " + fileName);
            return null;
        }
    }

    public String getConfigFilePathFromUser() {
        System.out.println("---------------------------");
        System.out.println("Please enter the path to the configuration file:");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String configFilePath = in.readLine().trim();
            File configFile = new File(configFilePath);
            if (!configFile.exists() || !configFile.isFile()) {
                System.out.println("Invalid configuration file path");
                return null;
            }
            return configFilePath;
        } catch (IOException e) {
            System.out.println("Error reading input");
            return null;
        }
    }
}

