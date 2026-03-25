package data.core;

public final class AuthTokenState {

    private volatile String token;

    public AuthTokenState(String token) {
        this.token = normalize(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = normalize(token);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            trimmed = trimmed.substring(7).trim();
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
}
