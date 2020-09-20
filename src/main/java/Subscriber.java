import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.rmi.NoSuchObjectException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Subscriber implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Subscriber.class);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String request = String.valueOf(httpExchange.getRequestURI());
        Map<String, String> in = queryToMap(request);
        String response = request;
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();

        if (in.containsKey("packet")) {
            Packet incoming = null;
            try {
                String packetString = decode64(in.get("packet"));
                logger.info("Incoming string: {}", packetString);
                incoming = parse(packetString);

                if (incoming.getAction().equalsIgnoreCase("purchase")) {
                    logger.info("The action is Purchase");
                    new PurchaseDbRecord(incoming.getMsisdn(), incoming.getTimestamp());

                } else if (incoming.getAction().equalsIgnoreCase("subscribe")) {
                    logger.info("The action is Subscribe");
                    new SubscribeDbRecord(incoming.getMsisdn(), incoming.getTimestamp());

                } else {
                    logger.error("There is no such action!");
                    throw new IllegalArgumentException("There is no such action!");
                }
            } catch (IllegalAccessException | SQLException e) {
                logger.error(String.valueOf(e));
                e.printStackTrace();
            }
        } else {
            logger.error("Incorrect incoming parameter: (missing packet)");
            throw new NoSuchObjectException("Incorrect incoming parameter");
        }
    }

    private String decode64(String income) {
        return new String(Base64.getDecoder().decode(income), StandardCharsets.UTF_8);
    }

    private Packet parse(String json) throws IllegalAccessException {
        JSONParser parser = new JSONParser();
        Reader reader = new StringReader(json);
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        long msisdn = (long) jsonObject.get("msisdn");
        String action = (String) jsonObject.get("action");
        long timestamp = (long) jsonObject.get("timestamp");

        Packet packet = new Packet();
        if (msisdn != 0 && !action.isBlank() && timestamp != 0) {
            packet.setMsisdn(msisdn);
            packet.setAction(action);
            packet.setTimestamp(timestamp);
            logger.info("JSON has been parsed successfully");
        } else {
            logger.error("Some fields in JSON are empty!");
            throw new IllegalAccessException("Some fields in JSON are empty!");
        }
        return packet;
    }

    private Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        String[] strArr = query.split("\\?");
        for (String part : strArr[1].split("&")) {
            String[] entry = part.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

}
