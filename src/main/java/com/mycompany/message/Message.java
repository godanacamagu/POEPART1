package com.mycompany.message;

import javax.swing.JOptionPane;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

class Message {
    private String storedUserName;
    private String storedPassword;
    private String storedFirstName;
    private String storedLastName;
    private String storedCellPhone;
    
    // Message-related fields
    private ArrayList<String[]> sentMessages;
    private ArrayList<String[]> storedMessages;
    private int totalMessagesSent;
    private int messageCounter;
    private static long messageIDCounter = 1000000000L; // Start with 10-digit number

    public Message(String firstName, String lastName) {
        this.storedFirstName = firstName;
        this.storedLastName = lastName;
        this.sentMessages = new ArrayList<>();
        this.storedMessages = new ArrayList<>();
        this.totalMessagesSent = 0;
        this.messageCounter = 1;
    }

    // Checks that username contains underscore and is no more than 5 characters long
    private boolean checkUserName(String userName) {
        if (userName == null) return false;
        return userName.contains("_") && userName.length() <= 5;
    }

    // Checks password complexity: >=8 chars, at least one uppercase, number, special char
    private boolean checkPasswordComplexity(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        return hasUpper && hasNumber && hasSpecial;
    }

    // Regex for South African cell phone number (international +27 prefix + exactly 9 digits after)
    private boolean checkCellPhoneNumber(String cellPhone) {
        if (cellPhone == null) return false;
        String regex = "^\\+27\\d{9}$";
        return Pattern.matches(regex, cellPhone);
    }

    // Register user with validation and return appropriate message
    public String registerUser(String userName, String password, String cellPhone) {
        if (!checkUserName(userName)) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted, please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(cellPhone)) {
            return "Cell number is incorrectly formatted or does not contain an international code, please correct the number and try again.";
        }
        this.storedUserName = userName;
        this.storedPassword = password;
        this.storedCellPhone = cellPhone;
        return "Username successfully captured. Password successfully captured. Cell phone number successfully added.";
    }

    // Verify login credentials
    public boolean loginUser(String userName, String password) {
        if (storedUserName == null || storedPassword == null) return false;
        return storedUserName.equals(userName) && storedPassword.equals(password);
    }

    // Return login status message
    public String returnLoginStatus(boolean loginSuccess) {
        if (loginSuccess) {
            return "Welcome " + storedFirstName + ", " + storedLastName + " it is great to see you again.";
        } else {
            return "Login failed: invalid username or password.";
        }
    }

    // MESSAGE CLASS METHODS

    // Check if message ID is valid (10 digits) - FIXED: Uses string manipulation
    public boolean checkMessageID(String messageID) {
        if (messageID == null || messageID.length() != 10) {
            return false;
        }
        
        // Check each character is a digit using loop and substring
        for (int i = 0; i < 10; i++) {
            String character = messageID.substring(i, i + 1);
            if (!character.matches("\\d")) {
                return false;
            }
        }
        return true;
    }

    // Generate 10-digit message ID using string manipulation - FIXED: No random generation
    private String generateMessageID() {
        String baseID = String.valueOf(messageIDCounter);
        messageIDCounter++;
        
        // Ensure it's exactly 10 digits using substring and padding
        if (baseID.length() > 10) {
            baseID = baseID.substring(0, 10);
        } else if (baseID.length() < 10) {
            // Pad with zeros using string manipulation
            StringBuilder paddedID = new StringBuilder();
            for (int i = 0; i < 10 - baseID.length(); i++) {
                paddedID.append("0");
            }
            paddedID.append(baseID);
            baseID = paddedID.toString();
        }
        
        return baseID;
    }

    // Check recipient cell number format using string manipulation - FIXED
    public int checkRecipientCell(String cellNumber) {
        if (cellNumber == null || cellNumber.isEmpty()) return -1;
        
        // Check international format (+27 followed by 9 digits)
        if (cellNumber.startsWith("+27")) {
            if (cellNumber.length() != 12) return -1;
            
            // Check remaining characters are digits using substring and loop
            String numberPart = cellNumber.substring(3);
            for (int i = 0; i < numberPart.length(); i++) {
                String digit = numberPart.substring(i, i + 1);
                if (!digit.matches("\\d")) {
                    return -1;
                }
            }
            return 1; // Valid South African number
        }
        // Check local number (up to 10 digits)
        else if (cellNumber.length() <= 10) {
            // Check all characters are digits
            for (int i = 0; i < cellNumber.length(); i++) {
                String digit = cellNumber.substring(i, i + 1);
                if (!digit.matches("\\d")) {
                    return -1;
                }
            }
            return 0; // Valid local number
        }
        return -1; // Invalid
    }

    // Create message hash using string manipulation - FIXED
    public String createMessageHash(String messageID, int messageNum, String message) {
        // Get first two digits using substring
        String firstTwo = messageID.substring(0, 2);
        
        // Extract words using split and string manipulation
        String[] words = message.split(" ");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
        
        // Remove non-alphanumeric characters from words
        firstWord = firstWord.replaceAll("[^a-zA-Z0-9]", "");
        lastWord = lastWord.replaceAll("[^a-zA-Z0-9]", "");
        
        return (firstTwo + ":" + messageNum + ":" + firstWord + lastWord).toUpperCase();
    }

    // Send message with options - FIXED: Includes proper JSON storage
    public String sentMessage(String recipient, String messageText) {
        // Generate 10-digit message ID using string manipulation
        String messageID = generateMessageID();
        
        // Create message hash
        String messageHash = createMessageHash(messageID, messageCounter, messageText);
        
        // Store message details
        String[] messageDetails = {
            messageID,
            messageHash,
            recipient,
            messageText,
            String.valueOf(messageCounter),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
        };
        
        // Show options to user
        String[] options = {"Send Message", "Disregard Message", "Store Message to send later"};
        int choice = JOptionPane.showOptionDialog(null,
            "Message ready:\n\n" +
            "Message ID: " + messageID + "\n" +
            "Recipient: " + recipient + "\n" +
            "Message: " + messageText + "\n" +
            "Message Hash: " + messageHash + "\n\n" +
            "What would you like to do?",
            "Message Options",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[0]);
        
        switch (choice) {
            case 0: // Send Message
                sentMessages.add(messageDetails);
                totalMessagesSent++;
                messageCounter++;
                
                // Store in JSON file
                String storeResult = storeMessage(messageDetails);
                if (storeResult.contains("successfully")) {
                    return "Message sent successfully!\n" + storeResult;
                } else {
                    return "Message sent but storage failed: " + storeResult;
                }
                
            case 1: // Disregard Message
                return "Message disregarded.";
                
            case 2: // Store Message
                storedMessages.add(messageDetails);
                String storeResult2 = storeMessage(messageDetails);
                return "Message stored for later sending.\n" + storeResult2;
                
            default:
                return "Operation cancelled.";
        }
    }

    // Return list of all sent messages
    public String returnAllMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages sent yet.";
        }
        
        StringBuilder allMessages = new StringBuilder("=== ALL SENT MESSAGES ===\n\n");
        for (String[] message : sentMessages) {
            allMessages.append("Message ID: ").append(message[0]).append("\n")
                      .append("Message Hash: ").append(message[1]).append("\n")
                      .append("Recipient: ").append(message[2]).append("\n")
                      .append("Message: ").append(message[3]).append("\n")
                      .append("Message Number: ").append(message[4]).append("\n")
                      .append("Timestamp: ").append(message[5]).append("\n\n");
        }
        return allMessages.toString();
    }

    // Return total number of messages sent
    public int returnTotalMessages() {
        return totalMessagesSent;
    }

    // Store message in JSON file - FIXED: Actual file I/O implementation
    public String storeMessage(String[] messageDetails) {
        try {
            // Create messages directory if it doesn't exist
            Files.createDirectories(Paths.get("messages"));
            
            // Create JSON content
            String jsonContent = "{\n" +
                " \"messageID\": \"" + messageDetails[0] + "\",\n" +
                " \"messageHash\": \"" + messageDetails[1] + "\",\n" +
                " \"recipient\": \"" + messageDetails[2] + "\",\n" +
                " \"message\": \"" + escapeJsonString(messageDetails[3]) + "\",\n" +
                " \"messageNumber\": \"" + messageDetails[4] + "\",\n" +
                " \"timestamp\": \"" + messageDetails[5] + "\",\n" +
                " \"sender\": \"" + storedFirstName + " " + storedLastName + "\",\n" +
                " \"username\": \"" + storedUserName + "\"\n" +
                "}";
            
            // Write to file
            String filename = "messages/message_" + messageDetails[0] + ".json";
            try (FileWriter file = new FileWriter(filename)) {
                file.write(jsonContent);
                file.flush();
            }
            
            return "Message stored successfully in: " + filename;
            
        } catch (IOException e) {
            return "Error storing message: " + e.getMessage();
        }
    }
    
    // Helper method to escape JSON strings
    private String escapeJsonString(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    // Store all messages in a single JSON file
    public String storeAllMessages() {
        try {
            Files.createDirectories(Paths.get("messages"));
            
            StringBuilder jsonArray = new StringBuilder("[\n");
            for (int i = 0; i < sentMessages.size(); i++) {
                String[] message = sentMessages.get(i);
                jsonArray.append(" {\n")
                        .append(" \"messageID\": \"").append(message[0]).append("\",\n")
                        .append(" \"messageHash\": \"").append(message[1]).append("\",\n")
                        .append(" \"recipient\": \"").append(message[2]).append("\",\n")
                        .append(" \"message\": \"").append(escapeJsonString(message[3])).append("\",\n")
                        .append(" \"messageNumber\": \"").append(message[4]).append("\",\n")
                        .append(" \"timestamp\": \"").append(message[5]).append("\",\n")
                        .append(" \"sender\": \"").append(storedFirstName).append(" ").append(storedLastName).append("\",\n")
                        .append(" \"username\": \"").append(storedUserName).append("\"\n")
                        .append(" }");
                
                if (i < sentMessages.size() - 1) {
                    jsonArray.append(",");
                }
                jsonArray.append("\n");
            }
            jsonArray.append("]");
            
            String filename = "messages/all_messages.json";
            try (FileWriter file = new FileWriter(filename)) {
                file.write(jsonArray.toString());
                file.flush();
            }
            
            return "All messages stored in: " + filename;
            
        } catch (IOException e) {
            return "Error storing all messages: " + e.getMessage();
        }
    }

    // Main application method
    public void runMessagingApp() {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.");
        
        boolean running = true;
        while (running) {
            String[] mainOptions = {"Send Messages", "Show recently sent messages", "Store All Messages", "Quit"};
            int mainChoice = JOptionPane.showOptionDialog(null,
                "Please choose an option:",
                "QuickChat Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                mainOptions,
                mainOptions[0]);
            
            switch (mainChoice) {
                case 0: // Send Messages
                    sendMessagesFlow();
                    break;
                    
                case 1: // Show recently sent messages
                    String messageDisplay = returnAllMessages();
                    if (messageDisplay.contains("No messages")) {
                        JOptionPane.showMessageDialog(null, "Coming Soon.\n\n" + messageDisplay);
                    } else {
                        JOptionPane.showMessageDialog(null, messageDisplay);
                    }
                    break;
                    
                case 2: // Store All Messages
                    String storeResult = storeAllMessages();
                    JOptionPane.showMessageDialog(null, storeResult);
                    break;
                    
                case 3: // Quit
                default:
                    running = false;
                    String finalMessage = "Thank you for using QuickChat!\n\n" +
                                         "Total messages sent: " + returnTotalMessages() + "\n" +
                                         "Stored messages: " + storedMessages.size();
                    JOptionPane.showMessageDialog(null, finalMessage);
                    break;
            }
        }
    }

    private void sendMessagesFlow() {
        try {
            String numMessagesStr = JOptionPane.showInputDialog(null, 
                "How many messages would you like to send?");
            if (numMessagesStr == null) return;
            
            int numMessages = Integer.parseInt(numMessagesStr);
            
            for (int i = 0; i < numMessages; i++) {
                // Get recipient
                String recipient = "";
                boolean validRecipient = false;
                while (!validRecipient) {
                    recipient = JOptionPane.showInputDialog(null, 
                        "Enter recipient cell number (Message " + (i + 1) + " of " + numMessages + "):\n" +
                        "Format: +27XXXXXXXXX for South Africa or local number up to 10 digits");
                    
                    if (recipient == null) {
                        JOptionPane.showMessageDialog(null, "Message cancelled.");
                        return;
                    }
                    
                    int cellCheck = checkRecipientCell(recipient);
                    if (cellCheck >= 0) {
                        validRecipient = true;
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Invalid cell number format. Please use:\n" +
                            "+27XXXXXXXXX for South Africa (12 characters total)\n" +
                            "OR local number up to 10 digits");
                    }
                }
                
                // Get message
                String messageText = "";
                boolean validMessage = false;
                while (!validMessage) {
                    messageText = JOptionPane.showInputDialog(null, 
                        "Enter your message (max 250 characters):\nMessage " + (i + 1) + " of " + numMessages);
                    
                    if (messageText == null) {
                        JOptionPane.showMessageDialog(null, "Message cancelled.");
                        return;
                    }
                    
                    if (messageText.length() <= 250) {
                        validMessage = true;
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Please enter a message of less than 250 characters.\n" +
                            "Your message is currently " + messageText.length() + " characters long.");
                    }
                }
                
                // Process message
                String result = sentMessage(recipient, messageText);
                JOptionPane.showMessageDialog(null, result);
                
                // If message was sent, show details
                if (result.contains("sent successfully")) {
                    String[] lastMessage = sentMessages.get(sentMessages.size() - 1);
                    String messageDetails = "=== MESSAGE DETAILS ===\n\n" +
                        "Message ID: " + lastMessage[0] + "\n" +
                        "Message Hash: " + lastMessage[1] + "\n" +
                        "Recipient: " + lastMessage[2] + "\n" +
                        "Message: " + lastMessage[3] + "\n" +
                        "Message Number: " + lastMessage[4] + "\n" +
                        "Timestamp: " + lastMessage[5];
                    JOptionPane.showMessageDialog(null, messageDetails);
                }
            }
            
            JOptionPane.showMessageDialog(null, 
                "All messages processed!\n\n" +
                "Total messages sent in this session: " + returnTotalMessages() + "\n" +
                "Messages stored for later: " + storedMessages.size());
                
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number entered. Please enter a valid number.");
        }
    }

    public static void main(String[] args) {
        String firstName = "";
        String lastName = "";
        
        while (true) {
            firstName = JOptionPane.showInputDialog(null, "Enter First Name (must be 'Teddy'):");
            if (firstName == null) {
                JOptionPane.showMessageDialog(null, "Registration cancelled.");
                return;
            }
            
            lastName = JOptionPane.showInputDialog(null, "Enter Last Name (must be 'Sithebe'):");
            if (lastName == null) {
                JOptionPane.showMessageDialog(null, "Registration cancelled.");
                return;
            }
            
            if ("Teddy".equals(firstName) && "Sithebe".equals(lastName)) {
                break;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid names. First name must be 'Teddy' and last name must be 'Sithebe'. Please try again.");
            }
        }
        
        Message messageApp = new Message(firstName, lastName);

        String username = "";
        boolean validUsername = false;
        while (!validUsername) {
            username = JOptionPane.showInputDialog(null, "Enter Username (must contain '_' and be ≤5 characters):");
            if (username == null) {
                JOptionPane.showMessageDialog(null, "Registration cancelled.");
                return;
            }
            validUsername = messageApp.checkUserName(username);
            if (!validUsername) {
                JOptionPane.showMessageDialog(null, "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
            }
        }

        String password = "";
        boolean validPassword = false;
        while (!validPassword) {
            password = JOptionPane.showInputDialog(null, "Enter Password (≥8 chars, with uppercase, number, and special char):");
            if (password == null) {
                JOptionPane.showMessageDialog(null, "Registration cancelled.");
                return;
            }
            validPassword = messageApp.checkPasswordComplexity(password);
            if (!validPassword) {
                JOptionPane.showMessageDialog(null, "Password is not correctly formatted, please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
            }
        }

        String cellPhone = "";
        boolean validCellPhone = false;
        while (!validCellPhone) {
            cellPhone = JOptionPane.showInputDialog(null, "Enter South African Cell Phone Number (format: +27XXXXXXXXX):");
            if (cellPhone == null) {
                JOptionPane.showMessageDialog(null, "Registration cancelled.");
                return;
            }
            validCellPhone = messageApp.checkCellPhoneNumber(cellPhone);
            if (!validCellPhone) {
                JOptionPane.showMessageDialog(null, "Cell number is incorrectly formatted or does not contain an international code, please correct the number and try again.");
            }
        }

        String registrationMessage = messageApp.registerUser(username, password, cellPhone);
        JOptionPane.showMessageDialog(null, registrationMessage);

        if (registrationMessage.contains("successfully")) {
            boolean loginSuccess = false;
            int attempts = 0;
            final int MAX_ATTEMPTS = 3;
            
            while (!loginSuccess && attempts < MAX_ATTEMPTS) {
                String loginUser = JOptionPane.showInputDialog(null, "Enter Username for Login:");
                if (loginUser == null) {
                    JOptionPane.showMessageDialog(null, "Login cancelled.");
                    return;
                }
                
                String loginPass = JOptionPane.showInputDialog(null, "Enter Password for Login:");
                if (loginPass == null) {
                    JOptionPane.showMessageDialog(null, "Login cancelled.");
                    return;
                }
                
                loginSuccess = messageApp.loginUser(loginUser, loginPass);
                attempts++;
                
                if (!loginSuccess && attempts < MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(null, "Login failed. Please try again. Attempts remaining: " + (MAX_ATTEMPTS - attempts));
                }
            }
            
            String loginMessage = messageApp.returnLoginStatus(loginSuccess);
            JOptionPane.showMessageDialog(null, loginMessage);
            
            if (loginSuccess) {
                // Run the messaging application after successful login
                messageApp.runMessagingApp();
            } else {
                JOptionPane.showMessageDialog(null, "Maximum login attempts exceeded. Please restart the application.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Registration failed. Please restart the application and try again.");
        }
    }
}