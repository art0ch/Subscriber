import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
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
        StringBuilder sb = new StringBuilder();
        InputStream ios = httpExchange.getRequestBody();
        System.out.println(httpExchange.getRequestBody().toString());
        int i;
        while ((i = ios.read()) != -1) {
            sb.append((char) i);
        }

        String json = sb.toString();
        System.out.println(json);

        //String json = URLDecoder.decode(sb.toString(), StandardCharsets.UTF_8);


        try {
            int res = parse(json);
            String response = "";

            if (res == 200) {
                response = "JSON is correct and has been parsed successfully";
                logger.info(response);
            } else {
                res = 400;
                response = "Something is wrong with JSON. You may check logs on the server";
                logger.info(response);
            }

            httpExchange.sendResponseHeaders(res, response.getBytes().length);
            logger.info("The response is: {}", res);

        } catch (IllegalAccessException e) {
            logger.error(String.valueOf(e));
            e.printStackTrace();
        }
        //httpExchange.sendResponseHeaders(200, response.getBytes().length);
        //os.write(response.getBytes());
        //os.flush();
        //os.close();
//        Packet incoming = null;
//        try {
//            logger.info("Incoming string: {}", json);
//            incoming = parse(json);
//
//            if (incoming.getAction().equalsIgnoreCase("purchase")) {
//                logger.info("The action is Purchase");
//                new PurchaseDbRecord(incoming.getMsisdn(), incoming.getTimestamp());
//
//            } else if (incoming.getAction().equalsIgnoreCase("subscribe")) {
//                logger.info("The action is Subscribe");
//                new SubscribeDbRecord(incoming.getMsisdn(), incoming.getTimestamp());
//            } else {
//                response = "Incorrect action";
//                code = 500;
//                logger.error("There is no such action!");
//                throw new IllegalArgumentException("There is no such action!");
//            }
//        } catch (IllegalAccessException | SQLException e) {
//            logger.error(String.valueOf(e));
//            e.printStackTrace();
//        }


    }

    private int parse(String json) throws IllegalAccessException {
        logger.info("JSON parsing has been started");
        JSONParser parser = new JSONParser();
        Reader reader = new StringReader(json);
        JSONObject jsonObject = null;
        Packet packet = new Packet();
        int httpCode = 200;

        try {
            jsonObject = (JSONObject) parser.parse(reader);
            long msisdn = (long) jsonObject.get("msisdn");
            String action = (String) jsonObject.get("action");
            long timestamp = (long) jsonObject.get("timestamp");

            if (msisdn != 0 && !action.isBlank() && timestamp != 0) {
                packet.setMsisdn(msisdn);
                packet.setAction(action);
                packet.setTimestamp(timestamp);
                logger.info("JSON has been parsed successfully");
            } else {
                logger.error("Some JSON fields are empty!");
                throw new IllegalAccessException("Some JSON fields are empty!");
            }

            if (packet.getAction().equalsIgnoreCase("purchase")) {
                logger.info("The action is Purchase");
                new PurchaseDbRecord(packet.getMsisdn(), packet.getTimestamp());

            } else if (packet.getAction().equalsIgnoreCase("subscription")) {
                logger.info("The action is Subscription");
                new SubscriptionDbRecord(packet.getMsisdn(), packet.getTimestamp());
            } else {

                logger.error("There is no such action!");
                throw new IllegalArgumentException("There is no such action!");
            }
        } catch(IOException | ParseException | SQLException e){
            logger.error("An error while parsing, {}", e.toString());
            e.printStackTrace();
        }
        return httpCode;
    }

//    private Map<String, String> queryToMap(String query) {
//        Map<String, String> result = new HashMap<>();
//        String[] strArr = query.split("\\?");
//        for (String part : strArr[1].split("&")) {
//            String[] entry = part.split("=");
//            if (entry.length > 1) {
//                result.put(entry[0], entry[1]);
//            } else {
//                result.put(entry[0], "");
//            }
//        }
//        return result;
//    }

}
