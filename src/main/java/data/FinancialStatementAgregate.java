package data;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class FinancialStatementAgregate implements Serializable {

    @Id
    @Column(name = "uid", nullable = false, updatable = false)
    private String uid;

    @Column(name = "statement_type")
    private String statementType;

    @Column(name = "line_code")
    private String lineCode;

    @Column(name = "rubrique")
    private String rubrique;

    @Column(name = "nature", columnDefinition = "TEXT")
    private String nature;

    @Column(name = "amount_usd")
    private Double amountUsd;

    @Column(name = "period_start", columnDefinition = "DATE")
    private LocalDate periodStart;

    @Column(name = "period_end", columnDefinition = "DATE")
    private LocalDate periodEnd;

    @Column(name = "fiscal_year")
    private Integer fiscalYear;

    @Column(name = "period_code")
    private String periodCode;

    @Column(name = "region")
    private String region;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "section_header")
    private Boolean sectionHeader = false;

    @Column(name = "total_line")
    private Boolean totalLine = false;

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void onDataOperation() {
        if (uid == null) {
            uid = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatementType() {
        return statementType;
    }

    public void setStatementType(String statementType) {
        this.statementType = statementType;
    }

    public String getLineCode() {
        return lineCode;
    }

    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    public String getRubrique() {
        return rubrique;
    }

    public void setRubrique(String rubrique) {
        this.rubrique = rubrique;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public Double getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(Double amountUsd) {
        this.amountUsd = amountUsd;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getPeriodCode() {
        return periodCode;
    }

    public void setPeriodCode(String periodCode) {
        this.periodCode = periodCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(Boolean sectionHeader) {
        this.sectionHeader = sectionHeader;
    }

    public Boolean getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(Boolean totalLine) {
        this.totalLine = totalLine;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FinancialStatementAgregate other)) {
            return false;
        }
        return Objects.equals(uid, other.uid);
    }
}
