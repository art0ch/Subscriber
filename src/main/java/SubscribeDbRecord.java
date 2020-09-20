import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SubscribeDbRecord extends BasicDbRecord {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeDbRecord.class);

    long msisdn;
    long timestamp;

    public SubscribeDbRecord(long msisdn, long timestamp) throws SQLException {
        super();
        this.msisdn = msisdn;
        this.timestamp = timestamp;
        setColumnNames();
        super.record();
    }

    public void setColumnNames() {
        columnNames.add("msisdn");
        columnNames.add("timestamp");
    }

    public String getTableName() {
        return "SUBSCRIBE";
    }

    public PreparedStatement fillStatement(PreparedStatement preparedStatement) throws SQLException {
        logger.info("Input subscribe msisdn: {}", this.msisdn);
        logger.info("Input subscribe timestamp: {}", this.timestamp);

        preparedStatement.setLong(1, this.msisdn);
        preparedStatement.setLong(2, this.timestamp);
        return preparedStatement;
    }
}
