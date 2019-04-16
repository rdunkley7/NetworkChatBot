/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rdunkley13
 */
public class RulesEngine {


    public String respondToMessage(String clientsMessageReceived) {

        if(clientsMessageReceived.contains("bye")){//message will end program.
            return "Bye! Thanks for the chat, have a nice day xoxo";
        }
        else if (clientsMessageReceived.contains("hello")
                || clientsMessageReceived.equals("hi") 
                || clientsMessageReceived.contains("hi")) {
            return "Hello";
        } 
        else if(clientsMessageReceived.contains("whats up")
                || clientsMessageReceived.contains("how are you")){
            return "I'm great thanks! hbu? :)";
        }
        else if(clientsMessageReceived.contains("<3")){
            return "I <3 you";
        }
        else if(clientsMessageReceived.contains(":)")){
            return ":D";
        }
        else if(clientsMessageReceived.contains("?")){
            return "I don't know yet";
        }
        else if(clientsMessageReceived.contains("!")){
            return "That's really cool";
        }
        
        else { //Default message if input doesn't match previous events
            return "I don't understand";
        }

    }
}
