package data.network;

import java.io.Serializable;

public class ValidationHashResponse implements Serializable {
    private String hash;
    private String fingerprint;

    public ValidationHashResponse() {}

    public ValidationHashResponse(String hash, String fingerprint) {
        this.hash = hash;
        this.fingerprint = fingerprint;
    }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    @Override
    public String toString() {
        return "ValidationHashResponse{" +
                "hash='" + hash + '\'' +
                ", fingerprint='" + fingerprint + '\'' +
                '}';
    }
}
