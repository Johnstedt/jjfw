package jjfw.config;

import jjfw.common.Config;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://" +Config.get("db_host") +":" + Config.getNum("db_port") + "/" + Config.get("db_name"));
        dataSource.setUsername(Config.get("db_user"));
        dataSource.setPassword(Config.get("db_pass"));
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    /*@Bean
    public DataSource txAwareDataSource(DataSource dataSource) {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean
    public DSLContext dslContext(DataSource txAwareDataSource) {
        return DSL.using(txAwareDataSource, SQLDialect.POSTGRES);
    }*/

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
