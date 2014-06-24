package main.java.control;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

//clasa de BE
public class PortForward extends AbstractTestElement {
    public static String LOCAL_PORT = "TcpProxyControlGui.localPort";
    public static String REMOTE_PORT = "TcpProxyControlGui.remotePort";
    public static String REMOTE_SERVER_NAME = "TcpProxyControlGui.remoteServerName";
    public static int DEFAULT_PORT = 8087;
    public static String DEFAULT_PORT_S =
            Integer.toString(DEFAULT_PORT);// Used by GUI
    public static String DEFAULT_REMOTE_PORT_S =
            Integer.toString(DEFAULT_PORT); // Used by GUI

    private boolean isRunning = true;
    static SimpleDateFormat fm;
    private EventSource eventSource;
    private ServerSocket serverSocket;
    private Thread t;


    static {
        PortForward.fm = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    }

    public PortForward(EventSource eventSource) {
        this.eventSource = eventSource;
    }


    public void startServer() {
        t = new Thread() {
            @Override
            public void run() {
                try {
                    startTcpProxy();
                } catch (IOException e) {
                    System.out.println("IOException catched!");
                }
            }
        };
        t.start();
    }

    public void stopServer() {
        isRunning = false;
        try {
            info(null, "stopping server...");
            serverSocket.close();
        } catch (SocketException e) {
            System.out.println(" ok Exception ");
            e.printStackTrace();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void startTcpProxy() throws IOException {
        isRunning = true;
        if (getLocalPort() != 0 && getRemotePort() != 0 && getRemoteServerName() != "") {
            int localPort = getLocalPort();
            String remoteServerName = getRemoteServerName();
            int remotePort = getRemotePort();
            forward(localPort, remoteServerName, remotePort);
        } else {
            System.out.println("return.");
        }
    }


    private void forward(final int localPort, final String remoteServerName, final int remotePort) throws IOException {
        Socket toClient, toServer = null;
        serverSocket = new ServerSocket(localPort);
//        System.out.println("local port " + localPort + " -> " + remoteServerName + ":" + remotePort+"bind local port ok");
        info(null, "local port " + localPort + " -> " + remoteServerName + ":" + remotePort);
        info(null, "bind local port ok");
        while (isRunning) {
            toClient = serverSocket.accept();
            toServer = new Socket(remoteServerName, remotePort);
            info(toClient.getRemoteSocketAddress(), "accepted, begin to forward  ");
            forward(toServer, toClient);
        }
    }

    private void forward(final Socket toServer, final Socket toClient) {
        final SocketAddress clientIp = toClient.getRemoteSocketAddress();
        try {
            forwardStream(clientIp, toClient.getInputStream(), toServer.getOutputStream(), "send   ----->");
            forwardStream(clientIp, toServer.getInputStream(), toClient.getOutputStream(), "receive----->");
        } catch (IOException e) {
            logIOException(clientIp, "get(Input/Out)Stream", e);
        }
    }

    private void forwardStream(final SocketAddress clientIp, final InputStream inputStream, final OutputStream outputStream, final String action) {
        new Thread() {
            public void run() {
                System.out.println("a intrat in forward stream");
                final byte[] read = new byte[4096];
                int cnt = 0;
                final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                while (true) {
                    int readed;
                    try {
                        readed = inputStream.read(read);
                    } catch (IOException e) {
                        logIOException(clientIp, action, e);
                        break;
                    }
                    if (readed != -1) {
                        try {
                            outputStream.write(read, 0, readed);
                            outputStream.flush();
                            bytes.write(read, 0, readed);
                            cnt += readed;
                            continue;
                        } catch (IOException e) {
                            logIOException(clientIp, String.valueOf(action) + " forward   ", e);
                        }
                        break;
                    }
                    break;
                }
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    logIOException(clientIp, action, e);
                }
                //info(clientIp, String.valueOf(action) + " forward bytes count " + cnt);
                //info(clientIp, String.valueOf(action) + "byte[]:" + PortForward.toHexStr(bytes.toByteArray()));
                try {
                    info(clientIp, String.valueOf(action) + "string:" + new String(bytes.toByteArray(), "GBK"));
                } catch (UnsupportedEncodingException ex) {
                }
//                info(clientIp, String.valueOf(action) + " forward end.");
            }
        }.start();
    }

    protected static String toHexStr(final byte[] byteArray) {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; ++i) {
            if (i > 0) {
                buffer.append(' ');
            }
            buffer.append(String.format("%2x", byteArray[i]));
        }
        return buffer.toString();
    }

    private void logIOException(final SocketAddress ip, final String action, final IOException e) {
        System.out.println(String.valueOf(PortForward.fm.format(new Date())) + " " + ip + "\t " + action + " : " + e.getMessage());
    }

    private void info(final SocketAddress ip, final String action) {
        if (ip == null) {
            eventSource.publishEvent(new LogEvent(String.valueOf(PortForward.fm.format(new Date())) + "\t " + action));
        } else {
            eventSource.publishEvent(new LogEvent(String.valueOf(PortForward.fm.format(new Date())) + "\n " + ip + "\t " + action));
        }
    }

    public int getLocalPort() {
        return getPropertyAsInt(LOCAL_PORT);
    }

    public void setLocalPort(int port) {
        setProperty(new IntegerProperty(LOCAL_PORT, port));
    }

    public String getRemoteServerName() {
        return getPropertyAsString(REMOTE_SERVER_NAME);
    }

    public void setRemoteServerName(String name) {
        setProperty(new StringProperty(REMOTE_SERVER_NAME, name));
    }

    public int getRemotePort() {
        return getPropertyAsInt(REMOTE_PORT);
    }

    public void setRemotePort(int port) {
        setProperty(new IntegerProperty(REMOTE_PORT, port));
    }
}
