package data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * Reponse d'erreur structuree renvoyee par le serveur quand une mutation
 * upsync echoue. Le client la parse pour decider de l'auto-healing des parents
 * manquants (ex: 417 client, 412 tresor, 460 produit, 461 mesure).
 *
 * Format cote serveur : {"message": "...", "missingUid": "...",
 * "missingType": "CLIENT|PRODUIT|MESURE|COMPTETRESOR|...", "produitUid": "...",
 * "ligneUid": "..."}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncErrorResponse {

    private String code;
    private String message;
    private String codeType;
    private String missingType;
    private String missingUid;
    private String produitUid;
    private String ligneUid;

    public SyncErrorResponse() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public String getMissingType() {
        return missingType;
    }

    public void setMissingType(String missingType) {
        this.missingType = missingType;
    }

    public String getMissingUid() {
        return missingUid;
    }

    public void setMissingUid(String missingUid) {
        this.missingUid = missingUid;
    }

    public String getProduitUid() {
        return produitUid;
    }

    public void setProduitUid(String produitUid) {
        this.produitUid = produitUid;
    }

    public String getLigneUid() {
        return ligneUid;
    }

    public void setLigneUid(String ligneUid) {
        this.ligneUid = ligneUid;
    }

    /**
     * @return true si l'erreur designe un parent manquant que le client peut
     * recuperer localement et repousser (auto-healing possible).
     */
    public boolean isMissingParent() {
        return missingType != null && !missingType.isBlank()
                && !Objects.equals(missingType, "PRODUIT_TENANT")
                && !Objects.equals(missingType, "REFERENCE");
    }

    @Override
    public String toString() {
        return "SyncErrorResponse{code=" + code + ", codeType=" + codeType
                + ", missingType=" + missingType + ", missingUid=" + missingUid
                + ", produitUid=" + produitUid + ", ligneUid=" + ligneUid
                + ", message=" + message + '}';
    }
}