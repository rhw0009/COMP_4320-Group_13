//Group 13: Montgomery, Wakeford, Williams
//COMP 4320
package com;

import com.sun.deploy.net.HttpRequest;
import com.sun.deploy.net.HttpResponse;
import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.util.*;

import java.lang.*;



public class UDPClient {

    /*May need to change which chrome or mozilla is being used.*/
    private static final String USER_AGENT = "Chrome/67.0.3396.99";
    private static final String GET_URL =
            "http://localhost:63342/COMP_4320-Group_13/Project/com/ExampleWebPage.html?_ijt=1jgbbu4v2fnob572c42brojk7g";


    public static void main(String args[]) throws IOException {

        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        String filename;

        //Change these variables when wanting to switch to the Tux computers.
        int clientPort = 8080;
        int serverPort = 8081;
        InetAddress IPAddress = InetAddress.getByName("localhost");
        String sendTest = "This String should show up in the Servers Run!";

        filename = GET_URL;




        DatagramSocket clientSocket = new DatagramSocket(clientPort);

        /*This is the HTTP request to the web server.
         *Sending request packet*/



            String request = ("GET " + filename + "HTTP/1.0\r\n");
            byte[] requestPage = new byte[request.length()];
            requestPage = request.getBytes();
            DatagramPacket requestPacket = new DatagramPacket(requestPage, requestPage.length, IPAddress, serverPort);
            clientSocket.send(requestPacket);



        /* This block is needed if you want to send something to
         * the server!*/
        sendData = sendTest.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
        clientSocket.send(sendPacket);
        System.out.println("Sending request method to server...\n\n");

        /*This block is needed if you want to receive packets from the server.*/
        System.out.println("Recieving packets from Server...\n\n ");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Message from Server: " + received);



       /* double chanceToCorrupt = 0;
        Scanner sc = new Scanner(System.in);
        //System.out.println("Please enter the chance for a packet to be corrupted as a value between 0.0 and 1.0:");
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
                //Generates 2 random numbers [1,100]; first is chance to corrupt, second is used to determine number of corrupt bits
                randStream = rng.ints(2, 1, 101);
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
        return checksum;*/
    }

    private static String getHTTPRequest(String urlToRead) throws Exception {

        StringBuilder result = new StringBuilder();
        URL url = new URL (urlToRead);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader read = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;

        while ((line = read.readLine()) != null)
        {
            result.append(line);
        }
        read.close();
        return result.toString();

        }

    }


