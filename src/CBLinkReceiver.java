
import static java.lang.System.console;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rdunkley13
 */
public class CBLinkReceiver {

// LinkReceiver receives a message from LinkSender and replies.
// LinkReceiver needs to be started before LinkSender.
    static int senderPort = 3200;   // port number used by sender
    static int receiverPort = 3300; // port number used by receiver
   

    public static void main(String args[]) throws Exception {
        
        Scanner console = new Scanner(System.in);
        RulesEngine rules = new RulesEngine();
        CRC8 crc8 = new CRC8();
        CRC8Driver crc8Driver = new CRC8Driver();
        

        int messageChecksum;
        int checksumReceived = 0;
        int lengthMessageReceived = 0;
        String messageToSend;
        String messageReceived;
        String clientID;
        byte[] sendingBuffer = new byte[512];
        byte[] receivingBuffer = new byte[512];
        String clientsMessageReceived = "";
        boolean traceOn;

        int countMessage = 0;
        int countTransmit = 0;
        int countReTransmit = 0;

        // Set up a link with source and destination ports
        // Any 4-digit number greater than 3000 should be fine. 
        Link myLink = new SimpleLink(receiverPort, senderPort);

        
        //Get server input for error rate
        System.out.println("Enter the error rate (0, 50, 60, 75, 90): ");
        int errorRate = console.nextInt();

        
        // Server Trace on/off?
        console.nextLine();
        System.out.println("Error trace on/off?: ");
        String serverTrace = console.nextLine();

        if (serverTrace.equalsIgnoreCase("off")) {
            traceOn = false;
            System.out.println("Trace is off.");
        } else {
            traceOn = true;
            System.out.println("Trace is On.");
        }
        
        
        do { //while for "bye"-end pgm check

            // Receive a message
            sendingBuffer = new byte[512];
            receivingBuffer = new byte[512];
            byte [] retransmitBuffer = new byte[512];
            
            lengthMessageReceived = 0;
            lengthMessageReceived = myLink.receiveFrame(receivingBuffer);
            
            
            messageReceived = new String(receivingBuffer, 0, lengthMessageReceived);
            
            // Make copy of the receivingBuffer - to be used for errors
            // Able to hold the receiving buffer so when message retransmits 10 times
            // the correct message can be sent via receivingBuffer..
            retransmitBuffer = receivingBuffer;
            
            // Get client ID from the recevingBuffer 
            clientID = new String(receivingBuffer, 0, 3);
            
            // Pull the message length stored in byte 3 of receivingBuffer 
            int messageLength = receivingBuffer[3];
            clientsMessageReceived = new String(receivingBuffer, 4, +messageLength);
            countReTransmit = 0;

           
            do {
                
                String errorMessageReceived = new String(retransmitBuffer, 4, +messageLength);
                
                //MAKE ERRORS - max 10 retransmission
                if (countReTransmit <= 10) {
                    errorFunction(retransmitBuffer, errorRate);
               
                }

                // Calculate the CRC from received payload - after error made
                errorMessageReceived = new String(retransmitBuffer, 4, +messageLength);
                messageChecksum = crc8.checksum(errorMessageReceived.getBytes());

                // Pull CRC from errorMessageReceived (retransmitBuffer)
                checksumReceived = retransmitBuffer[44];

                // Display the information and message received if trace is on
                
                if (traceOn == true) {
                    System.out.println("Clients Message received is: [" + clientID + " | " + messageLength
                            + " | " + errorMessageReceived + " | " + checksumReceived + "]");
                    System.out.println("Message Sender is: " + clientID);
                    System.out.println("Message length is: " + messageLength);
                    System.out.println("Checksum Value is: " + messageChecksum);
                }

                // COMPARE checksum with checkSumReceived  and send message/NAK
                //If not equal send NAK, else break while loop and send the message/response
                if (messageChecksum != checksumReceived) {
                    
                    if (traceOn == true) {
                        System.out.println("*Error. Need Retransmission.*");
                        
                    }
                   //send NAK
                    sendingBuffer[0] = 1;
                    myLink.sendFrame(sendingBuffer, 1);
                    countReTransmit++;
                    countTransmit++;
                    
                }else{ 
                    sendingBuffer[0]= 2;//crc is ok -> so need break out of while
                }

                
            } while (sendingBuffer[0]==1 && countReTransmit<=10 );

            System.out.println("Clients Message received is: [" + clientID + " | " + messageLength
                            + " | " + clientsMessageReceived + " | " + checksumReceived + "]");
            //Was having issue with sending message generated from the rules engine when
            // the max retransmision (10) was reached to be able to send the correct messgage without
            // saving the buffer - So this clientsMessageReceived will send the correct response, but
            // it was not pulled from the same buffer that had the errors created-- instead its the saved message.
            messageToSend = rules.respondToMessage(clientsMessageReceived);
            sendingBuffer = messageToSend.getBytes();
            myLink.sendFrame(sendingBuffer, sendingBuffer.length);
            countTransmit++;
            countMessage++;

        } while (!clientsMessageReceived.contains("bye"));

        // Calculate and print all of the statistics
        System.out.println("\n\n***********STATISTICS REPORT********************");
        System.out.println("Total client messages sent: " + countMessage);
        System.out.println("Total client messages transmitted: " + countTransmit);

        //need to cast the ints as doubles for calculation
        double decimalER = (double)errorRate/100; 
        double theoreticalTransmit = (countMessage / (1 - decimalER));
        System.out.printf("Theoretical total transmission: %.2f", theoreticalTransmit);

        int damagedMsg = countTransmit - countMessage;
        System.out.println("\nNumber of chat messages damaged: " + damagedMsg);
        System.out.println("Maximum number of retransmission for each chat message: " + 10);
// Close the connection
        myLink.disconnect();
        System.exit(0);

    }//end main method
    
    
    
/** Method to check if the message should be damaged - based on random number between 1-100.
* If less than or equal to the inputted error rate, then returns true to damage the message. 
* Else (greater than error rate) returns false to keep message undamaged. 
**/
    private static boolean shouldDamage(int errorRate) {

        int randomNumberX = (int) ((Math.random() * 100) + 1);
        return randomNumberX <= errorRate;
    
    }//end damage method

    
    
    /** Method to create errors on the message. Also based on random number 1-100.
     * Checks if message should be damaged, if it should be damaged make randomeNumberY and check. 
     * else return just the buffer unchanged.
     * 
     * If it should be damaged and If randomNumberY is less than 75 create one error on the frame.
     * Else randomNumberY is greater than 75 create two errors on the frame.
     * 
**/
    public static byte[] errorFunction(byte[] retransmitBuffer, int errorRate) {

        if(shouldDamage(errorRate)){
            int randomNumberY = (int) ((Math.random() * 100) + 1);

            if (randomNumberY <= 75) {
               retransmitBuffer[4] ^= 1;
            } else {
                retransmitBuffer[4] ^= 3;
            }
        }
        return retransmitBuffer;


    }//end error function
}//end CBLinkReceiver class
