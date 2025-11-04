package jjfw.security.rls;


import jjfw.security.UserAuthContext;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.util.List;

public class RlsExecuteListener extends DefaultExecuteListener {

    private final ObjectProvider<UserAuthContext> authContextProvider;

    public RlsExecuteListener(ObjectProvider<UserAuthContext> authContextProvider) {
        this.authContextProvider = authContextProvider;
    }

    @Override
    public void start(ExecuteContext ctx) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            return; // Only initialize in transactional scope
        }

        UserAuthContext auth = authContextProvider.getIfAvailable(UserAuthContext::anonymous);
        Connection conn = ctx.connection();
        if (conn != null) {
            var dsl = DSL.using(conn, SQLDialect.POSTGRES);
            // DB-side guard: initialize once per transaction on this connection
            String initialized = dsl.resultQuery("select current_setting('app.rls_initialized', true)")
                    .fetchOne(0, String.class);
            if (initialized == null || initialized.isBlank()) {
                Integer accountId = auth.getAccountId();
                if (accountId != null) {
                    dsl.execute("select set_config('app.account_id', ?, true)", accountId.toString());
                }
                List<String> groups = auth.getGroups();
                if (groups != null && !groups.isEmpty()) {
                    dsl.execute("select set_config('app.groups', ?, true)", String.join(",", groups));
                }
                dsl.execute("select set_config('app.rls_bypass', ?, true)", auth.isRlsBypass() ? "1" : "0");
                dsl.execute("select set_config('app.rls_initialized', '1', true)");
            }
        }
    }
}
