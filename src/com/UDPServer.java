//Group 13: Montgomery, Wakeford, Williams
//COMP 4320
package com;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

class UDPServer {


    static InetAddress IPAddress;       //This will be pulled from the received datagram.
    static int clientPort;              // This will be pulled from the received datagram.
    final static int serverPort = 8082; // change this when switching to the tux computers. Our ports are 10048 - 10051.
    final static int TIMEOUT = 40;      //Timeout is 40 milliseconds, 4 times the assumed RTT.
    final static int ACK = 1;
    final static int NAK = 0;
    final static int WINDOW_SIZE = 8;   // 0 - 7 packets.

    public static void main(String args[]) throws IOException {

        byte[] sendData = new byte[512];
        byte[] headerData = new byte[128];
        byte[] endData = new byte[1];

        String responseTest = "The string test did show up on the Servers run";

        DatagramSocket serverSocket = new DatagramSocket(serverPort);

        //Should receive the first packet that the client sends.
        System.out.println("Waiting on Client to send a request... \n");
        while (true) {
            /* This block is needed to receive readable messages from the client. */
            byte[] receivedData = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
            serverSocket.receive(receivePacket);
            System.out.println("Message from client: " + receivePacket.toString() + "\n");
            clientPort = receivePacket.getPort();
            IPAddress = receivePacket.getAddress();

            String postRequest = new String(receivePacket.getData());
            if (!postRequest.startsWith("GET")) {
                continue;
            }
            System.out.println("Message from client: " + postRequest);
            String fileName = postRequest.split(" ")[1];


        }

    }


    public static String setHeader(String header) {
        header = "http/1.0 200 document follows\r\n" +
                "Sequence number of Packet: \r\n" +
                "content-tyoe: text/plain \r\n" +
                "Content Length: " +
                "Checksum: ";

        //Using the CRC32 we create a checksum and add to the header.
        Checksum checksum = new CRC32();
        byte bytes[] = header.getBytes();
        checksum.update(bytes, 0, bytes.length);

        return header;

    }

    private static String postHttpRequest(String urlToPost) throws Exception {

        StringBuilder result = new StringBuilder();
        URL url = new URL (urlToPost);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        BufferedReader read = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String data;

        while ((data = read.readLine()) != null)
        {
            result.append(data);
        }
        read.close();
        return result.toString();

    }



    public static Vector<String> generatePackets(String filename) {
        Vector<String> output = new Vector<String>(0);
        try {
            FileInputStream file;
            file = new FileInputStream(filename);
            try {

                boolean eof = false;
                int numRead = 0;
                byte[] currentPacket = {};
                while (!eof) {
                    numRead = file.read(currentPacket, 0, 256);
                    if (numRead < 256) eof = true;
                    output.add(currentPacket.toString());
                }
            }  catch (FileNotFoundException fileNotFound) {
                System.out.println("Error: file not found.");
                return null;
            }
            output.add("\0"); //add eof null packet
        } catch(IOException ioExcept) {
            return null;
        }
        return output;
    }
}