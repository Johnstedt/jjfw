package jjfw.security.rls;


import jjfw.security.UserAuthContext;
import org.jooq.DSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RlsConfig {

    @Bean
    public RlsExecuteListener rlsConfigurer(
            DSLContext dsl,
            ObjectProvider<UserAuthContext> authContextProvider
    ) {
        var listener = new RlsExecuteListener(authContextProvider);
        org.jooq.Configuration cfg = dsl.configuration();
        cfg.set(new DefaultExecuteListenerProvider(listener));
        return listener;
    }
}
