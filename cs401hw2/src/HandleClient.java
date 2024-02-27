import javax.xml.crypto.Data;
import java.io.File;
import java.net.*;
import java.util.Scanner;

public class HandleClient implements Runnable{
    // shift amount for encrypting and decrypting
    static int shift =2;
    // put the alphabet into an array
    static char[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    DatagramSocket s;
    int clientPort, serverPort = 3000;
    byte[] buf= new byte[65535]; //it's better to keep the buffer size with in the MTU limit i.e. 1472 bytes
    String rcvMessage, sndMessage = "";
    DatagramPacket sndPacket, rcvPacket;
    InetAddress clientAddress;

    // takes the DatagramSocket port from the new thread
    public HandleClient(DatagramSocket s) {
        this.s = s;
    }

    public void run() {

        try {
            // read in sample file
            File file = new File("src/Sample.txt");
            Scanner scanner = new Scanner(file);

            //create a server socket to send and receive messages
            while(true) {
                DatagramSocket serverSocket = new DatagramSocket(serverPort);
                System.out.println("Waiting for incoming connections");
                rcvPacket = new DatagramPacket(buf, buf.length); //initialize the receive packet such that incoming data is stored in buf
                serverSocket.receive(rcvPacket); //wait for incoming data from client
                clientPort = rcvPacket.getPort(); //retrieve client's port number
                clientAddress = rcvPacket.getAddress(); //retrieve client's ip address

                // receive the message from the client, split it at the "_"
                rcvMessage = new String(rcvPacket.getData(), 0, rcvPacket.getLength());/*  rcvPacket.getData() returns the entire buffer buf (which we initially created of size 1000)includes both the server information and leftover bytes rcvPacket.getLength() returns the actual size of data sent by the server rcvMessage = new String(rcvPacket.getData(), 0, rcvPacket.getLength()); will return only the server response and removes the extra bytes *///alternative to the above://rcvMessage = new String(rcvPacket.getData()).trim();
                String[] split = rcvMessage.split("_");

                // overwrite the encrypted names with the decrypted names
                split[0] = decryptData(split[0]);
                split[1] = decryptData(split[1]);
                String line[];
                System.out.println(split[0] + " " + split[1]);
                // loop that reads each line of the file
                // separates it into first, last and social
                // and then checks if it matches the name that the client sent
                // if it does, return the  encrypted social,
                // if it doesn't return -1
                while(scanner.hasNextLine()){
                    line = scanner.nextLine().split(" ");
                    if(line[0].equals(split[0]) && line[1].equals(split[1])){

                        sndMessage = encryptData(line[2]);
                        break;
                    }
                }
                // if the username isnt found, set the message = -1
                if(sndMessage.equals(""))
                    sndMessage = "-1";
                // send the message back to the client
                sndPacket = new DatagramPacket(sndMessage.getBytes(), sndMessage.getBytes().length, clientAddress, clientPort ); //create a packet with the computed area and address it with the client's port number and IP address
                serverSocket.send(sndPacket);//send the packet to the client (client details are in the sndPacket) through the serverSocket
                buf = new byte[1000];//reset the buf and continue waiting for new incoming requests

            }
        }
        catch(Exception E) {

        }
    }



    // method to encrypt the numbers to send back to the client
    // (1)if the shift amount makes the number go over 9, subtract 10 to make it loop back around
    public String encryptData(String cipherText){
        char[] numbers = cipherText.toCharArray();
        StringBuilder encrypted = new StringBuilder();
        for(int i=0; i< numbers.length;i++){

            if(String.valueOf(numbers[i]).equals("-")) {
                encrypted.append("-");
            }
            else if(Integer.parseInt(String.valueOf(numbers[i]))+shift>9){ // (1)
                encrypted.append(Integer.parseInt(String.valueOf(numbers[i])) + shift - 10);
            }
            else{// append the shifted number to the String builder
                encrypted.append(Integer.parseInt(String.valueOf(numbers[i])) + shift);
            }

        }

        return encrypted.toString();
    }

    // method to decrypt the message from the client
    // it splits the message into individual letters
    // (1) find the index of the letter, and shift it to the left by the shift amount
    // (2) if it shifts past 0, wrap around to the other end of the alphabet
    // (3) add the encrpyted letter to the string builder
    public String decryptData(String cipherText){
        char[] letters = cipherText.toCharArray();
        StringBuilder encrypted = new StringBuilder();
        for(int i=0; i<letters.length; i++){
            int index = findIndex(letters[i]) - shift; //(1)
            if(index < 0){ //(2)
                index = Math.abs(index+26);
            }

            encrypted.append(alphabet[index]);// (3)
        }
        return encrypted.toString();
    }


    // helper method when decrypting, it finds the index of the current letter i.e B is 1
    public  int findIndex(char letter){
        int index=0;
        while(true){
            if(String.valueOf(letter).equals(String.valueOf(alphabet[index]))) {
                return index;
            }
            index++;
        }
    }

}