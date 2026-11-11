# BUILD, TESTS ET CI — Kazisafex

**Date** : 11 novembre 2026  
**Référence** : baseline `docs/remediation/BASELINE.md`.

---

## 1. Build local

```bash
mvn -B -ntp -DskipTests package
mvn -B -ntp -Dtests.skip=false test
```

- JDK cible : **Temurin 25** (LTS).
- `pom.xml` : `source/target/release = 25`, `--enable-preview`.
- Enforcer Maven : Java `[25,26)`, Maven `[3.9,)` ; un build hors de ces versions échoue explicitement.
- `tests.skip=true` est le défaut du POM (tests dépendants d'Ollama/DB locales) ;
  **la CI force toujours `-Dtests.skip=false`**.
- Le packaging produit :
  - `target/Kazisafex-2.0.25-build6.jar`
  - `target/Kazisafex-2.0.25-build6-jar-with-dependencies.jar`
  - `target/lib/`.

## 2. Résultat de référence

| Élément | Valeur |
|---|---|
| Tests exécutés | **218** |
| Échecs | 0 |
| Erreurs | 0 |
| Ignorés | 0 |
| Suites | 22 |
| Commande | `mvn -q clean test -Dtests.skip=false` |

Après ajout des tests TLS, `REMOVE`, dead-letter, politique HTTP, garde
d'inventaire, permissions SQLite, verrouillage, outbox atomique, sauvegardes et validation des quantités, le total est de **218** (validation par `mvn clean test`, qui
élimine les rapports Surefire périmés).

## 3. CI GitHub Actions

Fichier : `.github/workflows/ci.yml`.

Jobs :

1. **build-and-test** (bloquant) :
   - checkout ;
   - garde-fou secrets/TLS (`scripts/security-regression.sh`) ;
   - Temurin 25 + cache Maven ;
   - `mvn -Dtests.skip=false test` ;
   - `mvn -DskipTests package` ;
   - publication des rapports Surefire.
2. **static-analysis** (non bloquant au démarrage) :
   - SpotBugs + **FindSecBugs** (configuration dans `pom.xml`) ;
   - OWASP Dependency-Check ;
   - publication du rapport SpotBugs.
3. **secret-scan** :
   - Gitleaks sur tout l'historique (`fetch-depth: 0`).

### Pourquoi le job statique est non bloquant

La base historique contient 504 fichiers de production, 530 `System.out`, 101
`printStackTrace` et de nombreuses God Classes. Rendre SpotBugs bloquant
immédiatement bloquerait toutes les livraisons sans permettre de trier les
findings. Le rapport est publié et devra devenir bloquant après la campagne 6.

## 4. Limites actuelles

- Pas de runner Windows/Linux pour le packaging JavaFX.
- Pas de base MySQL de test en CI.
- Pas de test d'interface JavaFX (TestFX absent).
- Pas de seuil de couverture.
- SpotBugs 4.10.5 analyse Java 25 avec `effort=Default` et `maxHeap=2048` ; le
  rapport est produit (≈2 790 findings, dont des bugs réels). Le job reste non
  bloquant tant que les findings historiques ne sont pas triés.
- Gitleaks peut signaler les secrets historiques déjà connus (clé Ollama
  compromise) : leur présence dans l'historique est documentée dans
  `MANUAL_ACTIONS.md`, elle ne doit pas être « corrigée » en réécrivant
  l'historique sans décision d'équipe.
- Dependency-Check dépend du réseau et de la base NVD ; il est non bloquant.

## 5. Prochaines étapes

1. Rendre `build-and-test` obligatoire sur `master`.
2. Ajouter un job de packaging Windows/Linux.
3. Ajouter SQLite réel en test d'intégration.
4. Rendre SpotBugs bloquant par catégorie, après tri.
5. Ajouter un seuil de couverture progressif (ex. +5 % par campagne).
