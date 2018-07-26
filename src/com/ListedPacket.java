package com;

import java.net.DatagramPacket;

public class ListedPacket {
    int headerChecksum;
    int calculatedChecksum;
    int sequenceNum;
    String bufferString;
    byte[] bufferBytes;
    DatagramPacket original;
    boolean headerRemoved;

    ListedPacket(DatagramPacket packetIn, int sequence) {
        sequenceNum = sequence;
        bufferBytes = original.getData();
        bufferString = new String(bufferBytes);
        original = packetIn;
        headerRemoved = false;
        headerChecksum = extractChecksum();
        bufferString = removeHeader(bufferString);
        headerRemoved = true;
        bufferBytes = bufferString.getBytes();
        calculatedChecksum = calculateChecksum(bufferBytes);
    }

    private int extractChecksum() {
        if (headerRemoved == false) {
            int start = bufferString.indexOf(':') + 2;
            int end = bufferString.indexOf(' ', start);
            String checkSumString = bufferString.substring(start, end);
            return Integer.parseInt(checkSumString);
        }
        else return -1;
    }

    private int calculateChecksum(byte[] bytesIn) {
        int checksum = 0;
        for (int i = 0; i < bytesIn.length; i++) {
            checksum += bytesIn[i];
        }
        return checksum;
    }

    private String removeHeader (String input) {
        int index = input.indexOf('\n');
        String output = input.substring(index);
        return output;
    }
}