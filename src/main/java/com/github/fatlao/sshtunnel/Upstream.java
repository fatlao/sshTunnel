package com.github.fatlao.sshtunnel;

import java.io.*;
import java.net.Socket;

/**
 * Created by fat-lao on 15/12/30.
 */
public class Upstream {

    public static final void main(String args[]) throws IOException {

        String ip = args[0];
        String port = args[1];

        Socket socket = new Socket(ip, Integer.parseInt(port));
        final OutputStream outputStream = socket.getOutputStream();
        final InputStream inputStream = socket.getInputStream();

//        File file = new File("stdin.log");
//        final FileOutputStream logger = new FileOutputStream(file);

        Thread toOnline = new Thread() {
            public void run() {


                while (true)

                {
                    try {
                        int bytedata = inputStream.read();
                        if (-1 == bytedata) {
                            return;
                        }

                        String data = Integer.toHexString(bytedata);
                        if (data.length() < 2) {
                            System.out.print("0");
                        }
                        System.out.println(data);


                    } catch (IOException e) {
                        return;
                    }
                }
            }
        };

        Thread toClient = new Thread() {
            public void run() {
                while (true) {
                    try {
                        int bytedata1 = (int) System.in.read();
                        if (-1 == bytedata1) {
                            return;
                        }
                        int bytedata2 = (int) System.in.read();
                        if (-1 == bytedata2) {
                            return;
                        }

                        int bytedata3 = (int) System.in.read();
                        if (-1 == bytedata3) {
                            return;
                        }


                        String byteData = new String(new char[]{(char) bytedata1, (char) bytedata2});

//                        logger.write(bytedata1);
//                        logger.write(bytedata2);
//                        logger.flush();

                        int bytenum = Short.parseShort(byteData, 16);

                        outputStream.write(bytenum);
                        outputStream.flush();

                    } catch (IOException e) {
                        return;
                    }
                }
            }
        };

        toOnline.start();
        toClient.start();


        Thread closeChecker = new Thread() {
            public void run() {
                File f = new File("/proc/self/status");
                while (true) {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                        String line;
                        while (null != (line = reader.readLine())) {

                            if (line.startsWith("PPid:")) {
                                line = line.substring("PPid:".length()).trim();
                                if (Integer.parseInt(line) == 1) {
                                    System.exit(1);
                                }
                            }
                        }
                        reader.close();

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        System.exit(1);
                    }
                }

            }
        };
        closeChecker.start();
    }

}
