
import java.io.*;
import java.util.Scanner;
/**
 *
 * @author rdunkley13
 */
public class CBLinkSender {

// LinkSender sends a message to LinkReceiver and receives a reply.
// LinkReceiver needs to be started before LinkSender.
    static int senderPort = 3200;   // port number used by sender
    static int receiverPort = 3300; // port number used by receiver


    
    public static void main(String args[]) throws Exception {
        
        Scanner console = new Scanner(System.in);

        CRC8 crc8 = new CRC8();
    
        int lengthMessageReceived = 0;
        String messageToSend;
        String messageReceived = null;
        byte[] sendingBuffer = new byte[512];
        byte[] receivingBuffer = new byte[512];
        byte[] messageToSendBytes = new byte[512];
        String clientSendingBuffer;
        int crc;
        boolean traceOn;
        

        // Set up a link with source and destination ports
        Link myLink = new SimpleLink(senderPort, receiverPort);

        // Enter name/client ID
        System.out.println("Enter your name/ID: ");
        String clientID = console.next();
       
        
        // Client Trace on/off?
        System.out.println("Error trace on/off?: ");
        String clientTrace = console.next();
        console.nextLine();
        if (clientTrace.equalsIgnoreCase("off")) {
            traceOn = false;
            System.out.println("Trace is off.");
        }else{
            traceOn = true;
            System.out.println("Trace is On.");
        }
    
        
        do {

            // Prepare the client id for the sending buffer
            System.arraycopy(clientID.getBytes(), 0, sendingBuffer, 0, clientID.length());

            // Prepare a message  to send
            System.out.println("Enter a message you would like to send: ");
            messageToSend = console.nextLine().toLowerCase();

            // Get length of message into 3rd byte of array
            sendingBuffer[3] = (byte) messageToSend.length();

            // Copy messageToSend into end of sendingBuffer
            System.arraycopy(messageToSend.getBytes(), 0, sendingBuffer, 4, messageToSend.length());

            // Calculate CRC from messageToSend and put into pos 44 of byte array - sendingBuffer
            byte checksum = crc8.checksum(messageToSend.getBytes());
            sendingBuffer[44] = checksum; //cast as byte into 44


            // Send the message
            myLink.sendFrame(sendingBuffer, sendingBuffer.length);
            // Print out the message sent
            System.out.println("Message sent is:   [" + messageToSend + "]");

            // Receive a message &OR NAK
            do {
                lengthMessageReceived = myLink.receiveFrame(receivingBuffer);
                messageReceived = new String(receivingBuffer, 0, lengthMessageReceived);

                if (receivingBuffer[0] == 1) {
                    //retransmit the buffer
                    if(traceOn == true){
                        System.out.println("Error. Need retransmission.");
                        System.out.println("Message re-sent is:   [" + messageToSend + "]");
                    }
                }
            } while (receivingBuffer[0] == 1);
            if(traceOn == true) {
                System.out.println("OK.");
            }
            
            // Display the message
            messageReceived = new String(receivingBuffer, 0, lengthMessageReceived);
            
            System.out.println("Message received is: [ Hi | " + clientID + 
                                                    " | ! | "+messageReceived + "]");
        
        
        
        } while (!messageReceived.contains("Bye"));

        // Close the connection	
        myLink.disconnect();
        System.exit(0);

    }
}
