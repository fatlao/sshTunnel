package com.github.fatlao.sshtunnel;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Created by fat-lao on 15/12/30.
 */
public class Binder {

    public static final void main(String args[]) throws IOException, JSchException {

        final JSch jsch = new JSch();

        char hextbl[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        File configFile = new File("tunnels.properties");
        FileInputStream fileInputStream = new FileInputStream(configFile);
        Properties config = new Properties();
        config.load(fileInputStream);
        fileInputStream.close();

        final String host = config.getProperty("host");
        final String port = config.getProperty("port");
        final String username = config.getProperty("username");
        final String password = config.getProperty("password");
        final String commandLine = config.getProperty("commandline");


        for (Object keyobj : config.keySet()) {
            final String localport = keyobj.toString();
            final String remote = config.getProperty(localport);

            if (localport.matches("^\\d+$")) {

                System.out.println("loading config:local:" + localport + ",remote:" + remote);

                final int localp = Integer.parseInt(localport);

                final ServerSocket serverSocket = new ServerSocket(localp);

                Thread acceptThread = new Thread() {

                    public void run() {

                        try {
                            while (true) {
                                Socket accept = serverSocket.accept();
                                System.out.println("ssh connecting:" + remote);

                                final Session session = jsch.getSession(username, host, Integer.parseInt(port));
                                session.setPassword(password);
                                session.setConfig("StrictHostKeyChecking", "no");
                                session.connect(30000);


                                final ChannelExec channel = (ChannelExec) session.openChannel("exec");

                                String command = (commandLine + " " + remote);

                                channel.setCommand(command);
                                channel.connect();

                                System.out.println("ssh connected:" + remote);

                                final InputStream inputStream = channel.getInputStream();
                                final OutputStream outputStream = channel.getOutputStream();

                                final InputStream socketInputStream = accept.getInputStream();
                                final OutputStream socketOutputStream = accept.getOutputStream();

                                Thread toOnline = new Thread() {
                                    public void run() {
                                        try {

                                            while (true) {
                                                try {

                                                    int bytedata1 = (int) inputStream.read();
                                                    if (-1 == bytedata1) {
                                                        return;
                                                    }
                                                    int bytedata2 = (int) inputStream.read();
                                                    if (-1 == bytedata2) {
                                                        return;
                                                    }
                                                    int bytedata3 = (int) inputStream.read();
                                                    if (-1 == bytedata3) {
                                                        return;
                                                    }

                                                    char high = Character.toLowerCase((char) bytedata1), low = Character.toLowerCase((char) bytedata2);

                                                    int high_i = high - '0' > 9 ? high - 'a' + 10 : high - '0';
                                                    int low_i = low - '0' > 9 ? low - 'a' + 10 : low - '0';


                                                    int bytenum = (high_i << 4) | low_i;


                                                    socketOutputStream.write(bytenum);
                                                    socketOutputStream.flush();
                                                } catch (IOException e) {
                                                    try {
                                                        inputStream.close();
                                                        socketOutputStream.close();
                                                        outputStream.close();
                                                        socketInputStream.close();
                                                        channel.sendSignal("9");
                                                        channel.disconnect();
                                                        session.disconnect();
                                                    } catch (Exception e1) {
                                                        e1.printStackTrace();
                                                    }

                                                    return;
                                                }
                                            }

                                        } finally {

                                            System.out.print("disconnecting:" + remote);
                                            try {
                                                inputStream.close();
                                                socketOutputStream.close();
                                                outputStream.close();
                                                socketInputStream.close();
                                                channel.sendSignal("9");
                                                channel.disconnect();
                                                session.disconnect();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                };

                                Thread toClient = new Thread() {
                                    public void run() {
                                        try {
                                            while (true) {
                                                try {
                                                    int bytedata = socketInputStream.read();
                                                    if (-1 == bytedata) {
                                                        return;
                                                    }
//                                                    String hex = Integer.toHexString(bytedata);
//
//                                                    if (hex.length() < 2) {
//                                                        hex = "0" + hex;
//                                                    }

                                                    int high = bytedata >> 4;
                                                    int low = bytedata & 0x0F;

                                                    outputStream.write(hextbl[high]);
                                                    outputStream.write(hextbl[low]);
                                                    outputStream.write('\n');
                                                    outputStream.flush();

                                                } catch (IOException e) {
                                                    try {
                                                        inputStream.close();
                                                        socketOutputStream.close();
                                                        outputStream.close();
                                                        socketInputStream.close();
                                                        channel.sendSignal("9");
                                                        channel.disconnect();
                                                        session.disconnect();
                                                    } catch (Exception e1) {
                                                        e1.printStackTrace();
                                                    }
                                                    return;
                                                }
                                            }
                                        } finally {
                                            System.out.print("disconnecting:" + remote);
                                            try {
                                                inputStream.close();
                                                socketOutputStream.close();
                                                outputStream.close();
                                                socketInputStream.close();
                                                channel.sendSignal("9");
                                                channel.disconnect();
                                                session.disconnect();
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                };

                                toOnline.start();
                                toClient.start();
                            }
                        } catch (Exception e) {
                            try {
                                serverSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                return;
                            }

                        }

                    }
                };

                acceptThread.start();
            }
        }

    }

}
