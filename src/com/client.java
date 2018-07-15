package com;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;

public class UDPClient {
    public static void main(String args[]) throws Exception
    {
        //Get corruption chance chanceToCorrupt
        double chanceToCorrupt = 0; //temp
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket(10051);
        InetAddress IPAddress = InetAddress.getByName("hostname");

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        byte[] receiveEndData = new byte[1];

        //boolean receiveComplete = false;
        //Sends HTTP connection request to the server.
        URL url = new URL("http://server.com");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET TestFile.html HTTP/1.0");
        // Allows user to send custom message to the server.

        String sentence = inFromUser.readLine();
        sendData = sentence.getBytes();

        System.out.println("Sending request method to server...");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 10048);
        clientSocket.send(sendPacket);

        Scanner sc = new Scanner(System.in);
        double corruptionValue;
        do {
            System.out.println("Please enter the chance for a packet to be corrupted(Between 0.0 - 1.0: )");
            while (!sc.hasNextDouble()) {
                System.out.println("Please Enter An Accepted Value...");
                sc.next();
            }
            corruptionValue = sc.nextInt();
        } while (corruptionValue < 0.0 || corruptionValue > 1.0);
        System.out.println("Applying corruption to packets...");

        System.out.println(sc.nextLine());


        //Print send
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        boolean eof = false;
        Random rng = new Random();
        IntStream randStream;
        int[] randArray;
        //Receive header
        Vector packetList = new Vector(0);
        while (!eof) {  //Receive packets
            clientSocket.receive(receivePacket);
            if (receivePacket.getLength() == 1 && receivePacket.getData() == null) eof = true;
            else{
                randStream = rng.ints(2, 1, 101); //Generates 2 random numbers [1,100]; first is chance to corrupt, second is used to determine number of corrupt bits
                randArray = randStream.toArray();
                if (randArray[0] <= chanceToCorrupt * 100) {
                    receivePacket = gremlin(receivePacket, randArray[1]);
                }
            }
            packetList.add(receivePacket);
        }
        //Error detection goes here
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
        int[] randArray;
        IntStream randStream;
        while (noDuplicates) {
            randStream = rng.ints(numToCorrupt, 0, packet.getLength());
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
            dataByte = packetData[i];
            dataInt = (int)dataByte;
            dataInt = dataInt ^ ones;
            dataInt = dataInt ^ zeroes;
            packetData[i] = (byte)dataInt;
        }
        packet.setData(packetData);
        return packet;
    }

    public static void detectError() {

    }
}