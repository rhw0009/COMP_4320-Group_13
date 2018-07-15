
import java.io.*;
import java.net.*;

class UDPServer {
    public static void main(String args[]) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(10048);
        byte[] receiveData = new byte[1024];
        byte[] sendData  = new byte[1024];
        byte[] endData = new byte[1];
        endData = null;


        System.out.println("Receiving Data from client... ");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        String sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();

        System.out.println("getting the clients port number..");
        int port = receivePacket.getPort();
        String capitalizedSentence = ("http/1.0 200 document follows\r\n " +
                    "content-type: text/plain \r\n " +
                    "Content Length: 256 bytes \r\n\r\n" +
                    "Data");
        sendData = capitalizedSentence.getBytes();

        System.out.println("responding to the clients request...");

        int i = 0;
         while(i < 4)
         {
             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length / 4, IPAddress, port);
             serverSocket.send(sendPacket);
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
}