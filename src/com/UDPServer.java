package com;

import java.io.*;
import java.net.*;
import java.util.Vector;

public class UDPServer {

    private static final int CLIENT_PORT = 81;
    private static final int SERVER_PORT = 80;
    private static final String CLIENT_ADDRESS = "localhost";
    private static final int PACKET_SIZE = 512;
    private static final String DOC_HEADER = "HTTP/1.0 200 Document Follows\r\nContent-Type: text/plain\r\nContent-Length: ";

    public static void main(String[] args) {
        //initialize
        String packetContents;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(SERVER_PORT);
        } catch(SocketException socketE) {
            System.out.println("Could not establish socket on port " + SERVER_PORT + ". Exiting.");
            System.exit(1);
        }
        byte[] requestBytes = new byte[PACKET_SIZE];
        DatagramPacket request = new DatagramPacket(requestBytes, requestBytes.length);

        //receive request
        String filename = "";
        try {
            System.out.println("Awaiting request.");
            socket.receive(request);
            packetContents = new String(request.getData());
            filename = packetContents;
            System.out.println("File request received: \"" + packetContents + "\"");
        } catch(IOException ioE) {
            System.out.println("Socket was not initialized. Exiting.");
            System.exit(2);
        }

        //find file
        filename = filename.split(" ")[1];

        //generate packets
        Vector<String> packetList = generatePackets(filename);
        DatagramPacket packet;
        byte[] packetBytes = null;
        InetAddress clientAddress = null;
        try {
            clientAddress = InetAddress.getByName(CLIENT_ADDRESS);
        } catch (UnknownHostException uhE) {
            System.out.println("Could not find host. Exiting.");
            System.exit(5);
        }

        //send document header
        String docHeader = DOC_HEADER;
        int docSize = 0;
        for (int i = 0; i < packetList.size(); i++) {
            docSize += packetList.get(i).length();
        }
        docHeader = docHeader.concat(Integer.toString(docSize));
        docHeader = docHeader.concat("\r\n");
        byte[] headerBytes = docHeader.getBytes();
        DatagramPacket headerPacket = new DatagramPacket(headerBytes, headerBytes.length, clientAddress, CLIENT_PORT);
        System.out.println("Acknowledging request.");
        try{
            socket.send(headerPacket);
        } catch (IOException ioE) {
            System.out.println("Failed to send acknowledgment. Exiting.");
            System.exit(6);
        }

        //send packets
        for (int i = 0; i < packetList.size(); i++) {
            packetBytes = packetList.get(i).getBytes();
            packet = new DatagramPacket(packetBytes, packetBytes.length, clientAddress, CLIENT_PORT);
            packet = generateChecksum(packet);
            try{
                socket.send(packet);
            } catch(IOException ioE) {
                System.out.println("Could not send packet to target. Exiting.");
                System.exit(7);
            }
        }

        //receive ACK/NAKs
        String[] responseList = new String[packetList.size()];
        String currentResponse;
        byte[] responseBytes = new byte[PACKET_SIZE];
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length);
        DatagramPacket resendPacket;
        for (int i = 0; i < responseList.length; i++) {
            try {
                socket.receive(responsePacket);
            } catch (IOException ioE) {
                System.out.println("Failed to receive response. Exiting.");
                System.exit(8);
            }
            currentResponse = new String(responsePacket.getData());
            responseList[i] = currentResponse;
            if (!currentResponse.isEmpty()) {
                System.out.println("Received " + currentResponse);
            }
            else {
                System.out.println("Packet " + i + " timed out.");
            }
            //resend missing packets
            if (!currentResponse.startsWith("A")) {
                packetBytes = packetList.get(i).getBytes();
                resendPacket = new DatagramPacket(packetBytes, packetBytes.length, clientAddress, CLIENT_PORT);
                System.out.println("Resending packet " + i + ".");
                try {
                    socket.send(resendPacket);
                } catch (IOException ioE) {
                    System.out.println("Failed to resend packet. Exiting.");
                    System.exit(9);
                }
            }
        }



    }




    public static DatagramPacket generateChecksum(DatagramPacket input) {
        byte[] bytes = input.getData();
        String bufferString = new String(input.getData());
        DatagramPacket output = input;
        if (!bufferString.equals("\0")) {
            String newString = "";
            int checksum = 0;
            for (int i = 0; i < bytes.length; i++) {
                checksum += bytes[i];
            }
            newString = "Checksum: " + checksum + "\r\n" + bufferString;
            output.setData(newString.getBytes());
        }
        else {
            output.setData("\0".getBytes());
            output.setLength(1);
        }
        return output;
    }

    public static Vector<String> generatePackets(String filename) {
        Vector<String> output = new Vector<String>(0);
        FileInputStream file = null;
        try {
            file = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("File not found. Exiting.");
            System.exit(3);
        }
        boolean eof = false;
        byte[] currentPacket = new byte[PACKET_SIZE];
        String currentPacketString;
        try {
            while (!eof) {
                if (file.read(currentPacket) == -1) {
                    eof = true;
                    output.add("\0");
                    file.close();
                } else {
                    currentPacketString = new String(currentPacket);
                    output.add(currentPacketString);
                }
            }
        } catch (IOException ioE) {
            System.out.println("Could not read from file. Exiting.");
            System.exit(4);
        }
        return output;
    }
}