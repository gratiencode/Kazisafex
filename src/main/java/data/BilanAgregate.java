package data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "bilan_agregate")
public class BilanAgregate extends FinancialStatementAgregate {

    public BilanAgregate() {
        setStatementType("BILAN");
    }
}
