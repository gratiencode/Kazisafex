

package data.helpers;

public enum Role {
    //CLIENT SIDE
    Saler,//L0 il s'occupe tout cequi est vente dans une boutique (sys user)
    Finance,//L0 gere la caisse
    Magazinner,// L0 gere le stock (entrepot)
    Manager, // l2, gere les flux et la supervision de(s) boutique(s)
    Trader,//L3 Le proprietaire de la btq, il a acces a tout les ressources de sa btq
    //SERVER SIDE
    Agent,//L4 l'agent endeleya qui travail au back-office pour la prise en charge des plaintes d'users et activation des comptes
    Supervisor,//L5 Il supervise les agents du back office. (peut cree, modifier, valider un compte apres validation de l'admin)
    Worker, //L2 fonctionnaire de l'etat a developper
    Freelancer, // L4 agent distributaire du service kazisafe
    Administrator, //L6 control tout le sys
    //Advanced Access
    Finance_ALL_ACCESS,
    Magazinner_ALL_ACCESS,
    Manager_ALL_ACCESS,
    Saler_ALL_ACCESS,
    ALL_ACCESS;

    private Role() {
    }
}

