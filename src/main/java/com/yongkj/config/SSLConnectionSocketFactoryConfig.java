package com.yongkj.config;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class SSLConnectionSocketFactoryConfig extends SSLConnectionSocketFactory {

    public SSLConnectionSocketFactoryConfig() {
        super(SSLContexts.createSystemDefault());
    }

    @Override
    public Socket createSocket(HttpContext context) throws IOException {
        InetSocketAddress socksAddr = new InetSocketAddress(Global.getProxyHost(), Global.getProxyPort());
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksAddr);
        return new Socket(proxy);
    }

    @Override
    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
        InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
    }
}
