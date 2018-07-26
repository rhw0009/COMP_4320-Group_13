package com;

import java.net.DatagramPacket;
import java.util.Random;

public class ListedPacket {
    int headerChecksum;
    int calculatedChecksum;
    int sequenceNum;
    String bufferString;
    byte[] bufferBytes;
    DatagramPacket original;
    boolean headerRemoved;

    ListedPacket(DatagramPacket packetIn, int sequence) {
        original = packetIn;
        sequenceNum = sequence;
        bufferBytes = original.getData();
        bufferString = new String(bufferBytes);
        headerRemoved = false;
        if (!bufferString.startsWith("\0")) {
            headerChecksum = extractChecksum();
            bufferString = removeHeader(bufferString);
        }
        else {
            headerChecksum = 0;
        }
        headerRemoved = true;
        bufferBytes = bufferString.getBytes();
        setChecksum();
    }

    public void corruptPacket() {
        int rand;
        Random rng = new Random();
        rand = rng.nextInt(100);
        int numToCorrupt;
        int[] bytesToCorrupt;
        boolean noDuplicates = false;
        byte corruptedByte;
        if (rand < 20) {
            numToCorrupt = 3;
        }
        else if (rand < 50) {
            numToCorrupt = 2;
        }
        else {
            numToCorrupt = 1;
        }
        bytesToCorrupt = new int[numToCorrupt];
        while (!noDuplicates) {
            noDuplicates = true;
            for (int j = 0; j < bytesToCorrupt.length; j++) {
                rand = rng.nextInt(bufferBytes.length);
                bytesToCorrupt[j] = rand;
            }
            for (int j = 0; j < bytesToCorrupt.length - 1; j++) {
                for (int k = j + 1; k < bytesToCorrupt.length; k++) {
                    if (bytesToCorrupt[j] == bytesToCorrupt[k]) {
                        noDuplicates = false;
                    }
                }
            }
        }
        for (int j = 0; j < bytesToCorrupt.length; j++) {
            corruptedByte = (byte) (bufferBytes[j] ^ 0b11111111);
            bufferBytes[j] = corruptedByte;
        }
        this.setChecksum();
    }

    private int extractChecksum() {
        if (headerRemoved == false) {
            int start = bufferString.indexOf(':') + 2;
            int end = bufferString.indexOf('\r', start);
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

    public void setChecksum() {
        calculatedChecksum = calculateChecksum(bufferBytes);
    }

    private String removeHeader (String input) {
        int index = input.indexOf('\n');
        String output = input.substring(index);
        return output;
    }
}