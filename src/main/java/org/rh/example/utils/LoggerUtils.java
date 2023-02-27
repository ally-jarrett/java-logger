package org.rh.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoggerUtils {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    @Value("${java.logger.log.char_count:0}")
    private int charCount;

    /**
     * Generate logging string with specified padding
     * @param logMessage
     * @return
     */
    public String padLoggingStringToCharCount(String logMessage) {
        if (charCount > 0) {

            // Pad string
            if (logMessage.length() < charCount) {
                int padCharLength = charCount - logMessage.length();
                String padding = padRight("", padCharLength - 1);

                // Add padding if required
                if (padding.length() > 0) {
                    logMessage = logMessage + " " + padding;
                }
            }
        }
        return logMessage;
    }

    /**
     * Create a padded string to concat
     * @param s
     * @param n
     * @return
     */
    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s).replace(' ', '*');
    }

}
