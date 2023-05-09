/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webserverassignment;

/**
 *
 * @author Hp
 */
import java.util.HashMap;
import java.util.Map;

public class Config {
    private final String ipAddress;
    private final int port;
    private final Map<String, String> domainRootDirectoryMap;

    public Config(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.domainRootDirectoryMap = new HashMap<>();
    }

    public void addDomain(String domain, String rootDirectory) {
        domainRootDirectoryMap.put(domain, rootDirectory);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getRootDirectory(String domain) {
        return domainRootDirectoryMap.get(domain);
    }
}

