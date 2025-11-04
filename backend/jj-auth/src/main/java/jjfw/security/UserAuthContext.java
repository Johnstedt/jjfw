package jjfw.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserAuthContext {
    private final boolean authenticated;
    private final String subject;
    private final String username;
    private final String jwt;
    private Integer accountId;
    private final List<String> groups;
    private boolean rlsBypass;

    // Added for CGLIB proxying of request-scoped bean
    protected UserAuthContext() {
        this.authenticated = false;
        this.subject = null;
        this.username = null;
        this.jwt = null;
        this.accountId = null;
        this.groups = List.of();
        this.rlsBypass = false;
    }

    private UserAuthContext(boolean authenticated, String subject, String username, String jwt, List<String> groups) {
        this.authenticated = authenticated;
        this.subject = subject;
        this.username = username;
        this.jwt = jwt;
        this.groups = groups == null ? Collections.emptyList() : List.copyOf(groups);
        this.rlsBypass = false;
    }

    public static UserAuthContext fromJwt(Jwt jwt) {
        String sub = jwt.getSubject();
        Map<String, Object> claims = jwt.getClaims();
        String username = valueAsString(claims.get("cognito:username"), valueAsString(claims.get("username"), null));
        String jwt2 = jwt.getTokenValue();
        List<String> groups = extractGroups(claims);
        return new UserAuthContext(true, sub, username, jwt2, groups);
    }

    public static UserAuthContext anonymous() {
        return new UserAuthContext(false, null, null, null, List.of());
    }

    public boolean isAuthenticated() { return authenticated; }
    public String getSubject() { return subject; }
    public String getUsername() { return username; }
    public String getJwt() { return jwt; }
    public List<String> getGroups() { return groups; }

    public boolean isInGroup(String g) { return groups.contains(g); }

    public boolean isAdmin() {
        return groups.stream().anyMatch(g -> g.equalsIgnoreCase("ADMIN") || g.equalsIgnoreCase("ROLE_ADMIN"));
    }

    public boolean isRlsBypass() { return rlsBypass; }
    public void setRlsBypass(boolean rlsBypass) { this.rlsBypass = rlsBypass; }

    private static String valueAsString(Object v, String def) {
        return v == null ? def : v.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractGroups(Map<String, Object> claims) {
        Object v = claims.get("cognito:groups");
        if (v instanceof Collection<?> c) return c.stream().map(Object::toString).toList();
        v = claims.get("groups");
        if (v instanceof Collection<?> c2) return c2.stream().map(Object::toString).toList();
        return List.of();
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
}
