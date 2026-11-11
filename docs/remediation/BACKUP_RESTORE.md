# Sauvegarde et restauration SQLite

## Sauvegardes automatiques

- avant migration Hibernate : au plus une par jour ;
- avant clôture d'inventaire : immédiate, sous le verrou d'inventaire ;
- fichiers : `kazi_<uid>.db.bak-<label>-<timestamp>` ;
- empreinte : fichier `.sha256` compagnon (format `sha256sum`) ;
- test CI : `scripts/test-restore-backup.sh` ;
- rétention : 5 sauvegardes de migration / 10 sauvegardes de clôture.

## Vérification

`DatabaseBackup.verify(Path)` recalcule le SHA-256 et retourne `false` si la
copie ou l'empreinte ne concordent pas. Utilisé par les tests.

## Restauration manuelle

L'application **doit être fermée**.

```bash
./scripts/restore-sqlite-backup.sh \
  /chemin/kazi_<uid>.db.bak-inventory-close-<timestamp> \
  /chemin/kazi_<uid>.db \
  --confirm
```

Le script :

1. exige `--confirm` ;
2. exige la sauvegarde et son `.sha256` ;
3. refuse une empreinte invalide ;
4. copie puis remplace la base cible ;
5. supprime les anciens `-wal`/`-shm`.

Aucune restauration n'est déclenchée automatiquement par l'application.
