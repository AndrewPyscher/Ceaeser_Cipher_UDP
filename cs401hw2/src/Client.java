import java.io.FileNotFoundException;
import java.net.*;
import java.util.Scanner;

public class Client {
    // put the alphabet into an array
    static char[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    // the shift amount (for encrypting and decrypting)
    static int shift = 2;

    public static void main(String[] args) throws FileNotFoundException {
        Scanner input;
        byte[] buf = new byte[65535];
        DatagramPacket sndPacket, rcvPacket;
        String rcvMessage, sndMessage;
        int serverPort = 3000;
        String first, last;


        // create scanner, then prompt user to enter the name to search for
        // loop that repeats if the user enters an invalid username
        // user must enter name in all caps
        input = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a first name (all caps)"); //prompt the user to enter a first name
            first = input.next();
            System.out.println("Enter a last name (all caps)"); //prompt the user to enter a last name
            last = input.next();
            // checks if its in all caps by making the input all caps, then checking it against itself
            if(first.toUpperCase().equals(first) && last.toUpperCase().equals(last)) {
                break;
            }
            System.out.println("Enter a valid username");
        }
        // encrypt the first and last name
        first = encryptData(first);
        last = encryptData(last);

        try {
            DatagramSocket clientSocket = new DatagramSocket();//create a socket to send to and receive messages from the server
            InetAddress serverAddress = InetAddress.getByName("192.168.1.226"); //Determines the IP address of a host, given the host's name.

            // add "-" between first and last and send it to the server
            sndMessage = first + "_" + last;
            sndPacket = new DatagramPacket(sndMessage.getBytes(), sndMessage.getBytes().length, serverAddress, serverPort);//Constructs a datagram packet for sending packets of length length to the specified port number on the specified host.
            clientSocket.send(sndPacket);//Sends a datagram packet from this socket.
            rcvPacket = new DatagramPacket(buf, buf.length);//Constructs a DatagramPacket for receiving packets of length length.
            clientSocket.receive(rcvPacket);//Receives a datagram packet from this socket.
            rcvMessage = new String(rcvPacket.getData(), 0, rcvPacket.getLength()); /* 	rcvPacket.getData() returns the entire buffer buf (which we initially created of size 1000) includes both the server information and leftover bytes rcvPacket.getLength() returns the actual size of data sent by the server rcvMessage = new String(rcvPacket.getData(), 0, rcvPacket.getLength()); will return only the server response and removes the extra bytes */

            // if the server sends back "-1" it means the username wasnt found in the servers file
            if(rcvMessage.equals("-1")){
                System.out.println("invalid username");
            }
            // if it was found, it prints out the encrypted name and the corresponding SSN
            else{
                System.out.println("Encrypted Username: " + first + " " + last);
                System.out.println("SSN:" + decryptData(rcvMessage));
            }

        }
        catch(Exception E) {
            E.printStackTrace();
            System.out.println("why error?");
        }
    }

    // this is when I realised I should have used a linkedlist, but it was already too late
    // method that splits the text into single characters and loops through and encrypts each letter
    // (1) then finds the corresponding letter after shifting
    // (2) if the letters index is over 24 it means it will have to wrap around the alphabet i.e Z->A
    // (3) if (2) occurs, subtracting 26 and taking the absolute value of that will return the correct letter
    public static String encryptData(String plainText){
        char[] letters = plainText.toCharArray();
        StringBuilder encrypted = new StringBuilder();
        for(int i=0; i<letters.length; i++){
            int index = findIndex(letters[i]) + shift; // (1)
            if(index+1 > 24){ //(2)
                index = Math.abs(index-26); //(3)
            }
            // add the encrpyted letter to the string builder
            encrypted.append(alphabet[index]);
        }
        return encrypted.toString();
    }


    // method that shifts the returning numbers to the left 2
    // its still stored as a string because theres "-" in it
    // (1)if the number ends up being negative, then add 10 to wrap back around i.e 0 shift 2 -> 8
    public static String decryptData(String cipherText){
        char[] numbers = cipherText.toCharArray();
        StringBuilder encrypted = new StringBuilder();

        for(int i=0; i< numbers.length;i++){
            if(String.valueOf(numbers[i]).equals("-"))
                encrypted.append("-");
            else if(Integer.parseInt(String.valueOf(numbers[i]))-shift<0){ //(1)
                encrypted.append(Integer.parseInt(String.valueOf(numbers[i]))-shift +10);
            }
            else{
                encrypted.append(Integer.parseInt(String.valueOf(numbers[i]))-shift);
            }

        }

        return encrypted.toString();
    }

    // helper method when encrypting, it finds the index of the current letter i.e B is 1
    public static int findIndex(char letter){
        int index=0;
        while(true){
            if(String.valueOf(letter).equals(String.valueOf(alphabet[index]))) {
                return index;
            }
            index++;
        }
    }
}