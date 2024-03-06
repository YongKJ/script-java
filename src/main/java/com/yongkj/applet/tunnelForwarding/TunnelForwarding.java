package com.yongkj.applet.tunnelForwarding;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.yongkj.util.GenUtil;

import java.util.*;

public class TunnelForwarding {

    private final Map<String, Object> server;
    private final List<Map<String, Object>> tunnels;

    private TunnelForwarding() {
        server = GenUtil.getMap("server");
        tunnels = GenUtil.getListMap("tunnels");
    }

    private void apply() {
        Session session = getTunnelSession();
        if (session == null) return;

        for (Map<String, Object> tunnel : tunnels) {
            String targetHost = GenUtil.objToStr(tunnel.get("target-host"));
            int targetPort = GenUtil.objToInteger(tunnel.get("target-port"));
            int forwardPort = GenUtil.objToInteger(tunnel.get("forward-port"));

            try {
                session.setPortForwardingL(forwardPort, targetHost, targetPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Scanner scanner = new Scanner(System.in);
        do {
            if (Objects.equals(scanner.nextLine(), "exit")) break;
        } while (true);
    }

    private Session getTunnelSession() {
        String host = GenUtil.objToStr(server.get("host"));
        int port = GenUtil.objToInteger(server.get("port"));
        String userName = GenUtil.objToStr(server.get("username"));
        String password = GenUtil.objToStr(server.get("password"));

        JSch jsch = new JSch();
        Session session = null;
        try {
//            jsch.setKnownHosts("localhost");
            session = jsch.getSession(userName, host, port);
            session.setPassword(password);

            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            session.setConfig(sshConfig);
            session.connect(60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    public static void run(String[] args) {
        new TunnelForwarding().apply();
    }

}
