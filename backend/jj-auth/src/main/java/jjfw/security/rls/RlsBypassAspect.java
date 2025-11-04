package jjfw.security.rls;


import jjfw.security.UserAuthContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RlsBypassAspect {
    private final ObjectProvider<UserAuthContext> authProvider;

    public RlsBypassAspect(ObjectProvider<UserAuthContext> authProvider) {
        this.authProvider = authProvider;
    }

    @Around("@annotation(jjfw.security.rls.RlsBypass) || @within(jjfw.security.rls.RlsBypass)")
    public Object aroundRlsBypass(ProceedingJoinPoint pjp) throws Throwable {
        UserAuthContext auth = authProvider.getIfAvailable(UserAuthContext::anonymous);
        auth.setRlsBypass(true);
        return pjp.proceed();
    }
}
