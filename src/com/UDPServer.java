package com;

import com.sun.deploy.net.HttpResponse;
import com.sun.deploy.net.MessageHeader;

import java.io.*;
import java.net.*;
import java.util.*;

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

        /*This is the HttpResponse it should post the webpage to the clients request,
        * along with a header.*/
        HttpResponse response = new HttpResponse() {
            @Override
            public URL getRequest() {
                return null;
            }

            @Override
            public int getStatusCode() {
                return 0;
            }

            @Override
            public int getContentLength() {
                return 0;
            }

            @Override
            public long getExpiration() {
                return 0;
            }

            @Override
            public long getLastModified() {
                return 0;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public String getResponseHeader(String s) {
                return null;
            }

            @Override
            public BufferedInputStream getInputStream() {
                return null;
            }

            @Override
            public void disconnect() {

            }

            @Override
            public String getContentEncoding() {
                return null;
            }

            @Override
            public MessageHeader getHeaders() {
                return null;
            }
        };


        /* This block is needed to receive readable messages from the client. */
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
        System.out.println("Message from client: " + received);
        int clientPort = receivePacket.getPort();
        InetAddress IPAddress = receivePacket.getAddress();


        // This is the packet header. This should turn the String into bytes ready to send via datagram sendPacket
        String docHeader = ("http/1.0 200 document follows\r\n " +
                    "content-type: text/plain \r\n " +
                    "Content Length: " + sendData.length + "\r\n\r\n" +
                    "Checksum: " + responseTest);
       sendData = docHeader.getBytes();
       DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, clientPort);
       serverSocket.send(sendPacket);
       System.out.println("Responding to Client with a Header");




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