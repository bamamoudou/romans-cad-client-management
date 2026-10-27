# Client Management – Exercice technique Développeur Java

Gestion d'une base de **plus d'un million de clients** et de leurs **groupes** (un client peut appartenir à
plusieurs groupes), exposée par quatre applications construites par étapes successives :

| # | Application | Technologie | Rôle |
|---|---|---|---|
| 1 | `client-management-soap` | WAR – JAX-WS | Services SOAP : le cœur fonctionnel, base en mémoire |
| 2 | `client-management-soap-cli` | JAR exécutable | Client Java en ligne de commande, appelle le SOAP |
| 3 | `client-management-rest` | WAR – JAX-RS | Couche d'abstraction REST **au-dessus** des services SOAP |
| 4 | `client-management-rest-cli` | JAR exécutable | Client Java en ligne de commande, appelle le REST |

Contraintes respectées : Java 21, WildFly 41.0.1, Jakarta EE 11, **sans Spring ni framework équivalent**
(CDI, JAX-WS, JAX-RS et JSON fournis par la plateforme), données **en mémoire** initialisées
**par programme** au démarrage (1 000 000 de clients par défaut).

---

## Sommaire

1. [Architecture](#1-architecture)
2. [Prérequis](#2-prérequis)
3. [Structure du dépôt](#3-structure-du-dépôt)
4. [Construire le projet](#4-construire-le-projet)
5. [Démarrage rapide](#5-démarrage-rapide)
6. [Application 1 – Services SOAP](#6-application-1--services-soap)
7. [Application 2 – Client SOAP en ligne de commande](#7-application-2--client-soap-en-ligne-de-commande)
8. [Application 3 – Passerelle REST](#8-application-3--passerelle-rest)
9. [Application 4 – Client REST en ligne de commande](#9-application-4--client-rest-en-ligne-de-commande)
10. [Hypothèses](#10-hypothèses)
11. [Choix techniques et justifications](#11-choix-techniques-et-justifications)
12. [Données de test, mémoire et mesures](#12-données-de-test-mémoire-et-mesures)
13. [Gestion des erreurs](#13-gestion-des-erreurs)
14. [Tests et vérification](#14-tests-et-vérification)
15. [Limites connues et pistes d'amélioration](#15-limites-connues-et-pistes-damélioration)
16. [Dépannage](#16-dépannage)

---

## 1. Architecture

```
                      ┌──────────────────────────────────────────────── WildFly 41.0.1 ───────────────────────────────────────────────┐
                      │                                                                                                               │
 client-management-   │   client-management-rest (WAR)                    client-management-soap (WAR)                                │
 rest-cli (JAR)       │   ┌───────────────────────────┐   SOAP / HTTP     ┌───────────────────────────────────────────────┐           │
 ──── REST / JSON ───▶│   │ JAX-RS resources          │ ────────────────▶ │ JAX-WS endpoints (Customer, Group, Membership)│           │
                      │   │  └─ gateways + SOAP stubs │                   │  └─ service métier (verrou lecture/écriture)  │           │
 curl, navigateur ───▶│   └───────────────────────────┘                   │      └─ dépôts en mémoire (ConcurrentSkipListMap) │        │
                      │                                                   └───────────────────────────────────────────────┘           │
 client-management-   │                                                                                                               │
 soap-cli (JAR)       │                                                                                                               │
 ──── SOAP ──────────▶│ ──────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
```

Deux principes structurent tout le projet :

- **La passerelle REST ne contourne jamais le SOAP.** Le module REST n'a *aucune* dépendance sur le module
  `core` : il ne connaît que le contrat SOAP (WSDL) et passe par des stubs JAX-WS générés, exactement comme un
  client externe. C'est ce que demande l'énoncé (« couche d'abstraction REST au-dessus des services SOAP »).
- **Les deux clients en ligne de commande partagent le même moteur** (`client-management-cli-common`) : seule la
  façon de joindre le serveur change (SOAP ou REST). Le moteur ne dépend que des interfaces de service du domaine.

Couches du serveur SOAP (de l'extérieur vers l'intérieur) :

```
Endpoints JAX-WS ─▶ Services (règles métier, verrou) ─▶ Dépôts (ports) ─▶ Implémentations en mémoire
   (SOAP, DTO JAXB)        DefaultClientManagementService        interfaces            ConcurrentSkipListMap...
```

---

## 2. Prérequis

| Outil | Version | Remarque |
|---|---|---|
| JDK | 21 ou plus | `java -version` |
| Maven | 3.6.3 ou plus | le build a été validé avec 3.6.3 |
| WildFly | **41.0.1.Final** (Jakarta EE 11) | distribution « standalone », `$WILDFLY_HOME` |
| Git | n'importe laquelle | |

Les API JAX-WS/JAXB ne font plus partie du profil Jakarta EE 11 : elles sont déclarées à part dans les POM, mais
WildFly 41.0.1 les fournit toujours à l'exécution (JBossWS / Apache CXF 4.1.8, JAXB 4.0.5). Les WAR restent donc
légers : rien n'est embarqué (`provided`).

---

## 3. Structure du dépôt

```
client-management-parent (pom)
├── client-management-core            JAR   Domaine + dépôts en mémoire + générateur de données (Java pur, aucune dépendance EE)
├── client-management-cli-common      JAR   Moteur de ligne de commande partagé (arguments, commandes, affichage, codes de sortie)
├── client-management-soap            WAR   Étape 1 – services SOAP (JAX-WS) + initialisation des données
├── client-management-soap-cli        JAR   Étape 2 – client SOAP (stubs générés + adaptateurs)  → java -jar
├── client-management-rest            WAR   Étape 3 – passerelle REST (JAX-RS) au-dessus du SOAP
├── client-management-rest-cli        JAR   Étape 4 – client REST (java.net.http + JSON-P)       → java -jar
└── scripts/generate-soap-stubs.sh          Régénère les stubs SOAP d'un module à partir des WSDL
```

Pourquoi `core` est du Java pur : le métier est testable sans serveur d'applications, et il pourrait être
réutilisé par un autre mode de déploiement.

---

## 4. Construire le projet

```bash
mvn clean verify
```

Produit :

| Artefact | Chemin |
|---|---|
| Services SOAP | `client-management-soap/target/client-management-soap.war` |
| Passerelle REST | `client-management-rest/target/client-management-rest.war` |
| Client SOAP | `client-management-soap-cli/target/client-management-soap-cli.jar` (exécutable, autonome) |
| Client REST | `client-management-rest-cli/target/client-management-rest-cli.jar` (exécutable, autonome) |

Les stubs SOAP (du code généré à partir des WSDL) sont **versionnés dans le dépôt** : le build ne nécessite donc
aucun serveur en marche. Pour les régénérer, voir [§7.5](#75-régénérer-les-stubs).

---

## 5. Démarrage rapide

```bash
# 1. Construire
mvn clean verify

# 2. Démarrer WildFly avec assez de mémoire pour 1 million de clients (voir §12)
export WILDFLY_HOME=/chemin/vers/wildfly-41.0.1.Final
JBOSS_JAVA_SIZING="-Xms512m -Xmx2g" $WILDFLY_HOME/bin/standalone.sh

# 3. Dans un autre terminal : déployer les deux WAR
cp client-management-soap/target/client-management-soap.war $WILDFLY_HOME/standalone/deployments/
cp client-management-rest/target/client-management-rest.war $WILDFLY_HOME/standalone/deployments/
#    Attendre le message « Sample data ready » dans le journal de WildFly (quelques secondes).

# 4. Essayer les clients
java -jar client-management-soap-cli/target/client-management-soap-cli.jar customer get 500
java -jar client-management-rest-cli/target/client-management-rest-cli.jar customer get 500
curl -s http://localhost:8080/client-management-rest/api/customers/500
```

### 5.1 Utiliser les clients en ligne de commande (il n'y a pas de menu)

Les deux clients ne sont **pas interactifs** : chaque lancement exécute **une commande** puis se termine. C'est un
choix voulu : l'outil reste utilisable dans des scripts, grâce à ses codes de sortie (§7.3). Le « menu » est la
commande `help`, qui liste toutes les commandes et leurs options :

```bash
java -jar client-management-soap-cli/target/client-management-soap-cli.jar help
```

Pour ne pas retaper les longues commandes, définir deux raccourcis dans le terminal (depuis la racine du dépôt) :

```bash
alias soap='java -jar client-management-soap-cli/target/client-management-soap-cli.jar'
alias rest='java -jar client-management-rest-cli/target/client-management-rest-cli.jar'
```

**Parcours d'essai complet** (WildFly démarré, les deux WAR déployés). Après une commande, `echo $?` affiche son
code de sortie.

| # | Commande | Résultat attendu |
|---|---|---|
| 1 | `soap help` / `rest help` | la liste des commandes (identique pour les deux clients) |
| 2 | `soap customer get 500` puis `rest customer get 500` | le même client, par SOAP puis par REST |
| 3 | `soap customer list --limit 3` | 3 clients et un indicateur de page suivante |
| 4 | `soap customer list --after 3 --limit 3` | les clients 4, 5 et 6 (page suivante) |
| 5 | `soap customer create --first-name Ada --last-name Lovelace --email ada@example.com` | le client créé, avec son identifiant |
| 6 | `soap group create "Test SOAP" --description "essai"` | le groupe créé (noter son identifiant : 21 sur une base neuve) |
| 7 | `rest group create "test soap"` | refus : nom déjà pris (la casse est ignorée), code de sortie 1 |
| 8 | `soap group add 21 1 2 3` | associe les clients 1, 2 et 3 au groupe 21 |
| 9 | `rest group customers 21` | les clients 1, 2 et 3 : le REST lit ce que le SOAP a écrit |
| 10 | `soap customer groups 1` | les groupes du client 1, dont « Test SOAP » |
| 11 | `rest overview --limit 2` | 2 clients avec leurs groupes |
| 12 | `soap group add 21 1 99999999` | refus : client 99999999 introuvable, code 1, **aucune** association créée (opération atomique) |

Les commandes de dissociation et de suppression se trouvent dans la sortie de `help`.

**Vérification de la volumétrie** : démarrer WildFly avec le jeu de données complet
(`JBOSS_JAVA_SIZING="-Xms512m -Xmx2g" $WILDFLY_HOME/bin/standalone.sh`, sans `-Dclientmanagement.seed.customers`),
attendre `Sample data ready: 1,000,000 customers, …` dans le journal, puis :

```bash
soap customer get 1000000                          # le dernier client existe
soap customer get 1000001                          # introuvable (code de sortie 1)
soap customer list --after 999990 --limit 20       # dernière page : pas de page suivante
soap group customers 1 --limit 5                   # le plus gros groupe (~400 000 clients) : réponse immédiate
```

---

## 6. Application 1 – Services SOAP

### 6.1 Déploiement

1. Démarrer WildFly **avec un tas Java suffisant** (le défaut, 512 Mo, provoque une `OutOfMemoryError` avec
   1 000 000 de clients – voir [§12](#12-données-de-test-mémoire-et-mesures)) :
   ```bash
   JBOSS_JAVA_SIZING="-Xms512m -Xmx2g" $WILDFLY_HOME/bin/standalone.sh
   ```
2. Copier le WAR :
   ```bash
   cp client-management-soap/target/client-management-soap.war $WILDFLY_HOME/standalone/deployments/
   ```
3. Surveiller le journal. Deux lignes confirment le démarrage :
   ```
   Generating sample data: SeedConfiguration[customerCount=1000000, groupCount=20, randomSeed=42]
   Sample data ready: 1,000,000 customers, 20 groups, 1,441,221 memberships in ... ms
   ```
   Le déploiement n'est terminé qu'après la génération des données (elle est **synchrone** : voir §11.9).

### 6.2 Configuration (propriétés système, passées à WildFly avec `-D`)

| Propriété | Défaut | Rôle |
|---|---|---|
| `clientmanagement.seed.customers` | `1000000` | Nombre de clients générés (`0` = base vide) |
| `clientmanagement.seed.groups` | `20` | Nombre de groupes générés (0 à 20) |
| `clientmanagement.seed.randomSeed` | `42` | Graine : même graine = mêmes données à chaque démarrage |

Exemple, pour un démarrage rapide pendant le développement :
```bash
$WILDFLY_HOME/bin/standalone.sh -Dclientmanagement.seed.customers=20000
```

### 6.3 Les trois services

Chaque service a son WSDL, consultable une fois le WAR déployé :

| Service | WSDL | Opérations |
|---|---|---|
| Clients | `http://localhost:8080/client-management-soap/CustomerWebService?wsdl` | `createCustomer`, `getCustomer`, `deleteCustomer`, `listCustomers` |
| Groupes | `http://localhost:8080/client-management-soap/GroupWebService?wsdl` | `createGroup`, `getGroup`, `deleteGroup`, `listGroups` |
| Appartenances | `http://localhost:8080/client-management-soap/MembershipWebService?wsdl` | `addCustomersToGroup`, `removeCustomersFromGroup`, `listCustomersOfGroup`, `listGroupsOfCustomer`, `listCustomersWithGroups` |

Ces opérations couvrent les huit exigences de l'étape 1 : ajouter/supprimer des clients, ajouter/supprimer des
groupes, associer/dissocier clients et groupes, lister les clients d'un groupe, lister les groupes d'un client,
lister les clients avec leurs groupes.

Espace de noms du contrat (versionné) : `http://clientmanagement.romanscad.com/v1`.

### 6.4 Comportement du contrat

- **Listes paginées par curseur** : `listXxx(afterId, limit)` renvoie au plus `limit` éléments (1 à 1000, 100 par
  défaut) dont l'identifiant est strictement supérieur à `afterId`, plus un `nextCursor` à repasser comme
  `afterId` (absent sur la dernière page).
- **Association / dissociation** : *idempotentes* (associer un client déjà membre n'est pas une erreur) et
  *atomiques* (si un client n'existe pas, rien n'est modifié). Maximum **1000 identifiants par appel**. Le résultat
  est le nombre d'appartenances réellement modifiées.
- **Suppression d'un client ou d'un groupe** : supprime aussi ses appartenances ; les clients d'un groupe supprimé
  ne sont pas supprimés.
- **Erreurs** : une seule faute SOAP, `ClientManagementFault`, portant un `ErrorDetail` (code, message, identifiant
  de la ressource). Codes : `CUSTOMER_NOT_FOUND`, `GROUP_NOT_FOUND`, `DUPLICATE_GROUP_NAME`, `INVALID_REQUEST`.

### 6.5 Vérification rapide

```bash
curl -s -H 'Content-Type: text/xml' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://clientmanagement.romanscad.com/v1"><soapenv:Body><v1:getCustomer><v1:customerId>500</v1:customerId></v1:getCustomer></soapenv:Body></soapenv:Envelope>' \
  http://localhost:8080/client-management-soap/CustomerWebService
```

---

## 7. Application 2 – Client SOAP en ligne de commande

### 7.1 Lancement

```bash
java -jar client-management-soap-cli/target/client-management-soap-cli.jar [--url <adresse>] <commande> [arguments]
```

- `--url` : adresse de l'application SOAP, **placée avant la commande**. Défaut :
  `http://localhost:8080/client-management-soap`. Les deux formes `--url X` et `--url=X` sont acceptées.
- `help` affiche la liste exacte des commandes (elle est générée à partir des définitions de commandes : elle ne
  peut pas être obsolète).

### 7.2 Exemples

```bash
CLI="java -jar client-management-soap-cli/target/client-management-soap-cli.jar"

$CLI help
$CLI customer get 500
$CLI customer list --limit 3
$CLI customer list --after 3 --limit 3            # page suivante : on repart du dernier identifiant reçu
$CLI customer list --all                           # tout lister, page après page
$CLI customer create --first-name Ada --last-name Lovelace --email ada@example.com
$CLI group create "Demo Group" --description "Créé par le client SOAP"
$CLI group add 21 1 2 3                            # associer les clients 1, 2, 3 au groupe 21
$CLI group customers 21                            # clients du groupe 21
$CLI customer groups 1                             # groupes du client 1
$CLI overview --limit 2                            # clients avec leurs groupes
$CLI --url http://autre-serveur:8080/client-management-soap customer get 1
```

### 7.3 Codes de sortie

| Code | Signification |
|---|---|
| `0` | Succès |
| `1` | Demande refusée par le serveur (introuvable, doublon, données invalides) |
| `2` | Erreur d'utilisation (commande ou option inconnue, argument manquant) |
| `3` | Serveur injoignable, délai dépassé ou réponse inexploitable |
| `4` | Erreur inattendue (un bug) |

Les codes permettent d'utiliser l'outil dans des scripts (`if ! $CLI customer get 5; then ...`). Les messages
d'erreur sont écrits sur la sortie d'erreur, les résultats sur la sortie standard.

### 7.4 Fonctionnement interne

- Les **stubs JAX-WS** (package `...cli.soap.generated`) sont générés à partir des WSDL embarqués dans le JAR
  (`src/main/resources/wsdl`). Le programme n'a donc pas besoin de joindre le serveur pour les lire.
- Trois adaptateurs (`SoapCustomerService`, `SoapGroupService`, `SoapMembershipService`) implémentent les
  **interfaces de service du domaine** en appelant le SOAP : le moteur de commandes ne voit aucune différence
  avec un service local (patron *proxy distant*).
- Les fautes SOAP sont retraduites en exceptions du domaine (`FaultTranslator`) ; les pannes réseau deviennent
  « serveur indisponible » (code de sortie 3) avec un message lisible (« connection refused », « timed out »...).
- Délais : 5 s pour la connexion, 60 s pour la réponse.

### 7.5 Régénérer les stubs

Nécessaire seulement si le contrat SOAP change. Les stubs sont déjà versionnés dans le dépôt : un simple
`mvn clean verify` suffit pour construire le client.

Pour régénérer :

1. Construire et déployer le WAR SOAP à jour, puis démarrer WildFly (le serveur doit tourner : l'option
   `--fetch` télécharge les WSDL depuis lui). Contrôle : la commande suivante doit afficher `200`.
   ```bash
   curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/client-management-soap/CustomerWebService?wsdl"
   ```
2. Dans un terminal ouvert à la racine du dépôt, indiquer où est WildFly (la variable ne vaut que pour ce terminal) :
   ```bash
   export WILDFLY_HOME=/chemin/vers/wildfly-41.0.1.Final
   ls $WILDFLY_HOME/bin/wsconsume.sh        # doit afficher le fichier
   ```
3. Lancer le script (une seule ligne) :
   ```bash
   chmod +x scripts/generate-soap-stubs.sh
   scripts/generate-soap-stubs.sh client-management-soap-cli com.romanscad.clientmanagement.cli.soap.generated --fetch
   ```
4. Vérifier : `ls client-management-soap-cli/src/main/java/com/romanscad/clientmanagement/cli/soap/generated | wc -l`
   doit afficher environ 43 ; sous Eclipse, *Refresh* puis *Maven → Update Project*.

La passerelle REST utilise le même script, sans `--fetch` (les WSDL sont déjà copiés dans son dossier
`src/main/resources/wsdl`) :
```bash
scripts/generate-soap-stubs.sh client-management-rest com.romanscad.clientmanagement.rest.soap.generated
```

---

## 8. Application 3 – Passerelle REST

### 8.1 Déploiement

```bash
cp client-management-rest/target/client-management-rest.war $WILDFLY_HOME/standalone/deployments/
```

La passerelle s'appelle sur `http://localhost:8080/client-management-rest/api`. Elle appelle le serveur SOAP, qui
doit être déployé sur le même serveur ou sur un autre. Le déploiement réussit même si le SOAP n'est pas encore
disponible : la connexion est établie au premier appel (voir §11.12).

### 8.2 Configuration

| Réglage | Propriété système | Variable d'environnement | Défaut |
|---|---|---|---|
| Adresse de l'application SOAP | `clientmanagement.soap.url` | `CLIENTMANAGEMENT_SOAP_URL` | `http://localhost:8080/client-management-soap` |

La propriété système l'emporte sur la variable d'environnement. Exemple :
```bash
$WILDFLY_HOME/bin/standalone.sh -Dclientmanagement.soap.url=http://serveur-soap:8080/client-management-soap
```

### 8.3 Contrat REST

Base : `/client-management-rest/api`. Toutes les réponses et tous les corps sont en JSON.

| Méthode et chemin | Rôle | Réponses |
|---|---|---|
| `POST /customers` | créer un client | `201` + en-tête `Location` ; `400` |
| `GET /customers/{id}` | lire un client | `200` ; `404` |
| `DELETE /customers/{id}` | supprimer un client | `204` ; `404` |
| `GET /customers?after=&limit=` | page de clients | `200` ; `400` |
| `GET /customers/with-groups?after=&limit=` | clients avec leurs groupes | `200` ; `400` |
| `GET /customers/{id}/groups?after=&limit=` | groupes d'un client | `200` ; `404` |
| `POST /groups` | créer un groupe | `201` + `Location` ; `400` ; `409` (nom déjà pris) |
| `GET /groups/{id}` | lire un groupe | `200` ; `404` |
| `DELETE /groups/{id}` | supprimer un groupe | `204` ; `404` |
| `GET /groups?after=&limit=` | page de groupes | `200` ; `400` |
| `GET /groups/{id}/customers?after=&limit=` | clients d'un groupe | `200` ; `404` |
| `POST /groups/{id}/customers` | associer des clients | `200` `{"count":n}` ; `400` ; `404` |
| `POST /groups/{id}/customers/removal` | dissocier des clients | `200` `{"count":n}` ; `400` ; `404` |

Corps des requêtes :
```json
POST /customers                 {"firstName": "Ada", "lastName": "Lovelace", "email": "ada@example.com"}
POST /groups                    {"name": "VIP", "description": "facultative"}
POST /groups/{id}/customers     {"customerIds": [1, 2, 3]}
```

Une page de résultats :
```json
{ "items": [ {"id": 4, "firstName": "...", "lastName": "...", "email": "..."} ], "nextCursor": 4 }
```
`nextCursor` est absent sur la dernière page ; sinon il se repasse dans `after`.

### 8.4 Format des erreurs

Toutes les erreurs suivent la **RFC 9457** (`Content-Type: application/problem+json`) :
```json
{ "type": "about:blank", "title": "Not Found", "status": 404,
  "detail": "Customer 99999999 not found", "code": "CUSTOMER_NOT_FOUND", "resourceId": 99999999 }
```

| Situation | Statut | `code` |
|---|---|---|
| Client ou groupe inexistant | `404` | `CUSTOMER_NOT_FOUND`, `GROUP_NOT_FOUND` |
| Nom de groupe déjà pris | `409` | `DUPLICATE_GROUP_NAME` |
| Données invalides, limite hors bornes, corps absent | `400` | `INVALID_REQUEST` |
| Serveur SOAP injoignable | `503` | `SOAP_SERVICE_UNAVAILABLE` |
| Serveur SOAP trop lent | `504` | `SOAP_SERVICE_TIMEOUT` |
| Faute SOAP imprévue | `502` | `SOAP_SERVICE_ERROR` |
| Bug de la passerelle | `500` | `INTERNAL_ERROR` |

### 8.5 Exemples

```bash
B=http://localhost:8080/client-management-rest/api

curl -s $B/customers/500
curl -s "$B/customers?limit=3"
curl -i -X POST $B/groups -H 'Content-Type: application/json' -d '{"name":"Demo REST","description":"cree en REST"}'
curl -s -X POST $B/groups/21/customers -H 'Content-Type: application/json' -d '{"customerIds":[1,2,3]}'
curl -s "$B/groups/21/customers?limit=10"
curl -s $B/customers/1/groups
curl -s "$B/customers/with-groups?limit=2"
curl -i $B/customers/99999999          # 404 problem+json
curl -i "$B/customers?limit=5000"      # 400 : limite supérieure à 1000
```

---

## 9. Application 4 – Client REST en ligne de commande

### 9.1 Lancement

```bash
java -jar client-management-rest-cli/target/client-management-rest-cli.jar [--url <adresse>] <commande> [arguments]
```

- `--url` : adresse de **l'API REST**, placée avant la commande. Défaut :
  `http://localhost:8080/client-management-rest/api`.
- Les commandes, les options, les codes de sortie et l'affichage sont **identiques** à ceux du client SOAP
  ([§7](#7-application-2--client-soap-en-ligne-de-commande)) : c'est le même moteur.

```bash
CLI="java -jar client-management-rest-cli/target/client-management-rest-cli.jar"
$CLI help
$CLI customer get 500
$CLI group create "Demo REST" --description "Créé par le client REST"
$CLI group add 21 1 2 3
$CLI overview --limit 2
```

### 9.2 Fonctionnement interne

- **Aucune bibliothèque de client REST** : le transport est le `java.net.http.HttpClient` du JDK (HTTP/1.1),
  le JSON est lu et écrit avec **JSON-P** (`jakarta.json`, implémentation Eclipse Parsson). Le JAR reste petit
  et sans framework.
- Trois adaptateurs (`RestCustomerService`, `RestGroupService`, `RestMembershipService`) implémentent les mêmes
  interfaces de service du domaine que le client SOAP (patron *proxy distant*).
- Les réponses d'erreur `problem+json` sont retraduites en exceptions du domaine à partir du champ `code` (et de
  `resourceId` pour les « introuvable ») : les messages et les codes de sortie sont donc ceux du client SOAP.
- Les statuts `502`, `503` et `504` de la passerelle, et les pannes réseau, deviennent « serveur indisponible »
  (code de sortie 3).
- Délais : 5 s pour la connexion, 60 s pour la réponse.

---

## 10. Hypothèses

1. **Identifiants** : numériques, attribués par le serveur (séquence croissante à partir de 1) ; un identifiant
   n'est jamais réutilisé après une suppression.
2. **Clients** : prénom, nom et e-mail obligatoires (100, 100 et 254 caractères au plus) ; l'e-mail est normalisé en
   minuscules et le contrôle de forme est volontairement minimal (un seul `@` entouré de texte).
3. **Groupes** : le nom est obligatoire (100 caractères au plus) et **unique, sans tenir compte de la casse ni des
   espaces de bordure** ; la description est facultative (500 caractères au plus).
4. **Volumétrie** : plus d'un million de clients, mais un nombre de groupes bien plus faible (20 dans le jeu de
   données). Un client appartient donc à un nombre limité de groupes, ce qui permet d'inclure tous ses groupes dans la
   vue « clients avec leurs groupes » ; c'est la liste des *clients* qui est paginée. En revanche un groupe peut
   contenir des centaines de milliers de clients : ses membres ne sont **jamais** renvoyés d'un bloc.
5. **Toute liste est paginée** : l'énoncé impose plus d'un million de clients ; aucune réponse ne doit contenir
   toute la base. Taille de page : 100 par défaut, 1000 au maximum.
6. **Données non persistantes** : la base est en mémoire, recréée à chaque démarrage de l'application SOAP
   (c'est l'énoncé) ; les modifications sont perdues à l'arrêt.
7. **Un seul serveur** : pas de répartition de charge, donc pas de cohérence distribuée à gérer.
8. **Pas d'authentification** : hors du périmètre de l'exercice ; voir §15.
9. **Association** : un client peut être associé plusieurs fois au même groupe sans erreur (idempotence) ; les
   opérations en masse sont atomiques et limitées à 1000 clients par appel.
10. **Les clients en ligne de commande** sont des outils d'exploitation et de démonstration : sortie texte lisible
    sur la sortie standard, erreurs sur la sortie d'erreur, codes de sortie utilisables dans des scripts.

---

## 11. Choix techniques et justifications

### 11.1 Structures de données (cœur en mémoire)

| Besoin | Choix | Pourquoi |
|---|---|---|
| Clients et groupes par identifiant | `ConcurrentSkipListMap<Long, …>` | Tri naturel par identifiant, accès en O(log n), sûre pour les accès concurrents, et surtout elle permet la **pagination par curseur** : `tailMap(afterId, false)` se positionne directement après le curseur sans parcourir ce qui précède. Une `ConcurrentHashMap` serait plus rapide en accès unitaire mais ne sait pas ordonner ni se positionner : la pagination imposerait un tri complet (O(n log n)) à chaque page. Le surcoût de la skip list (quelques dizaines de nanosecondes par accès) est négligeable face à la latence réseau d'un appel SOAP. |
| Retrouver un groupe par son nom | `ConcurrentHashMap<String, Long>` (nom normalisé → identifiant) | Test d'unicité en O(1) ; ici l'ordre n'a pas d'importance. |
| Appartenances | **Deux** index inverses : groupe → ensemble de clients, client → ensemble de groupes (`ConcurrentSkipListSet`) | Les deux questions de l'énoncé (« clients d'un groupe », « groupes d'un client ») sont aussi rapides l'une que l'autre, et chaque ensemble est trié donc paginable par curseur. Le coût est une écriture dans deux index, rendue atomique par le verrou du service. |
| Compteurs | `LongAdder` | `size()` d'une skip list est en O(n) : on tient un compteur à part, peu contendu sous charge concurrente. |

### 11.2 Concurrence : verrou lecture/écriture au niveau du service

Le service métier utilise un `ReentrantReadWriteLock` : les **lectures** (listes, consultations) s'exécutent en
parallèle ; les **écritures** (création, suppression, association) sont exclusives. Pourquoi un verrou en plus
des structures concurrentes ? Une opération comme « supprimer un client » touche plusieurs structures (le client,
ses appartenances dans les deux index) : sans verrou, une lecture concurrente pourrait voir un état à moitié
modifié, et l'atomicité de « associer 500 clients, ou aucun » serait impossible. Les structures `Concurrent*` assurent
la sûreté mémoire ; le verrou assure la cohérence métier.

### 11.3 Pagination par curseur (et non par numéro de page)

Une pagination par décalage (« page 3 ») oblige à parcourir tous les éléments précédents (coût O(décalage)) et donne
des résultats instables si des données changent entre deux appels. Le curseur (« après l'identifiant X ») se positionne en
O(log n) et reste stable. Le serveur détecte la dernière page en demandant s'il existe un élément après la page,
sans compter la base.

### 11.4 Architecture en couches et principes SOLID

- **Dépôts (ports)** : interfaces `CustomerRepository`, `GroupRepository`, `MembershipRepository` ; le service ne
  connaît que ces interfaces (inversion des dépendances) : on pourrait passer à une base SQL sans toucher au métier.
- **Services** : interfaces fines (une par responsabilité) implémentées par `DefaultClientManagementService`.
- **Endpoints** : traduisent SOAP ↔ domaine et rien d'autre.
- **Records pour le domaine** : immuables, validés à la construction (impossible d'obtenir un client invalide),
  `equals`/`hashCode` corrects sans code répétitif.
- **Exceptions** : hiérarchie *scellée* (`sealed`) : le compilateur vérifie que tous les cas sont traités dans les
  `switch` de traduction d'erreurs, **sans `default`** : l'ajout d'un nouveau cas d'erreur casse la compilation
  tant qu'il n'est pas traité partout.

### 11.5 Trois endpoints SOAP plutôt qu'un seul

Un endpoint par domaine (clients, groupes, appartenances) : contrats courts et cohérents, responsabilités claires,
un WSDL lisible par service, et évolution indépendante. Un endpoint unique à treize opérations aurait mélangé tout.

### 11.6 Contrat SOAP : DTO JAXB, espace de noms et fautes

- Les DTO sont des classes JAXB ordinaires (JAXB ne sait pas construire des `record`), séparées du domaine : le
  contrat externe ne change pas quand le modèle interne évolue.
- Espace de noms **versionné** (`.../v1`), appliqué à *tous* les éléments (`@XmlSchema` + `targetNamespace` explicite
  sur chaque `@WebParam`/`@WebResult`) : sans cela, le WSDL publié et les messages réellement échangés n'étaient
  pas d'accord et les clients générés échouaient à la désérialisation.
- **Une seule faute** `ClientManagementFault` avec un code d'erreur : un client écrit un seul `catch` et un `switch`
  sur le code, plutôt que quatre types de fautes à générer et à maintenir.

### 11.7 Moteur de ligne de commande partagé

- Les deux clients sont deux **proxies distants** (SOAP, REST) implémentant les mêmes interfaces de service : le moteur
  est écrit une fois, testé sans aucun serveur (avec des services factices), et le second client n'a coûté que des
  adaptateurs.
- **Analyse des arguments écrite à la main** (pas de bibliothèque) : le besoin est petit (options à valeur, drapeaux,
  arguments positionnels), l'énoncé interdit les frameworks, et le code reste entièrement lisible et testé (15 tests).
  Une bibliothèque se justifierait pour une dizaine de sous-commandes complexes, pas ici.
- Aide **générée** depuis la description des commandes : impossible qu'elle diverge du comportement.
- Codes de sortie distincts (§7.3) : un script peut distinguer « refusé par le serveur » de « serveur injoignable ».

### 11.8 Client SOAP

- **Stubs générés avec l'outil standard de WildFly (`wsconsume`) et versionnés** : le build est reproductible sans
  serveur en marche. Contrepartie : en cas de changement du contrat, il faut régénérer (script fourni).
- **Ports paresseux** : lire un WSDL et initialiser CXF prend du temps ; `help` et les erreurs de syntaxe restent
  instantanés.
- **JAR autonome (`maven-shade-plugin`)** : `java -jar` suffit. Deux transformations sont nécessaires pour que CXF
  fonctionne une fois les JAR fusionnés : les fichiers `META-INF/services` et `META-INF/cxf/bus-extensions.txt`.
- Les API JAX-WS/JAXB sont déclarées en `compile` dans ce module (elles sont `provided` dans le parent, car WildFly
  les fournit aux WAR ; un programme autonome n'a pas de serveur).
- Délais explicites : sans eux un serveur qui ne répond pas bloquerait le programme indéfiniment.

### 11.9 Initialisation des données synchrone au démarrage

La génération est déclenchée par l'événement CDI `@Initialized(ApplicationScoped.class)` et s'exécute **avant** que le
WAR soit déclaré prêt : aucune requête ne peut arriver sur une base à moitié remplie. Elle est **déterministe**
(`SplittableRandom` avec graine fixe 42) : mêmes données à chaque démarrage, donc des essais reproductibles. La
génération passe par les interfaces de service, comme le ferait un client : elle ne contourne ni validation ni verrou.

### 11.10 Passerelle REST

- **Aucune dépendance sur `core`**, uniquement des stubs SOAP : la couche REST est réellement « au-dessus » du SOAP.
- **Trois couches** : ressources (HTTP uniquement : chemins, statuts, en-têtes) → passerelles (appel SOAP) → stubs. La
  traduction des erreurs est localisée dans une seule classe.
- **Objets de réponse REST distincts des types SOAP** : le contrat REST reste stable si le WSDL évolue.
- **Statuts HTTP** : `404`/`409`/`400` pour les erreurs de l'appelant ; `502`/`503`/`504` pour les problèmes en
  amont (SOAP injoignable, trop lent, réponse imprévue) : un client REST distingue ainsi « ma requête est mauvaise »
  de « la passerelle n'arrive pas à joindre son serveur ». Les détails techniques (adresses, exceptions) restent dans le
  journal et ne sont pas renvoyés au client.
- **Erreurs au format `application/problem+json` (RFC 9457)** : standard, lisible par un humain, exploitable par un
  programme grâce au champ `code` (et `resourceId` pour les « introuvable »). Un filet de sécurité transforme aussi toute
  exception imprévue en `500` du même format, sans jamais exposer de trace d'exécution.
- **Création** : `201` + `Location` ; **suppression** : `204` ; **dissociation en masse** : `POST …/customers/removal`
  et non `DELETE` avec corps, car beaucoup de clients HTTP et de proxys ignorent ou refusent le corps d'un `DELETE`.
- **Pagination par curseur conservée** de bout en bout (`after`/`limit`/`nextCursor`) : mêmes performances sur
  1 million de clients.

### 11.11 Client REST

- `java.net.http.HttpClient` (JDK) et JSON-P : zéro framework, JAR léger ; HTTP/1.1 forcé, car la négociation
  automatique vers HTTP/2 en clair pose des problèmes avec certains serveurs pour les requêtes avec corps.
- Les erreurs sont retraduites en exceptions du domaine d'après le champ `code` du `problem+json`, **jamais d'après le
  texte du message** (un texte peut changer, un code est un contrat).

### 11.12 Ports SOAP de la passerelle

Un port par service, créé à la demande puis conservé : le créer est coûteux (lecture du WSDL, construction du client).
Les ports d'Apache CXF supportent les appels concurrents tant que leur configuration n'est plus modifiée après la
création. Créés à la demande, ils permettent de déployer la passerelle avant le serveur SOAP.

### 11.13 Choix de build

- **Maven multi-modules** : versions centralisées dans le parent, build reproductible, un seul `mvn clean verify`.
- `maven.compiler.release=21` (et non `source`/`target`) : garantit l'usage de l'API de Java 21.
- **Pas de Spring ni équivalent** : CDI (injection et événement de démarrage), JAX-WS, JAX-RS et JSON viennent de WildFly.
  Les injections sont faites **par champ** dans les beans à portée applicative : CDI crée un proxy qui exige un
  constructeur sans argument.

---

## 12. Données de test, mémoire et mesures

Jeu de données par défaut (configurable, voir §6.2) :

| | |
|---|---|
| Clients | 1 000 000 |
| Groupes | 20 |
| Appartenances | 1 441 221 (soit ~1,4 groupe par client en moyenne) |
| Graine | 42 |

Forme des données : le groupe n° *k* contient environ 40 % / *k* des clients (loi de type Zipf) : un groupe énorme
(~400 000 clients) puis des groupes de plus en plus petits, ce qui exerce la pagination sur de gros ensembles.

**Mesures** (poste de développement, `SeedBenchmark` du module `core`) : génération de 1 000 000 de clients et de
leurs appartenances en **≈ 2,8 s** ; mémoire retenue **≈ 450 Mo**.

**Conséquence sur WildFly** : le tas par défaut du serveur (512 Mo) est **insuffisant** (`OutOfMemoryError` au
démarrage de l'application SOAP). Démarrer WildFly avec :

```bash
JBOSS_JAVA_SIZING="-Xms512m -Xmx2g" $WILDFLY_HOME/bin/standalone.sh
```

2 Go laissent de la marge pour le serveur d'applications lui-même, les pages de résultats et le ramasse-miettes.
Pour des essais rapides : `-Dclientmanagement.seed.customers=20000` (le tas par défaut suffit alors).

---

## 13. Gestion des erreurs

Chaîne complète d'une erreur « client introuvable » :

```
Dépôt/service : CustomerNotFoundException(500)
   └─ Endpoint SOAP : ClientManagementFault { code = CUSTOMER_NOT_FOUND, resourceId = 500 }
        ├─ Client SOAP  : retraduit en CustomerNotFoundException → « Error: Customer 500 not found », sortie 1
        └─ Passerelle REST : HTTP 404 problem+json { code: CUSTOMER_NOT_FOUND, resourceId: 500 }
             └─ Client REST : retraduit en CustomerNotFoundException → même message, sortie 1
```

Les pannes de transport suivent un chemin parallèle : SOAP injoignable → `503` à la passerelle → « serveur
indisponible », sortie 3 pour le client REST ; serveur SOAP injoignable depuis le client SOAP → sortie 3 directement.
Les messages destinés à l'utilisateur ne contiennent jamais de trace d'exécution.

---

## 14. Tests et vérification

**Tests automatisés** (`mvn clean verify`) : le noyau (`core` : dépôts, service, pagination, générateur de données),
la couche SOAP (traduction des fautes, DTO, initialisation, endpoint des appartenances) et le moteur de ligne de
commande (`cli-common` : analyse des arguments, tableaux, scénarios de bout en bout sur des services factices).

**Non couverts par des tests automatisés** (par manque de temps, voir §15) : les adaptateurs du client SOAP, la passerelle REST
et le client REST. Ils se vérifient par le scénario manuel suivant, serveur démarré :

| # | Commande | Résultat attendu |
|---|---|---|
| 1 | `$SOAP customer get 500` | le client 500, sortie 0 |
| 2 | `$REST customer get 500` | le même client, sortie 0 |
| 3 | `$SOAP customer get 99999999` / `$REST customer get 99999999` | `Customer 99999999 not found`, sortie 1 |
| 4 | `$SOAP group create "Demo"` puis `$REST group create "demo"` | la seconde échoue : nom déjà pris, sortie 1 |
| 5 | `$REST group add <id> 1 2 3` puis `$SOAP group customers <id>` | les clients 1, 2, 3 (le REST écrit via le SOAP) |
| 6 | `$SOAP customer list --limit 5000` | refus : limite hors bornes, sortie 1 |
| 7 | `$SOAP --url http://localhost:1/x customer get 1` | message « Cannot reach… connection refused », sortie 3 |
| 8 | `curl -i $B/customers/99999999` | `404` `application/problem+json` |
| 9 | arrêter le WAR SOAP puis `curl -i $B/customers/1` | `503` `application/problem+json` |

(`$SOAP` et `$REST` désignent les commandes `java -jar …` des §7 et §9.)

---

## 15. Limites connues et pistes d'amélioration

- **Pas de persistance** : la base est volatile par exigence de l'énoncé. Les dépôts étant des interfaces, une base
  relationnelle remplacerait l'implémentation en mémoire sans toucher au service.
- **Une seule JVM** : pas de mise à l'échelle horizontale ; il faudrait externaliser l'état.
- **Pas d'authentification, d'autorisation ni de TLS** : hors périmètre ; à ajouter avant toute exposition réelle
  (WS-Security ou jeton pour le SOAP, OAuth2 pour le REST).
- **Mémoire** : ≈ 450 Mo pour 1 million de clients. Au-delà de quelques millions il faudrait un stockage externe.
- **Écritures sérialisées** par le verrou global du service : suffisant pour l'exercice, mais un verrou par groupe
  pourrait augmenter le débit en écriture si besoin.
- **Association limitée à 1000 clients par appel** (protection du serveur) ; pour davantage il faut plusieurs
  appels (chacun reste atomique, pas l'ensemble).
- **Latence cumulée** de la passerelle REST (REST → SOAP → mémoire) : prix de l'abstraction demandée.
- **Tests** : pas de tests automatisés sur les adaptateurs du client SOAP, la passerelle REST et le client REST ;
  des tests d'intégration seraient la prochaine étape (par exemple avec Arquillian ou un serveur embarqué).
- **Affichage** : sur un terminal dont la locale n'est pas UTF-8, les accents des arguments et des résultats peuvent
  apparaître sous forme de `?` (voir §16).

---

## 16. Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| `OutOfMemoryError` au déploiement du WAR SOAP | Tas par défaut (512 Mo) trop petit pour 1 M de clients | Démarrer avec `JBOSS_JAVA_SIZING="-Xms512m -Xmx2g"` (§12) |
| `Cannot reach the SOAP service … connection refused` (client SOAP, sortie 3) | WildFly arrêté, WAR non déployé, mauvaise adresse | Vérifier que le WSDL s'ouvre dans un navigateur ; utiliser `--url` |
| `Cannot reach the REST gateway … connection refused` (client REST) | Idem pour la passerelle | Vérifier `http://localhost:8080/client-management-rest/api/customers?limit=1` |
| Passerelle : `503 SOAP_SERVICE_UNAVAILABLE` | Le WAR SOAP n'est pas (encore) déployé ou ne répond pas à l'adresse configurée | Déployer le SOAP ; régler `clientmanagement.soap.url` si le port ou l'hôte diffère |
| Le client SOAP échoue avec `Unmarshalling Error: unexpected element` | Stubs générés à partir d'un ancien WSDL | Redéployer le SOAP à jour puis régénérer les stubs (§7.5) |
| `Unexpected response from the server: HTTP 404` (client REST) | `--url` ne pointe pas sur `/client-management-rest/api` | Corriger `--url` |
| Les accents s'affichent en `?` | Locale du terminal non UTF-8 | `export LC_ALL=C.UTF-8` (ou `-Dstdout.encoding=UTF-8` côté Java) |
| `mvn clean verify` : « JUnit cannot be resolved » dans Eclipse | Projet Eclipse non synchronisé | Clic droit sur le projet → Maven → *Update Project* |
| Déploiement lent | Génération de 1 M de clients synchrone au démarrage (quelques secondes) | Normal ; réduire `clientmanagement.seed.customers` pour développer |