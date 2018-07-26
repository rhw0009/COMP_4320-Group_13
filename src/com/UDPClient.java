package com;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;
import java.lang.*;

public class UDPClient {

    private static final int CLIENT_PORT = 81;
    private static final int SERVER_PORT = 80;
    private static final String SERVER_ADDRESS = "localhost";
    private static final String FILENAME = "ExampleWebPage.html";
    private static final int PACKET_SIZE = 256;

    public static void main(String[] args) {
        //initialize
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(CLIENT_PORT);
        } catch(SocketException socketE) {
            System.out.println("Could not establish socket on port " + CLIENT_PORT + ". Exiting.");
            System.exit(1);
        }

        String requestBuffer = "GET " + FILENAME + " HTTP/1.0";
        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName(SERVER_ADDRESS);
        } catch (UnknownHostException uhE) {
            System.out.println("Could not find host. Exiting.");
            System.exit(2);
        }

        DatagramPacket request = new DatagramPacket(requestBuffer.getBytes(), requestBuffer.length(), serverAddress, SERVER_PORT);
        Scanner sc = new Scanner(System.in);
        double chanceToCorrupt = 0;
        double chanceToDrop = 0;
        System.out.println("Please enter the chance for a packet to be corrupted as a value between 0.0 and 1.0:");
        boolean validInput = false;
        while (!validInput) {
            chanceToCorrupt = sc.nextDouble();
            if (chanceToCorrupt >= 0 && chanceToCorrupt <= 1) {
                validInput = true;
            }
            else {
                System.out.println("Invalid input; please enter a value between 0.0 and 1.0:");
            }
        }
        System.out.println("Please enter the chance for a packet to be dropped as a value between 0.0 and 1.0:");
        validInput = false;
        while (!validInput) {
            chanceToDrop = sc.nextDouble();
            if (chanceToDrop >= 0 && chanceToDrop <= 1) {
                validInput = true;
            }
            else {
                System.out.println("Invalid input; please enter a value between 0.0 and 1.0:");
            }
        }

        //send request
        try {
            System.out.println("Sending request for file.");
            socket.send(request);
        } catch(IOException ioE) {
            System.out.println("Packet was not initialized. Exiting.");
            System.exit(3);
        }

        //receive document header
        byte[] headerBytes = new byte[PACKET_SIZE];
        DatagramPacket docHeader = new DatagramPacket(headerBytes, headerBytes.length);
        String finalText = "";
        try {
            socket.receive(docHeader);
        } catch (IOException ioE){
            System.out.println("Failed to receive request acknowledgment. Exiting.");
            System.exit(4);
        }
        String docHeaderString = new String(docHeader.getData());
        finalText = finalText.concat(docHeaderString);
        System.out.println("Received request acknowledgment. Document header: \n\n" + docHeaderString);

        //receive packets
        byte[] bytesIn = new byte[PACKET_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(bytesIn, bytesIn.length);
        String packetContent;
        boolean nullPacketReceived = false;
        ListedPacket newPacket;
        Vector<ListedPacket> packetsReceived = new Vector<>(0);
        int sequenceNum = 0;
        Random rng = new Random();
        int rand;
        while (!nullPacketReceived) {
            //receive packet
            try {
                socket.receive(receivePacket);
            } catch(IOException ioE) {
                System.out.println("Failed to receive packet. Exiting.");
                System.exit(5);
            }
            //check for null packet (eof)
            packetContent = new String(receivePacket.getData());
            if (packetContent.equals("\0")) {
                System.out.println("End of file reached.");
                nullPacketReceived = true;
            }
            else {
                //extract from header
                newPacket = new ListedPacket(receivePacket, sequenceNum);

                //gremlin drop
                rand = rng.nextInt(100);
                if (rand < chanceToCorrupt * 100) {
                    //dropped
                }
                else {

                }

                //gremlin corrupt
                rand = rng.nextInt(100);
                if (rand < chanceToDrop) {
                    //dropped
                }
                else {

                }

                sequenceNum++;
            }
        }

        //error detection
        //send ACK/NAKs
        //receive missing packets
        //assemble packets
        //print output
    }
}