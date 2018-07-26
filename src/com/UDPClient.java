package com;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPClient {

    private static final int CLIENT_PORT = 81;
    private static final int SERVER_PORT = 80;
    private static final String SERVER_ADDRESS = "localhost";
    private static final String FILENAME = "ExampleWebPage.html";
    private static final int PACKET_SIZE = 512;

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
        DatagramPacket receivePacket = null;
        String packetContent;
        boolean nullPacketReceived = false;
        ListedPacket newPacket;
        Vector<ListedPacket> packetList = new Vector<>(0);
        int sequenceNum = 0;
        while (!nullPacketReceived) {
            //receive packet
            try {
                receivePacket = new DatagramPacket(bytesIn, bytesIn.length);
                socket.receive(receivePacket);
            } catch(IOException ioE) {
                System.out.println("Failed to receive packet. Exiting.");
                System.exit(5);
            }
            //check for null packet (eof)
            packetContent = new String(receivePacket.getData());
            if (packetContent.equals("\0") | (!packetContent.startsWith("C"))) {
                System.out.println("End of file reached.");
                nullPacketReceived = true;
            }
            //extract from header
            newPacket = new ListedPacket(receivePacket, sequenceNum);
            packetList.add(newPacket);
            sequenceNum++;
            }

        //gremlin
        packetList = (gremlin(packetList, chanceToDrop, chanceToCorrupt));

        //error detection
        String[] responseList = new String[packetList.size()];
        ListedPacket currentPacket;
        for (int i = 0; i < packetList.size(); i++) {
            try {
                currentPacket = packetList.get(i);
                if (currentPacket.calculatedChecksum == currentPacket.headerChecksum) {
                    responseList[i] = "ACK" + i;
                }
                else responseList[i] = "NAK" + i;
            } catch (NullPointerException nullE) {
                responseList[i] = "";
            }
        }

        //send ACK/NAKs
        DatagramPacket responsePacket;
        byte[] responseBytes;
        for (int i = 0; i < responseList.length; i++) {
            responseBytes = responseList[i].getBytes();
            responsePacket = new DatagramPacket(responseBytes, responseBytes.length, serverAddress, SERVER_PORT);
            try {
                socket.send(responsePacket);
            } catch (IOException ioE) {
                System.out.println("Failed to send ACK/NAK. Exiting.");
                System.exit(6);
            }
        }

        //receive missing packets
        ListedPacket fixedPacket;
        DatagramPacket receiveFixed = new DatagramPacket(bytesIn, bytesIn.length);
        for (int i = 0; i < responseList.length; i++) {
            if (!responseList[i].startsWith("A")) {
                try {
                    socket.receive(receiveFixed);
                } catch (IOException ioE) {
                    System.out.println("Failed to receive resent packet. Exiting.");
                    System.exit(7);
                }
                fixedPacket = new ListedPacket(receiveFixed, i);
                packetList.set(i, fixedPacket);
            }
        }

        //assemble packets
        System.out.println("Assembling document.");
        for (int i = 0; i < packetList.size(); i++) {
            finalText.concat(packetList.get(i).bufferString);
        }

        //print output
        System.out.println("Printing document.\r\n\n");
    }



    public static Vector<ListedPacket> gremlin(Vector<ListedPacket> input, double chanceToDrop, double chanceToCorrupt) {
        Random rng = new Random();
        int rand;
        ListedPacket corruptedPacket;
        Vector<ListedPacket> output = input;
        for (int i = 0; i < output.size(); i++) {
            rand = rng.nextInt(100);
            if (rand < chanceToDrop * 100) {
                //drop packet
                output.set(i, null);
            }
            else {
                rand = rng.nextInt(100);
                if(rand < chanceToCorrupt) {
                    //corrupt packet
                    corruptedPacket = output.get(i);
                    corruptedPacket.corruptPacket();
                    output.set(i, corruptedPacket);
                }
            }
        }
        return output;
    }
}