package data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "compte_resultat_agregate")
public class CompteResultatAgregate extends FinancialStatementAgregate {

    public CompteResultatAgregate() {
        setStatementType("COMPTE_RESULTAT");
    }
}
