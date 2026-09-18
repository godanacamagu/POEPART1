package chat.chat_demo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 * package chat.chat_demo;
 *
 *
 * @author Camagu Godana
 */
public class Chat_Demo {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+\\d{1,3})?\\d{10}$");
    
    private static final ArrayList<String> sentMessages = new ArrayList<>();

    
    private static int showMenu() {
        String[] options = {"Send Messages", "Show Recently Sent", "Quit"};
        return JOptionPane.showOptionDialog(
                null,
                "Choose an option:",
                "Message Application",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
    }


    private static void sendMessages() {
        String inputCount = JOptionPane.showInputDialog(null, "How many messages do you want to send?");
        if (inputCount == null) return;

        try {
            int count = Integer.parseInt(inputCount);
            if (count <= 0) {
                JOptionPane.showMessageDialog(null, "Please enter a positive number.");
                return;
            }

            String phoneNumber = getValidPhoneNumber();
            if (phoneNumber == null) return;

            for (int i = 1; i <= count; i++) {
                String message = JOptionPane.showInputDialog(null, "Enter your message " + i + " (max 250 characters):");
                if (message == null || message.trim().isEmpty()) continue;

                if (message.length() > 250) {
                    JOptionPane.showMessageDialog(null, "Error: Message exceeds 250 characters!", "Input Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                handleMessageOptions(message);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number entered.");
        }
    }

   
    private static String getValidPhoneNumber() {
        while (true) {
            String input = JOptionPane.showInputDialog(null, "Enter phone number (10 digits, optional +country code):");
            if (input == null) {
                JOptionPane.showMessageDialog(null, "Operation cancelled.");
                return null;
            }

            Matcher matcher = PHONE_PATTERN.matcher(input);
            if (matcher.matches()) {
                JOptionPane.showMessageDialog(null, "Valid phone number entered: " + input);
                
                return input;
            } else {
                JOptionPane.showMessageDialog(null, "Invalid phone number! Try again.");
            }
        }
    }


    private static void handleMessageOptions(String message) {
    int messageID = ThreadLocalRandom.current().nextInt(1000000000, 2000000000);
    int messageNumber = sentMessages.size() + 1;
    sentMessages.add("Unique Id:" + messageID + "  " + message);

    String[] messageOptions = {"Send Message", "Disregard Message", "Store Message"};
    int choice = JOptionPane.showOptionDialog(
            null,
            "Choose an option for your message:",
            "",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            messageOptions,
            messageOptions[0]
    );

    switch (choice) {
        case 0 -> {
            String hash = generateMessageHash(messageID, messageNumber, message);
            JOptionPane.showMessageDialog(null, "Your Message was successfully sent!");
            JOptionPane.showMessageDialog(null, "Id: " + messageID + " Hash: "+ hash + " Recipient: " + messageNumber + " Message: " + message);
      

            int totalSentMessages = sentMessages.size();
            JOptionPane.showMessageDialog(null, "Total Sent Messages: " + totalSentMessages);
        }
        case 1 -> {
            sentMessages.remove(sentMessages.size() - 1);
            JOptionPane.showMessageDialog(null, "Message Removed!");
        }
        case 2 -> saveMessageToFile(message);
    }
}

private static String generateMessageHash(int messageID, int messageNumber, String message) {
    String idPrefix = String.valueOf(messageID).substring(0, 2);
    String[] words = message.trim().split("\\s+");
    String firstWord = words.length > 0 ? words[0] : "";
    String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;

    String hash = String.format("%s:%d:%s%s", idPrefix, messageNumber, firstWord, lastWord);
    return hash.toUpperCase();
}


    private static void saveMessageToFile(String message) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter file = new FileWriter("message.json")) {
            gson.toJson(message, file);
            JOptionPane.showMessageDialog(null, "Message saved to message.json!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving message: " + e.getMessage());
        }
    }


    private static void showSentMessages() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No messages have been sent yet.");
        } else {
            StringBuilder sb = new StringBuilder("Recently Sent Messages:\n");
            for (String msg : sentMessages) {
                sb.append(msg).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        }
    }

    public static void main(String[] args) {

        JOptionPane.showMessageDialog(null, "" + "Welcome to QuickChat");

        boolean running = true;

        while (running) {
            int choice = showMenu();
            switch (choice) {
                case 0 ->
                    sendMessages();
                case 1 -> showSentMessages();
                case 2, JOptionPane.CLOSED_OPTION -> {
                    running = false;
                    JOptionPane.showMessageDialog(null, "Goodbye!");
                }
                default -> {
                    running = false;
                    JOptionPane.showMessageDialog(null, "Goodbye!");
                }
            }
        }
        System.exit(0);

    }
}
