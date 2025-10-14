import java.sql.DriverManager;
import java.sql.SQLException;

import jjfw.common.Config;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Generator;
import org.jooq.impl.DSL;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Target;

public class GenJooqStuff {
    public static void main(String[] args) throws SQLException {
        Config.setSitePath(args[0]);
        generateSchemas();

       /* getDSL().execute("""
               CREATE TABLE trees (
                    id BIGSERIAL PRIMARY KEY,
                    species VARCHAR(100) NOT NULL,
                    diameter_cm DECIMAL(8,2) NOT NULL,
                    height_m DECIMAL(6,2) NOT NULL,
                    age_years INTEGER,
                    location VARCHAR(200),
                    planted_date DATE,
                    health_status VARCHAR(50) DEFAULT 'healthy',
                    circumference_cm DECIMAL(10,2),
                    canopy_width_m DECIMAL(6,2),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX idx_trees_species ON trees(species);

                COMMENT ON COLUMN trees.diameter_cm IS '@Api.exclude Tree trunk diameter in centimeters';
        
        """);
*/

    }

    public static DSLContext getDSL() throws SQLException {
        /*var connection = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/tcdb", "pgsuper", "fff0");
        return DSL.using(connection, SQLDialect.POSTGRES);*/
        return null;
    }

    public static Jdbc getMetaDatabase() {
        return new Jdbc()
            .withDriver("org.postgresql.Driver")
            .withUrl("jdbc:postgresql://" + Config.get("db_host") +":" + Config.getNum("db_port") + "/" + Config.get("db_name"))
            .withUser(Config.get("db_user"))
            .withPassword(Config.get("db_pass"));
    }

    public static void generateSchemas() {
        try {
            // Configure jOOQ code generation
            Configuration configuration = new Configuration()
                    .withJdbc(getMetaDatabase())
                    .withGenerator(new org.jooq.meta.jaxb.Generator()
                            .withName("CustomJavaGenerator")
                            .withDatabase(new Database()
                                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public"))
                            .withGenerate(new Generate()
                                    .withRecords(true)
                                    //.withPojosAsJavaRecordClasses(true)
                                    //.withInterfaces(true)
                                    .withComments(true)
                                    .withPojos(true)
                                    .withDaos(true)
                            )
                            .withTarget(new Target()
                                    .withPackageName("generated.jooq")
                                    .withDirectory("src/main/java")));

            // Generate the schema
            GenerationTool.generate(configuration);
            System.out.println("Schema generation completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during schema generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
