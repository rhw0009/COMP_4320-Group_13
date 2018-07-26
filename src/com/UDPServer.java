package com;

import com.sun.deploy.net.HttpResponse;
import com.sun.deploy.net.MessageHeader;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.*;
import java.util.*;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

class UDPServer {

    public static void main(String args[]) throws IOException
    {

        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        byte[] endData = new byte[1];


        String responseTest = "The string test did show up on the Servers run";

        // Change this when switching to the Tux computers.
        int serverPort = 8081;
        DatagramSocket serverSocket = new DatagramSocket(serverPort);

        //Should receive the first packet that the client sends
        System.out.println("Waiting on Client to connect... ");

        /* This block is needed to receive readable messages from the client. */
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Message from client: " + received);
        int clientPort = receivePacket.getPort();
        InetAddress IPAddress = receivePacket.getAddress();

        String header = "http/1.0 200 document follows\r\n" +
                "Sequence number of Packet: \r\n" +
                "content-tyoe: text/plain \r\n" +
                "Content Length: " +
                "Checksum: ";


        //Using the CRC32 we create a checksum and add to the header.
        Checksum checksum = new CRC32();
        byte bytes[] = header.getBytes();
        checksum.update(bytes, 0 ,bytes.length);

        //This string wil allow us to see if the checksum was changed by the gremlin or not.
        System.out.println("Checksum value is: " + checksum.getValue());
        String data = "Hello World!!!";



        for(int i = 0; i < 4; i++)
        {

            // This is the packet header. This should turn the String into bytes ready to send via datagram sendPacket
            String docHeader = ("http/1.0 200 document follows\r\n " +
                    "Sequence number of Packet: " + i + "\n\r" +
                    "content-type: text/plain \r\n " +
                    "Content Length: " + bytes.length + "\r\n\r\n" +
                    "Checksum: " + checksum.getValue() +
                    "\r\n" + data);
            try {
              System.out.println(postHttpRequest(args[0]));
            } catch (Exception e) {

            }
            sendData = docHeader.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
            serverSocket.send(sendPacket);

        }
        System.out.println("Responding to Client with a Header and Checksum");


       /* int i = 0;
        String packetHeader = " ";
        byte[] dataArray = {};
        int arrayLength;
        int dataIndex = 0;

        while(i < 4)
         {
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
             packetHeader = ("Checksum: " + getChecksum(sendPacket));
             arrayLength = packetHeader.length() + sendPacket.getLength() + 1;
             dataArray = new byte[arrayLength];
             for (int j = 0; i < packetHeader.length(); i++) {
                 dataArray[j] = packetHeader.getBytes()[j];
             }
             dataArray[packetHeader.length()] = ' ';
             dataIndex = 0;
             for (int j = packetHeader.length() - 1; i < dataArray.length; i++) {
                 dataArray[j] = sendPacket.getData()[dataIndex];
                 dataIndex++;
             }
             sendPacket.setData(dataArray);
             serverSocket.send(sendPacket);
             getChecksum(sendPacket);
             i++;
         }*/


         DatagramPacket sendEndPacket = new DatagramPacket(endData, endData.length, IPAddress, clientPort);
         serverSocket.send(sendEndPacket);
         serverSocket.close();

    }

    private static String postHttpRequest(String urlToPost) throws Exception {

        StringBuilder result = new StringBuilder();
        URL url = new URL (urlToPost);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        BufferedReader read = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String data = "Hello World!!!";

        while ((data = read.readLine()) != null)
        {
            result.append(data);
        }
        read.close();
        return result.toString();

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