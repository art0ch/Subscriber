import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;

public class Subscriber implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Subscriber.class);
    private HttpExchange httpExchange;
    Packet packet;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        this.httpExchange = httpExchange;
        StringBuilder sb = new StringBuilder();
        InputStream ios = httpExchange.getRequestBody();
        logger.info("The request body: {}", ios.toString());

        int i;
        while ((i = ios.read()) != -1) {
            sb.append((char) i);
        }

        String json = sb.toString();
        logger.info("Incoming JSON string: {}", json);
        JSONObject jsonObject = parseStringIntoJson(json);
        try {
            this.packet = unpackJson(jsonObject);
        } catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }

        String action = recognizeAction(packet);
        createRecord(action);
        onSuccess();
    }


    private JSONObject parseStringIntoJson(String json) {
        JSONParser parser = new JSONParser();
        Reader reader = new StringReader(json);
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException ioException) {
            logger.error("Error while parsing JSON into string!");
            logger.error(String.valueOf(ioException));
        }
        return jsonObject;
    }

    Packet unpackJson(JSONObject jsonObject) throws IllegalAccessException, IOException {
        Packet packet = new Packet();

        long msisdn = (long) jsonObject.get("msisdn");
        String action = (String) jsonObject.get("action");
        long timestamp = (long) jsonObject.get("timestamp");

        logger.info("msisdn: {}", msisdn);
        logger.info("action: {}", action);
        logger.info("timestamp: {}", timestamp);

        if (msisdn != 0 && !action.isBlank() && timestamp != 0) {
            packet.setMsisdn(msisdn);
            packet.setAction(action);
            packet.setTimestamp(timestamp);
            logger.info("JSON has been parsed successfully");
        } else {
            onError("Some JSON fields are empty!");
            throw new IllegalAccessException("Some JSON fields are empty!");
        }
        return packet;
    }

    String recognizeAction(Packet packet) throws IOException {
        String action = packet.getAction();
        if (action.equalsIgnoreCase("purchase")) {
            logger.info("The action is Purchase");
        } else if (action.equalsIgnoreCase("subscription")) {
            logger.info("The action is Subscription");
        } else {
            onError("There is no such action!");
            throw new IllegalArgumentException("There is no such action!");
        }
        return action;
    }

    void createRecord(String action) {
        try {
            if (action.equalsIgnoreCase("purchase")) {
                PurchaseDbRecord purchaseDbRecord = new PurchaseDbRecord();
                purchaseDbRecord.setMsisdn(packet.getMsisdn());
                purchaseDbRecord.setTimestamp(packet.getTimestamp());
                purchaseDbRecord.record();
            } else {
                SubscriptionDbRecord subscriptionDbRecord = new SubscriptionDbRecord();
                subscriptionDbRecord.setMsisdn(packet.getMsisdn());
                subscriptionDbRecord.setTimestamp(packet.getTimestamp());
                subscriptionDbRecord.record();
            }
        } catch (SQLException throwables) {
            logger.error(String.valueOf(throwables));
            throwables.printStackTrace();
        }
    }


    void onError(String response) throws IOException {
        int res = 400;
        logger.error(response);
        logger.info("The response code is: {}", res);
        this.httpExchange.sendResponseHeaders(res, response.getBytes().length);
        OutputStream os = this.httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    void onSuccess() throws IOException {
        int res = 200;
        String response = "JSON has been successfully parsed and saved to DB!";
        logger.info(response);
        logger.info("The response code is: {}", res);
        this.httpExchange.sendResponseHeaders(res, response.getBytes().length);
        OutputStream os = this.httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}


