import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class BasicDbRecord {

    private static final Logger logger = LoggerFactory.getLogger(BasicDbRecord.class);

    public BasicDbRecord() throws SQLException {
        setConnection();
    }

    final static String DB_URL = "jdbc:postgresql://localhost:5432/pub-sub";
    final static String USER = "postgres";
    final static String PASS = "ghbdtnrfrltkf";
    private Connection connection = null;
    ArrayList<String> columnNames = new ArrayList<>();

    abstract String getTableName();

    abstract PreparedStatement fillStatement(PreparedStatement preparedStatement) throws SQLException;

    abstract void setColumnNames();

    public Connection getConnection() {
        return connection;
    }

    public void setConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("There is no driver class for Postgresql!", e);
            e.printStackTrace();
        }

        connection = DriverManager.getConnection(DB_URL, USER, PASS);

        if (!connection.isClosed()) {
            logger.info("The connection to DB {} is established", DB_URL);
        } else {
            logger.error("The connection is NOT established!");
        }
    }

    public void closeConnection() {
        try {
            connection.close();
            logger.info("The connection is closed");
        } catch (SQLException throwables) {
            logger.error("The connection can't be closed", throwables);
        }

    }

    private String getColumnNamesString() {
        StringBuilder res = new StringBuilder();
        for (String str : columnNames) {
            res.append(str).append(", ");
        }
        res.setLength(res.length() - 2);
        return res.toString();
    }

    private String getValuesString() {
        StringBuilder res = new StringBuilder();
        for (String str : columnNames) {
            res.append("?, ");
        }
        res.setLength(res.length() - 2);
        return res.toString();
    }

    public String getStatement() {
        String sql = "INSERT INTO \""
                + getTableName()
                + "\" ("
                + getColumnNamesString()
                + ") Values ("
                + getValuesString()
                + ")";

        return sql;
    }

    public void record() throws SQLException {
        setColumnNames();
        Connection conn = getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(getStatement());
        preparedStatement = fillStatement(preparedStatement);
        logger.info("The SQL request is: {}", preparedStatement);
        preparedStatement.execute();
        logger.info("The request to DB has been executed");
        closeConnection();
    }
}
