/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webserverassignment;

/**
 *
 * @author Hp
 */
public class Config {

    private final String ipAddress;
    private final int port;
    private final String rootDirectory;

    public Config(String ipAddress, int port, String rootDirectory) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.rootDirectory = rootDirectory;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }
}
