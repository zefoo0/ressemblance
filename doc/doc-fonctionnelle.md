# Documentation fonctionnelle — Ressemblance

## Présentation

Ressemblance est une application Java Swing destinée à repérer des fichiers dupliqués, quasi dupliqués ou visuellement proches dans un ou plusieurs répertoires. Elle compare récursivement les fichiers d’un ensemble de dossiers racines, affiche les paires les plus probables et propose des actions sur les résultats (ouverture, suppression, analyse détaillée). Un mode de surveillance permet aussi de supprimer automatiquement des fichiers dans un dossier surveillé lorsqu’ils correspondent à des doublons selon un seuil défini.

## Interface principale

La fenêtre principale expose deux volets :

- un panneau de configuration, intitulé `Paramètres`,
- un panneau de résultats, qui affiche la liste des paires comparées lorsque l’analyse est lancée.

Dans le panneau de configuration, l’utilisateur renseigne :

- `Dossiers racine` : un ou plusieurs chemins de répertoire, un chemin par ligne ; les sous-répertoires sont parcourus récursivement ;
- une table `Comparateurs` avec les colonnes `Comparateur`, `Extensions` et `Coefficient` ;
- le bouton `Add Directory` pour choisir un dossier et l’ajouter à la liste.

Les comparateurs sont paramétrables individuellement. Le coefficient permet de pondérer le résultat d’un comparateur dans le score final. Le filtrage par extension permet de n’appliquer un comparateur qu’à certains types de fichiers. Le joker `*` signifie : toutes les extensions.

## Comparateurs disponibles

La configuration par défaut contient quatre comparateurs :

1. `Fichier` : compare le contenu des fichiers à l’aide d’un hash SHA-1 ; si deux fichiers ont la même taille et la même empreinte, le score vaut `100`.
2. `Contenu image` : s’applique aux images JPEG, JPG, PNG, GIF, BMP, TIFF, TIF, WBMP ; utilise une comparaison visuelle basée sur des empreintes d’images.
3. `Contenu vidéo` : s’applique aux vidéos AVI, MOV, FLV, WMV, MP4, MPG ; extrait des fichiers vidéo des empreintes et les compare.
4. `Nom de fichier` : s’applique à tous les fichiers ; compare les noms sans extension avec la distance de Jaro-Winkler.

Le score retenu pour une paire est celui du meilleur comparateur qui a produit un résultat après application du coefficient. Les valeurs de similarité sont exprimées sur une échelle de `0` à `100`.

## Analyse des fichiers

L’action `Run` lance une comparaison entre toutes les paires de fichiers issues des répertoires racines. Les fichiers sont d’abord collectés récursivement, dédupliqués par chemin, puis triés. Chaque paire est comparée selon les comparateurs actifs et les extensions autorisées.

Le moteur de comparaison :

- ignore les chemins non répertoires ;
- traite les fichiers comme des paires uniques `A/B` sans redondance ;
- applique les comparateurs qui acceptent le type de fichier ;
- conserve uniquement le meilleur score pour chaque paire.

Les lignes de résultats sont ensuite triées par score décroissant. Si aucune paire ne donne un résultat pertinent, la table peut rester vide et aucun tableau n’est ouvert.

## Tableau des résultats

Le tableau affiche, pour chaque ligne, au moins :

- `Fichier 1` et `Fichier 2` : les deux chemins concernés ;
- le score de similarité (`similarité` ou colonne équivalente selon l’affichage) ;
- éventuellement des informations de diagnostic ou d’erreur de comparaison.

Le menu contextuel du tableau propose des actions sur les fichiers sélectionnés :

- `Open file 1` / `Open file 2` : ouvrir le fichier avec l’application système associée ;
- `Delete file 1` / `Delete file 2` : supprimer un ou plusieurs fichiers sélectionnés après confirmation ;
- `Open directory` / `Open directory 1` / `Open directory 2` : ouvrir le dossier parent du fichier ;
- `Isolate` : préparer une isolation des fichiers sélectionnés dans un dossier de travail pour comparaison manuelle ;
- `Copy cell` : copier la valeur de la cellule sélectionnée dans le presse-papiers ;
- `Afficher les erreurs` : afficher les détails d’erreurs survenues pendant la comparaison, si disponibles.

Les suppressions exécutées depuis l’interface sont définitives : elles ne passent pas par la corbeille et ne sont pas annulées dans l’application.

## Gestion des configurations

Le menu `Files` propose :

- `New` : créer une nouvelle configuration vide ;
- `Open` : ouvrir une configuration depuis un fichier JSON ;
- `Save` : enregistrer la configuration actuelle ;
- `Save as` : enregistrer sous un autre nom ;
- `Quit` : fermer l’application.

Une configuration contient les dossiers racines et les paramètres des comparateurs. Tant qu’elle n’a pas été enregistrée dans un fichier, ses valeurs peuvent rester conservées dans les préférences utilisateur et être restaurées au prochain démarrage. L’ouverture d’un fichier JSON remplace la configuration active, et l’enregistrement dans un fichier supprime la version de session en mémoire.

Les répertoires sélectionnés récemment sont mémorisés pour faciliter la reprise de l’utilisateur.

## Surveillance périodique

Le menu `Monitoring` propose :

- `Start` : démarrer la surveillance d’un répertoire donné ;
- `Stop` : arrêter la surveillance active.

La boîte de configuration de surveillance demande :

- le `répertoire à surveiller` ;
- un seuil numérique `Offset` : une paire n’est considérée que si son score atteint ou dépasse ce seuil.

La surveillance démarre après un délai initial d’environ 2 secondes, puis relance une analyse toutes les 5 minutes. L’analyse combine le répertoire surveillé avec les dossiers racines de la configuration actuelle. Pour chaque paire de fichiers ayant un score supérieur ou égal au seuil :

- si l’un des fichiers est dans le dossier surveillé et si les deux fichiers existent encore, le fichier du dossier surveillé est supprimé ;
- avant la suppression, l’application écrit un marqueur nommé `__deleted-file__<nom-du-fichier>.txt` dans le dossier du fichier supprimé ;
- le fichier marqueur contient le chemin du fichier correspondant et la date de suppression.

Les logs et événements de surveillance sont affichés dans une console dédiée. La surveillance peut supprimer des fichiers automatiquement sans confirmation interactive : il faut donc vérifier le dossier ciblé et le seuil avant de la lancer.

## Précautions d’utilisation

- Vérifier les dossiers racines et les comparateurs avant d’exécuter une analyse.
- Contrôler les résultats avant d’utiliser les actions de suppression ou d’isolation.
- Utiliser la surveillance avec prudence : le mode automatique peut effacer des fichiers sans validation de l’utilisateur.
- Les suppressions sont définitives et la trace laissée par le fichier marqueur ne constitue pas une restauration automatique.
