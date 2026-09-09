package data.dto;

/**
 * DTO upsync asymetrique Category : champs scalaires uniquement.
 */
public class CategoryUpsertDto {

    private String uid;
    private String descritption;
    private String updatedAt;

    public CategoryUpsertDto() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescritption() {
        return descritption;
    }

    public void setDescritption(String descritption) {
        this.descritption = descritption;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}