package com;

import java.io.*;
import java.net.*;
import java.util.Vector;

class UDPServer {
    public static void main(String args[]) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(10048);
        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        byte[] endData = new byte[1];
        int checksum = 1024;
        endData = null;

        //Should receive the first packet that the client sends
        System.out.println("Receiving Data from client... ");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        String sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();

        // Should print out whatever data the client first sends.
        System.out.println(receiveData.toString());

        System.out.println("getting the clients port number in order to return the message.");
        int port = receivePacket.getPort();


        // This is the packet header. This should turn the String into bytes ready to send via datagram sendPacket
        String packetHeader = ("http/1.0 200 document follows\r\n " +
                    "content-type: text/plain \r\n " +
                    "Content Length: 1024 bytes \r\n\r\n" +
                    "Checksum: " + checksum);
        sendData = packetHeader.getBytes();

        System.out.println("responding to the clients request...");


        // Loop that is supposed to split the response packet into 4 and sends back to the client
        int i = 0;
         while(i < 4)
         {
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length / 4, IPAddress, port);
             serverSocket.send(sendPacket);
             getChecksum(sendPacket);
             i++;
         }


            // This is suppose to send At the end of the file,
            // it transmits 1 byte(NULL character) that indicates the end of the file.
            // It will then close the file.
            System.out.println("Sending the 1 byte packet to identify the end of the message...");
            DatagramPacket sendEndPacket = new DatagramPacket(endData, endData.length, IPAddress, port);
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
            try {
                file = new FileInputStream(filename);
            } catch (FileNotFoundException fileNotFound) {
                System.out.println("Error: file not found.");
                return null;
            }
            boolean eof = false;
            int numRead = 0;
            byte[] currentPacket = {};
            while (!eof) {
                numRead = file.read(currentPacket, 0, 256);
                if (numRead < 256) eof = true;
                output.add(currentPacket.toString());
            }
            output.add("\0"); //add eof null packet
        } catch(IOException ioExcept) {
            return null;
        }
        return output;
    }
}