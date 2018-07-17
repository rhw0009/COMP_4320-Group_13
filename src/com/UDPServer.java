package com;

import java.io.*;
import java.net.*;
import java.util.Vector;

class UDPServer {
    public static void main(String args[]) throws IOException
    {
        DatagramSocket serverSocket = new DatagramSocket(10050);
        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        byte[] endData = null;


        //Should receive the first packet that the client sends
        System.out.println("Waiting on Client to connect... ");

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        System.out.println("Receiving Data from client... ");
        System.out.println(receivePacket.toString());

        //String requestMethod = con.getRequestMethod();

        String sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();

        // Should print out whatever data the client first sends.
        System.out.println(receiveData.toString());

        System.out.println("Data received!");
        int port = receivePacket.getPort();

        URL url = new URL("HTTP","localhost",port,"ExampleWebPage.html");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");


        // This is the packet header. This should turn the String into bytes ready to send via datagram sendPacket
        String docHeader = ("http/1.0 200 document follows\r\n " +
                    "content-type: text/plain \r\n " +
                    "Content Length: 1024 bytes \r\n\r\n" +
                    "Checksum: ");
        sendData = docHeader.getBytes();

        System.out.println("responding to the clients request...");

        // Loop that is supposed to split the response packet into 4 and sends back to the client
        int i = 0;
        String packetHeader = " ";
        byte[] dataArray = {};
        int arrayLength;
        int dataIndex = 0;
        System.out.println("Setting the checksum...");
         while(i < 4)
         {
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length / 4, IPAddress, port);
             packetHeader = ("Checksum: " + getChecksum(sendPacket));
             arrayLength = packetHeader.length() + sendPacket.getLength() + 1;
             dataArray = new byte[arrayLength];
             for (int j = 0; i < packetHeader.length(); i++) {
                 dataArray[j] = packetHeader.getBytes()[j];
             }
             dataArray[packetHeader.length()] = ' ';
             dataIndex = 0;
             for (int j = packetHeader.length() + 1; i < dataArray.length; i++) {
                 dataArray[j] = sendPacket.getData()[dataIndex];
                 dataIndex++;
             }
             sendPacket.setData(dataArray);
             serverSocket.send(sendPacket);
             getChecksum(sendPacket);
             i++;
         }


            // This is suppose to send At the end of the file,
            // it transmits 1 byte(NULL character) that indicates the end of the file.
            // It will then close the file.
            System.out.println("Sending the Null packet to identify the end of the message...");


         DatagramPacket sendEndPacket = new DatagramPacket(endData, endData.length, IPAddress, 10051);
         serverSocket.send(sendEndPacket);
         serverSocket.close();

    }

    public static int getChecksum(DatagramPacket packet) {
        int checksum = 0;
        for (int i = 0; i < packet.getLength(); i++) {
            checksum += packet.getData()[i];
        }
        return checksum;
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