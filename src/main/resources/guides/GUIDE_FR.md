# Kazisafe — Guide d'utilisation

> Bienvenue dans le guide officiel de Kazisafe. Ce document vous explique, etape par etape, comment utiliser toutes les fonctionnalites du logiciel de gestion commerciale et financiere pour PME.

---

## 1. Presentation de Kazisafe

Kazisafe est un logiciel de gestion integre qui permet de piloter votre commerce :

- Gestion des **produits**, **mesures** et **stocks**
- **Ventes** au comptoir avec caisse et facturation
- **Achats**, **livraisons fournisseurs** et **requisitions**
- **Clients**, **fournisseurs** et **tresorerie**
- **Production**, **immobilisations** et **inventaire**
- **Rapports** financiers et etats de stock
- Assistant intelligent **Gratien** qui enregistre vos documents et repond a vos questions
- Fonctionnement **hors ligne** avec synchronisation automatique

| Element | Role |
|---|---|
| Menu de gauche | Acces rapide a tous les modules |
| Barre du haut | Recherche, parametres, aide |
| Assistant Gratien | Chat intelligent integre a droite |

---

## 2. Demarrage et connexion

1. Lancez Kazisafe depuis votre bureau ou le menu demarrer.
2. A la premiere ouverture, l'application cree automatiquement les dossiers de travail et la base de donnees locale.
3. Saisissez le **token d'activation** et l'**identifiant de l'entreprise** fournis par votre revendeur.
4. Choisissez votre **region** (ex : Goma, Bukavu, Kinshasa...) puis la **devise principale** (USD ou CDF).
5. Cliquez sur **Connexion**.

> L'application se connecte a votre base locale meme sans internet. Les donnees sont ensuite synchronisees periodiquement avec le cloud.

---

## 3. L'ecran d'accueil (Dashboard)

L'ecran d'accueil affiche une vue d'ensemble de votre activite :

- **Chiffre d'affaires** du jour, de la semaine et du mois
- **Ventes recentes** et top produits
- **Alertes de stock** (produits en rupture ou sous le seuil)
- **Tresorerie** disponible par compte
- **Dettes fournisseurs** et **creances clients**

Pour actualiser les donnees, cliquez sur le bouton **Actualiser** en haut a droite de chaque carte.

---

## 4. Gestion des produits

### 4.1 Creer un produit

1. Ouvrez le menu **Produits**.
2. Cliquez sur le bouton **Nouveau produit** (+).
3. Renseignez :
   - **Nom du produit** et **code-barres**
   - **Categorie**, **marque**, **modele**
   - **Taille** et **couleur** (optionnel)
   - **Image** du produit
4. Cliquez sur **Enregistrer**.

### 4.2 Modifier ou supprimer un produit

- Cliquez droit sur un produit dans la liste pour **modifier** ou **supprimer**.
- La suppression est desactivee si le produit possede de l'historique de stock ou de ventes.

### 4.3 Categories et marques

Depuis le module Produits, vous pouvez gerer :

- Les **categories** (boissons, alimentation, electronique, ...)
- Les **marques** et **modeles**
- Les **groupes** de produits

### 4.4 Mesures (unites)

Chaque produit possede des **mesures** (Piece, Kg, Carton, Litre...).

1. Ouvrez un produit puis l'onglet **Mesures**.
2. Cliquez sur **Ajouter une mesure**.
3. Renseignez la **description** et la **quantite de contenu** :
   - Ex : 1 Carton = 12 Pieces → description "Carton", quantContenu = 12
4. Enregistrez.

> La mesure de base (quantContenu = 1) est utilisee pour le stock de reference. Les autres mesures permettent les ventes par carton, pack, etc.

---

## 5. Fournisseurs

1. Ouvrez le menu **Fournisseurs**.
2. Cliquez sur **Nouveau fournisseur**.
3. Renseignez : **nom**, **adresse**, **telephone**, **identifiant**, **RCCM**, **numero d'impot**.
4. Enregistrez.

Vous pouvez ensuite :

- **Modifier** les coordonnees
- **Consulter l'historique** des livraisons et des dettes du fournisseur
- **Regler les dettes** directement depuis la fiche fournisseur

---

## 6. Livraisons et approvisionnements (Achat)

Une **facture fournisseur** se transforme en approvisionnement : reception de marchandises puis mise en stock. Vous pouvez l'enregistrer de **deux facons** :

| Methode | Quand l'utiliser |
|---|---|
| Avec l'assistant **Gratien** | Facture en photo ou PDF, sans saisie |
| **Manuellement** | Saisie directe au clavier, ligne par ligne |

### 6.1 Enregistrer une facture fournisseur avec l'assistant Gratien

1. Ouvrez le chat **Gratien** (panneau de droite).
2. Cliquez sur l'icone **piece jointe** et selectionnez la **photo ou le PDF** de la facture fournisseur.
3. Ecrivez l'instruction, ex : *« Enregistre cet approvisionnement »* ou *« Fais entrer cette facture en stock »*.
4. Gratien lit la facture et cree automatiquement :
   - les **produits** (il retrouve les existants ou en cree de nouveaux)
   - le **fournisseur**
   - la **livraison** avec le numero de piece et la reference de la facture
   - les **requisitions** (quantites, couts d'achat, lots, dates d'expiration)
5. Verifiez les informations proposées puis **confirmez** avant la validation.
6. Le stock est ajoute : controlez l'entree dans **Approvisionnement** et l'etat de stock.

> Gratien deduit les couts d'achat et les prix de vente des montants de la facture. Si la devise de la facture differe de la devise principale, il demande confirmation avant conversion.

### 6.2 Enregistrer une facture fournisseur manuellement

1. Ouvrez le menu **Approvisionnement / Achats**.
2. Choisissez la provenance **Achat**.
3. Cliquez sur **Ajouter** : le formulaire de livraison s'ouvre.
4. Selectionnez ou creez le **fournisseur** de la facture.
5. Renseignez le **numero de piece** (numero figurant sur la facture) et la **reference**.
6. Pour chaque article de la facture, ajoutez : produit, **lot**, **quantite**, **mesure**, **cout d'achat**, **date d'expiration** et **stock d'alerte**.
7. Cliquez sur **Enregistrer la livraison**.
8. Puis creez la **requisition** pour mettre la marchandise en stock vendable (voir section 7).

> Le cout d'achat et le prix de vente peuvent aussi etre remplis automatiquement par Gratien a partir de la facture (photo ou PDF) meme si vous enregistrez la livraison manuellement.

### 6.3 Le lot

Chaque marchandise recue possede un **numero de lot** qui permet de :

- Retrouver la date d'expiration
- Appliquer la methode FIFO / LIFO / FEFO pour la sortie des stocks
- Tracer l'origine de chaque article

---

## 7. Requisitions (mise en stock)

La **requisition** cree le stock disponible a la vente, par produit, lot et mesure.

### 7.1 Provenance « Achat »

1. Dans **Approvisionnement**, selectionnez une **livraison**.
2. Cliquez sur l'icone **+** pour ajouter un article.
3. La requition pre-remplit : produit, **cout d'achat**, quantite, lot.
4. Verifiez le **prix de vente** par paliers et la **mesure de vente**.
5. Cliquez sur **Enregistrer** : le stock est ajoute.

### 7.2 Provenance « Entrepot »

Pour recevoir des marchandises deja departees depuis un autre depot :

1. Dans **Approvisionnement**, choisissez la provenance **Entrepot**.
2. Cliquez sur **Ajouter**.
3. La liste **Reference depot** affiche les destockages envoyes vers votre region.
4. Selectionnez la **reference** : le produit, le **lot**, le **cout d'achat** et la **quantite** se remplissent automatiquement.
5. Ajustez si necessaire puis **Enregistrer**.

> Les entrees « Entrepot » n'exigent pas de numero de piece fournisseur : elles proviennent de vos propres depots.

---

## 8. Destockage et entreposage

Le module **Destockage** sort des articles du stock d'un depot pour les envoyer vers un autre depot ou une autre region.

1. Ouvrez le menu **Destockage**.
2. Selectionnez le **produit** : les lots disponibles et le **cout d'achat** s'affichent automatiquement.
3. Choisissez la **mesure** de sortie et la **quantite**.
4. Choisissez la **destination** (region cible) dans la liste.
5. Saisissez la **reference** (ex : DST12345K) et une **observation**.
6. Cliquez sur **Ajouter au tableau**, puis **Valider** pour enregistrer le destockage.

> Le destockage retire le stock du depot source. Le depot destination pourra ensuite le recevoir via une requisition de provenance **Entrepot**.

---

## 9. Ventes et caisse (POS)

### 9.1 Effectuer une vente

1. Ouvrez le menu **Vente / Caisse**.
2. Recherchez un produit par **code-barres**, nom ou marque.
3. Cliquez sur le produit pour l'ajouter au panier.
4. Modifiez la **quantite** et verifiez le **prix** et la **mesure** de vente.
5. Choisissez le **client** (ou « Client de passage »).
6. Cliquez sur **Payer** :
   - Selectionnez le **mode de paiement** (Especes, Mobile Money, Carte, Credit)
   - Saisissez le **montant recu**
   - Le logiciel calcule la **monnaie a rendre**
7. Validez : le ticket est imprime (optionnel) et le stock diminue.

### 9.2 Vente a credit

- Choisissez un client enregistre puis le mode de paiement **Credit**.
- La creance est ajoutee a la fiche du client et peut etre **recouvree** depuis le menu Clients ou Tresorerie.

### 9.3 Historique des ventes

- L'onglet **Ventes** affiche toutes les transactions.
- Vous pouvez **voir le detail**, **imprimer** un recu ou **annuler** une vente (avec autorisation).

---

## 10. Clients

1. Ouvrez le menu **Clients**.
2. Cliquez sur **Nouveau client** et renseignez **nom**, **adresse**, **telephone**.
3. Enregistrez.

Chaque fiche client contient :

- L'**historique des achats**
- Les **creances** (ventes a credit en cours)
- Les **reglements** effectues

---

## 11. Tresorerie

### 11.1 Comptes de tresorerie

1. Ouvrez le menu **Tresorerie**.
2. Creez vos comptes : **Caisse**, **Banque**, **Mobile Money**...
3. Renseignez la **devise** de chaque compte.

### 11.2 Operations

- **Entree de fonds** : approvisionnement d'un compte
- **Sortie de fonds** : retrait ou paiement
- **Depenses** : enregistrez les charges avec **motif**, **montant** et **date**
- **Transfert** : deplacement de fonds entre comptes

> Les depenses peuvent etre saisies manuellement ou lues automatiquement depuis un recu par l'assistant Gratien.

### 11.3 Reglements

- **Regler une dette fournisseur** : selectionnez la livraison et saisissez le montant
- **Recouvrer une creance client** : selectionnez la vente a credit et enregistrez le paiement

---

## 12. Production

Le module Production permet de fabriquer des articles finis a partir de matieres premieres.

1. Ouvrez le menu **Production**.
2. Creez une **nomenclature** (liste des matieres premieres et quantites necessaires).
3. Lancez une **fabrication** : le logiciel decremente les matieres et incremente le produit fini.
4. Suivez le **couts** de production dans les etats.

---

## 13. Immobilisations

1. Ouvrez le menu **Immobilisations**.
2. Enregistrez vos actifs : **designation**, **date d'acquisition**, **valeur**, **duree d'amortissement**.
3. Le logiciel calcule les **amortissements** automatiquement.

---

## 14. Inventaire et comptage

1. Ouvrez le menu **Inventaire**.
2. Creez un **comptage** pour une region ou un depot.
3. Saisissez la **quantite physique** de chaque produit.
4. Comparez avec le stock theorique : les **ecarts** sont affiches.
5. Validez le comptage : le stock est **ajuste**.

> L'ajustement cree automatiquement les ecritures de stock (entrees ou sorties) avec reference d'inventaire.

---

## 15. Rapports et etats

Le menu **Rapports** donne acces a :

| Rapport | Contenu |
|---|---|
| Etat de stock | Stock par produit, lot, mesure et valeur |
| Ventes | Chiffre d'affaires par periode, par produit, par vendeur |
| Achats | Livraisons et depenses d'approvisionnement |
| Tresorerie | Mouvements et soldes des comptes |
| Compte de resultat | Produits, charges et resultat |
| Bilan | Actif, passif et capitaux propres |
| Flux de tresorerie | Encaissements et decaissements |

Pour chaque rapport :

1. Choisissez la **periode** (du / au) et la **region**.
2. Cliquez sur **Generer**.
3. **Exportez** en Excel, PDF ou imprimez le document.

---

## 16. Parametres

Le menu **Parametres** (roue dentee) permet de configurer :

- **Entreprise** : nom, adresse, telephone, identifiant, logo
- **Devise principale** et **taux de change** USD/CDF
- **Region** et depots
- **Langue** de l'interface (Francais, Anglais, Swahili, Lingala, Kinyarwanda, Arabe, Hindi)
- **Theme** clair / sombre
- **Methode de stock** : FIFO, LIFO ou FEFO
- **Imprimante** et format de ticket
- **Utilisateurs et permissions** : qui peut vendre, annuler, modifier, supprimer

> Chaque modification est appliquee immediatement. Certaines preferences demandent un redemarrage.

---

## 17. L'assistant Gratien

**Gratien** est l'assistant intelligent integre a Kazisafe. Il vous aide a :

### 17.1 Enregistrer un document

Envoyez a Gratien une **photo** ou un **PDF** de facture fournisseur, recu ou ticket :

1. Cliquez sur l'icone **piece jointe** dans le chat.
2. Selectionnez votre document.
3. Ecrivez votre instruction, ex : *« Enregistre cet approvisionnement »* ou *« Cree la depense depuis ce recu »*.
4. Gratien lit le document, cree les **produits**, **fournisseur**, **livraison** et **requisitions** automatiquement.
5. Il vous demande confirmation et detail avant de valider.

> Pour enregistrer une facture fournisseur sans Gratien, suivez la procedure manuelle en **section 6.2**. Pour les depenses, voyez la **section 11.2**.

### 17.2 Repondre a vos questions

Posez vos questions en langage naturel :

- *« Comment creer un produit ? »*
- *« Quel est mon stock de Coca 1.5L ? »*
- *« Combien me doit ce client ? »*
- *« Fais le bilan du mois dernier »*

Gratien repond en utilisant le **guide d'utilisation** de Kazisafe et vos donnees en temps reel.

### 17.3 Commandes speciales

- `/kanuni <instruction>` : enregistre une instruction personnalisee que Gratien respectera toujours.
- *« Annule »* : arrete une operation en cours avec confirmation.

---

## 18. Synchronisation et multi-postes

Kazisafe fonctionne sur plusieurs postes qui partagent les memes donnees :

- Chaque poste travaille sur sa **base locale** (fonctionnement hors ligne garanti).
- Les donnees sont **synchronisees** avec le cloud automatiquement quand la connexion est disponible.
- La **synchronisation en temps reel** utilise les WebSockets pour les postes connectes.
- Vous pouvez forcer la synchronisation depuis le **menu de mise a jour** (bouton de synchronisation).

> Avant de travailler, verifiez l'icone de synchronisation en haut : verte = a jour, orange = en attente.

---

## 19. Sauvegarde et maintenance

- La **base de donnees locale** est creee automatiquement dans le dossier de travail de Kazisafe.
- Effectuez des **sauvegardes periodiques** du dossier `Kazisafe/Media` et du fichier de base de donnees.
- Gardez toujours une **copie de votre token d'activation** en lieu sur.

---

## 20. Depannage

| Probleme | Solution |
|---|---|
| Application lente | Verifiez la connexion internet et la synchronisation en attente |
| Stock incorrect | Faites un inventaire de comptage et validez les ecarts |
| Synchronisation bloquee | Redemarrez l'application puis forcez la synchronisation |
| Mot de passe oublie | Contactez votre administrateur ou votre revendeur |
| Imprimante ne repond pas | Verifiez le port USB et le format de ticket dans les parametres |

---

## 21. Assistance

Pour toute assistance :

- Ouvrez le menu **Aide ?** en bas de la barre laterale : le **guide PDF** de cette documentation s'ouvre.
- Interrogez **Gratien** directement dans le chat.
- Contactez votre **revendeur** ou l'equipe Kazisafe avec votre numero de licence.

> Merci d'utiliser Kazisafe. Ce guide est aussi disponible au format PDF depuis le menu **Aide ?**.
