import jjfw.DatabaseMigrator;
import jjfw.Dependency;
import jjfw.common.Config;

import java.io.IOException;
import java.sql.SQLException;

public class Purge {
    public static void main(String args[]) throws SQLException, IOException, InterruptedException {
        Dependency.call();
        Config.setSitePath(args[0]);
        DatabaseMigrator.purgeTables();
    }
}
