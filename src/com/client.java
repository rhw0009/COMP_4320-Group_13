package com;

import java.io.*;
import java.net.*;
import java.util.*;

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
            if (receivePacket.length == 1 && receivePacket.buf == NULL) eof = true;
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

    public static DatagramPacket gremlin(DatagramPacket packet, int distribution) {
        numToCorrupt;
        if (distribution > 50) numToCorrupt = 1;
        else if (distribution > 20) numToCorrupt = 2;
        else numToCorrupt = 3;
        Random rng = new Rand();
        noDuplicates = true;
        while (noDuplicates) {
            randStream = rng.ints(numToCorrupt, 0, packet.length);
            randArray = randStream.toArray;
            for (int i = 0; i < numToCorrupt; i++) {
                for (int j = i + 1; j < numToCorrupt; j++) {
                    if (randArray[i] == randArray[j]) {
                        noDuplicates == false;
                    }
                }
            }
        }
    }

    public static void detectError() {

    }
}