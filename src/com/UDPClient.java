package com;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.IntStream;
import java.lang.*;

public class UDPClient {

    private static final int CLIENT_PORT = 81;

    public static void main() {
        //intitialize
        try {
            DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
        } catch(SocketException socketE) {
            System.out.println("Could not establish socket on port " + CLIENT_PORT + ".");
        }

        //send request
        //receive packets
        //gremlin
        //error detection
        //send ACK/NAKs
        //receive missing packets
        //assemble packets
        //print output
    }
}