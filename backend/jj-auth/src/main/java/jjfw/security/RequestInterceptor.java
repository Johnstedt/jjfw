package jjfw.security;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jooq.DSLContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private final ObjectProvider<UserAuthContext> authContextProvider;

    private final DSLContext dsl;
    private final AccountCreator accountCreator;

    public RequestInterceptor(
            ObjectProvider<UserAuthContext> authContextProvider,
            DSLContext dsl,
            AccountCreator accountCreator) {
        this.authContextProvider = authContextProvider;
        this.dsl = dsl;
        this.accountCreator = accountCreator;

    }

    // Executes before the request reaches the controller
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("Pre-handle logic: Intercepting request URL - " + request.getRequestURI());
        UserAuthContext auth = authContextProvider.getIfAvailable(UserAuthContext::anonymous);
        String username = auth.getUsername();
        if (username != null && !username.isBlank()) {
            String idStr = dsl.resultQuery(
                    "select id::text from public.account where cognito = ? limit 1", username)
                    .fetchOne(0, String.class);
            if (idStr == null || idStr.isBlank()) {
                idStr = accountCreator.createAccountFromCognitoUser(auth, dsl);
            }
            Integer accountId = Integer.parseInt(idStr);
            auth.setAccountId(accountId);
        }
        return true;
    }

    // Executes after the controller processes the request but before the response is sent
    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        System.out.println("Post-handle logic: Executing after controller logic");

        System.out.println("Pre-handle logic: User authenticated - " + authContextProvider.getIfAvailable(UserAuthContext::anonymous).getUsername());

        // Example: You can modify response headers here
        response.setHeader("X-Custom-Header", "Interceptor added this");
        System.out.println("Post-handle logic: Added custom header to the response");
    }

    // Executes after the entire request is completed
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception exception) throws Exception {
        System.out.println("After completion logic: Cleaning up after request");
    }
}
