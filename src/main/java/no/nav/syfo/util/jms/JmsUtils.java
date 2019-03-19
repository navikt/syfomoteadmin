package no.nav.syfo.util.jms;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.core.MessageCreator;

import javax.jms.TextMessage;
import java.security.SecureRandom;

import static java.util.Optional.ofNullable;

@Slf4j
public class JmsUtils {

    public static MessageCreator messageCreator(final String message) {
        String callId = ofNullable(getFromMDC(MDC_CALL_ID))
                .orElseGet(JmsUtils::generateCallId);
        return messageCreator(message, callId);
    }

    public static MessageCreator messageCreator(final String message, final String callId) {
        return session -> {
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setStringProperty("callId", callId);
            return textMessage;
        };
    }

    public static final String MDC_CALL_ID = "callId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_CONSUMER_ID = "consumerId";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCallId() {
        int randomNr = getRandomNumber();
        long systemTime = getSystemTime();

        StringBuilder callId = new StringBuilder();
        callId.append("CallId_");
        callId.append(systemTime);
        callId.append("_");
        callId.append(randomNr);

        return callId.toString();
    }

    public static String getFromMDC(String key) {
        String value = MDC.get(key);
        log.debug("Getting key: " + key + " from MDC with value: " + value);
        return value;
    }

    public static void putToMDC(String key, String value) {
        log.debug("Putting value: " + value + " on MDC with key: " + key);
        MDC.put(key, value);
    }

    public static void remove(String key) {
        log.debug("Removing key: " + key);
        MDC.remove(key);
    }

    private static int getRandomNumber() {
        int value = RANDOM.nextInt(Integer.MAX_VALUE);
        return value;
    }

    private static long getSystemTime() {
        return System.currentTimeMillis();
    }
}
