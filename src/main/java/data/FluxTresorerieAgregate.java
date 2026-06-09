package data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "flux_tresorerie_agregate")
public class FluxTresorerieAgregate extends FinancialStatementAgregate {

    public FluxTresorerieAgregate() {
        setStatementType("FLUX_TRESORERIE");
    }
}
