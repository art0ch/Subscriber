import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        try {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(80), 0);
            server.createContext("/", new Subscriber());
            server.start();
            logger.info("The Server is up and running");
        } catch (IOException e) {
            logger.error(String.valueOf(e));
            throw e;
        }
    }
}
