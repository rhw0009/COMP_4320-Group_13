package com;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;
import java.lang.*;


public class UDPClient {
    public static void main(String args[]) throws Exception {

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket(10051);
        InetAddress IPAddress = InetAddress.getByName("131.204.14.55");

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        byte[] receiveEndData = new byte[1];

        //boolean receiveComplete = false;
        //Sends HTTP connection request to the server.
        System.out.println("Sending HTTP request to Server port...");
        URL url = new URL("HTTP", "localhost",10050,"ExampleWebPage.html");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");// ExampleWebPage.html HTTP/1.0");
        con.setReadTimeout(15*1000);
       // con.connect();

        // Allows user to send custom message to the server.
        String sentence = inFromUser.readLine();
        sendData = sentence.getBytes();

        System.out.println("Sending request method to server...");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10050);
        clientSocket.send(sendPacket);

        double chanceToCorrupt = 0;
        Scanner sc = new Scanner(System.in);
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
        System.out.println("Applying a " + chanceToCorrupt + " chance of corruption to packets...");
        //System.out.println(sc.nextLine());
        System.out.println("Verifying checksum of corrupted packets...");

        //Print send
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        boolean eof = false;
        Random rng = new Random();
        IntStream randStream;
        int[] randArray = {0};
        int sequenceNum = 0;
        //Receive header
        Vector<DatagramPacket> packetList = new Vector(0);
        System.out.println("Response received!");
        System.out.println("Closing the Server Client Connection Thank you!");
        while (!eof) {  //Receive packets
            clientSocket.receive(receivePacket);
            if (receivePacket.getLength() == 1 && receivePacket.getData() == null) eof = true;
            else {
                randStream = rng.ints(2, 1, 101); //Generates 2 random numbers [1,100]; first is chance to corrupt, second is used to determine number of corrupt bits
                randArray = randStream.toArray();
                if (randArray[0] <= chanceToCorrupt * 100) {
                    receivePacket = gremlin(receivePacket, randArray[1]);
                }
                errorDetected(receivePacket, sequenceNum);
            }
            packetList.add(receivePacket);
            sequenceNum++;
        }

        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence); //Print receive
        clientSocket.close();
    }

    public static DatagramPacket gremlin(DatagramPacket packetIn, int distribution) {
        DatagramPacket packet = packetIn;
        int numToCorrupt;
        if (distribution > 50) numToCorrupt = 1;
        else if (distribution > 20) numToCorrupt = 2;
        else numToCorrupt = 3;
        Random rng = new Random();
        boolean noDuplicates = true;
        int[] randArray = {0};
        IntStream randStream;
        while (noDuplicates) {
            randStream = rng.ints(numToCorrupt, 9, packet.getLength());
            randArray = randStream.toArray();
            for (int i = 0; i < numToCorrupt; i++) {
                for (int j = i + 1; j < numToCorrupt; j++) {
                    if (randArray[i] == randArray[j]) {
                        noDuplicates = false;
                    }
                }
            }
        }
        byte[] packetData = packet.getData();
        byte dataByte;
        int dataInt;
        byte ones = 0b1111111;
        byte zeroes = 0b00000000;
        for (int i = 0; i < randArray.length; i++) {
            int byteIndex = randArray[i];
            dataByte = packetData[byteIndex];
            dataInt = (int) dataByte;
            dataInt = dataInt ^ ones;
            dataInt = dataInt ^ zeroes;
            packetData[i] = (byte) dataInt;
        }
        packet.setData(packetData);
        return packet;
    }

    public static boolean errorDetected(DatagramPacket packet, int sequenceNum) {
        int checksum = 0;
        int cSumStartIndex = (packet.getData()).toString().indexOf("Checksum: ");
        cSumStartIndex += 10;
        int cSumEndIndex = (packet.getData()).toString().indexOf(' ', cSumStartIndex);
        String cSumString = (packet.getData()).toString().substring(cSumStartIndex, cSumEndIndex);
        int correctSum = Integer.parseInt(cSumString);
        for (int i = 0; i < packet.getLength(); i++) {
            checksum += packet.getData()[i];
        }
        if (checksum == correctSum) {
            return false;
        } else {
            System.out.println("Error detected in packet " + sequenceNum);
            return true;
        }
    }

    public static int getChecksum(DatagramPacket packet) {
        int checksum = 0;
        for (int i = 0; i < packet.getLength(); i++) {
            checksum += packet.getData()[i];
        }
        return checksum;
    }
}