// LinkReceiver receives a message from LinkSender and replies.
// LinkReceiver needs to be started before LinkSender.

public class LinkReceiver {

	static int senderPort = 3200;   // port number used by sender
	static int receiverPort = 3300; // port number used by receiver
	
	public static void main (String args[]) throws Exception 
	{	
		int lengthMessageReceived = 0;
		String messageToSend;
		String messageReceived;
		byte[] sendingBuffer = new byte[512];
		byte[] receivingBuffer = new byte[512];

		// Set up a link with source and destination ports
		// Any 4-digit number greater than 3000 should be fine. 
		Link myLink = new SimpleLink(receiverPort, senderPort);

		// Receive a message
		lengthMessageReceived = myLink.receiveFrame(receivingBuffer);

		// Display the message
		messageReceived = new String(receivingBuffer, 0, lengthMessageReceived);
		System.out.println("Message received is: [" + messageReceived + "]");	

		// Prepare a message		
		messageToSend = "OK!";
		
		// Convert string to byte array
		sendingBuffer = messageToSend.getBytes();

		// Send the message
		myLink.sendFrame(sendingBuffer, sendingBuffer.length);
		
		// Print out the message sent
		System.out.println("Message sent is:   [" + messageToSend + "]");

		// Close the connection
		myLink.disconnect();
	}
}
