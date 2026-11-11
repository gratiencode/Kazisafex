# Packaging applicatif — Kazisafex

## Image autonome (Linux testé)

Prérequis :

- JDK 25 avec `jpackage` ;
- jar `Kazisafex-*-jar-with-dependencies.jar` construit.

```bash
mvn -q -DskipTests package
./scripts/package-app-image.sh
```

Résultat :

```text
target/jpackage/Kazisafex/
├── bin/Kazisafex
└── lib/
```

L’image embarque un runtime Java et le jar with-dependencies ; elle ne dépend
pas du classpath Maven. Testée sous Linux avec préférences utilisateur isolées.

## Windows

Le même script, exécuté sous Windows avec un JDK 25, produit une image
`app-image` Windows (jpackage ne cross-compile pas). Aucun test Windows n’a
encore été exécuté.

## Limites

- pas d’installateur MSI/DEB signé ;
- pas de signature de code ;
- la mise à jour applicative reste gérée par `UpdateManager`, pas par
  l’installeur.
