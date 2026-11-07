# Documentation fonctionnelle — Ressemblance

## Présentation

Ressemblance est une application de bureau Java (interface Swing) qui recherche des fichiers susceptibles d’être des doublons ou de porter des noms proches. Elle permet de comparer des répertoires, de consulter les paires détectées et de gérer les fichiers depuis les résultats. Un mode de surveillance peut également analyser périodiquement un répertoire et supprimer automatiquement certains fichiers qui s’y trouvent.

## Comparaison de fichiers

### Choisir les répertoires et les règles

La fenêtre principale présente trois zones éditables :

- **File** : liste des répertoires racines à analyser, un chemin par ligne. Le bouton **Add file** ouvre en pratique un sélecteur de répertoire.
- **Separator** : séparateurs utilisés pour découper les noms de fichiers en mots.
- **Exclude** : mots à ignorer lors de la comparaison des noms.

Les sous-répertoires des racines sont parcourus récursivement. Seuls les fichiers sont comparés ; un chemin qui n’est pas un répertoire n’ajoute pas de fichiers à l’analyse.

Par défaut, les séparateurs comprennent le point, le souligné, le tiret, les espaces et la virgule. Les mots exclus par défaut incluent notamment des variantes de « James Bond » et des extensions vidéo courantes. Les noms sont convertis en majuscules avant comparaison.

### Lancer une analyse

L’action **Run** compare chaque paire de fichiers trouvée dans les répertoires configurés. Les résultats sont triés du score le plus élevé au plus faible et affichés dans une fenêtre sous forme de tableau : les deux fichiers comparés et leur niveau de ressemblance. Aucun tableau n’est ouvert lorsqu’aucune paire n’est retenue.

Le score combine deux signaux :

1. **Contenu identique** : lorsque deux fichiers ont la même taille et le même empreinte SHA-1, la paire reçoit le score spécial **9999**.
2. **Nom et taille** : sinon, chaque mot du nom du premier fichier présent dans celui du second ajoute 10 points. Un petit bonus (de 0 à 9 points) favorise les tailles proches.

Le score reflète donc principalement la similarité des noms ; le contenu n’est vérifié que pour reconnaître des fichiers identiques de même taille. Ce mécanisme ne constitue pas une comparaison sémantique du contenu.

### Actions sur les résultats

Le menu contextuel du tableau propose :

- **Run** : ouvrir le fichier sélectionné avec l’application associée au système.
- **Open Directory** : ouvrir le répertoire parent du fichier.
- **Isolate** : placer temporairement des liens vers les deux fichiers dans un répertoire de travail afin de les examiner, puis choisir éventuellement lequel supprimer.
- **Delete A** ou **Delete B** : supprimer, après confirmation, le fichier correspondant pour chacune des lignes sélectionnées.

Les suppressions effectuées depuis ces actions sont directes. Elles ne passent pas par la corbeille et ne sont pas annulables par l’application.

## Gestion des configurations

Les actions du menu **File** permettent de créer une configuration (**New**), d’en ouvrir une (**Open**), de l’enregistrer (**Save**) ou de l’enregistrer sous un autre fichier (**Save as**). Une configuration contient les racines, séparateurs et mots exclus. Elle est enregistrée sous forme d’objet Java sérialisé ; ce n’est pas un fichier de configuration texte destiné à être modifié manuellement.

Les derniers emplacements utilisés pour sélectionner des répertoires et des fichiers sont mémorisés dans les préférences utilisateur.

## Surveillance périodique

L’action **Monitoring start** ouvre une boîte de dialogue permettant de définir :

- le répertoire racine à surveiller ;
- un seuil numérique (**Offset**) à partir duquel une paire est considérée pour le traitement.

Le répertoire doit exister et le seuil doit être numérique. La valeur initiale proposée pour le seuil est **9999**, soit le niveau attribué aux fichiers ayant même taille et même empreinte.

Une fois démarrée, la surveillance relance une analyse après un délai initial d’environ 2 secondes, puis toutes les 5 minutes. Chaque analyse combine le répertoire surveillé avec les racines de la configuration courante. Pour les paires dont le score atteint le seuil, si l’un des fichiers se trouve dans le répertoire surveillé et que les deux fichiers existent, le fichier surveillé est supprimé.

Avant la suppression, l’application tente de créer dans le répertoire du fichier un fichier marqueur nommé `__deleted-file__<nom-du-fichier>.txt`. Ce marqueur indique le chemin de l’autre fichier et la date de suppression. Les événements et erreurs de surveillance sont visibles dans une console dédiée. **La surveillance peut supprimer des fichiers sans confirmation interactive** : vérifier le répertoire et le seuil avant de la démarrer.

**Monitoring stop** arrête les analyses périodiques et ferme la console de suivi.

## Résumé des précautions

- Vérifier soigneusement les répertoires et les règles avant une analyse.
- Examiner les résultats avant d’utiliser **Delete A**, **Delete B** ou **Isolate**.
- Utiliser la surveillance avec prudence : elle peut supprimer automatiquement un fichier présent dans le répertoire surveillé.
- Les fichiers supprimés ne sont pas récupérables via l’application ; le fichier marqueur est une trace, pas une sauvegarde.
