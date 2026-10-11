package com.musigma.models;

import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.RepresentationException;
import com.musigma.models.exception.StockException;
import com.musigma.models.exception.TypeTicketException;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Festival représente un festival avec un nom, une date de début, un prix de emplacement,
 * une superficie, un emplacement et des ressources, comme des artistes,
 * des types de tickets, de stocks et de représentations.
 */
public final class Festival implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * Nom de la scène créée avec un nouveau festival.
     */
    public static final String SCENE_PAR_DEFAUT = "Scène principale";

    /**
     * Logger pour afficher les logs.
     */
    private static final Logger LOGGER = getLogger(Festival.class);
    /**
     * Liste des artistes du festival.
     */
    private final ArrayList<Artiste> artistes;
    /**
     * Liste des types de tickets du festival.
     */
    private final ArrayList<TypeTicket> typesTicket;
    /**
     * Liste des stocks du festival.
     */
    private final ArrayList<Stock> stocks;
    /**
     * Liste des représentations du festival.
     */
    private final ArrayList<Representation> representations;
    /**
     * Scènes du festival, sur lesquelles se déroulent les représentations.
     */
    private final ArrayList<String> scenes;
    /**
     * Fichier associé à ce festival, renseigné au chargement ou à l'enregistrement.
     */
    private transient File fichier = null;
    /**
     * État sérialisé du festival lors du dernier enregistrement, pour détecter les modifications.
     */
    private transient byte[] etatEnregistre = null;
    /**
     * Nom du festival.
     */
    private String nom;
    /**
     * Date de début du festival.
     */
    private LocalDateTime debut;
    /**
     * Date de fin du festival (exclue).
     */
    private LocalDateTime fin;
    /**
     * Prix de emplacement du festival.
     */
    private float prixLocation;
    /**
     * Emplacement du festival.
     */
    private String emplacement;
    /**
     * Superficie du festival (m2)
     */
    private float superficie;

    /**
     * Constructeur de la classe Festival.
     *
     * @param nom          le nom du festival
     * @param debut         la date de début du festival ; le festival dure un jour et compte une scène par défaut
     * @param prixLocation le prix de emplacement du festival
     * @param superficie          la superficie du festival
     * @param emplacement      l'emplacement du festival
     * @throws FestivalException si les valeurs des paramètres ne sont pas valides
     */
    public Festival(String nom, LocalDateTime debut, float prixLocation, float superficie, String emplacement) throws FestivalException {
        LOGGER.fine("Initialize Festival");
        artistes = new ArrayList<>();
        typesTicket = new ArrayList<>();
        stocks = new ArrayList<>();
        representations = new ArrayList<>();
        scenes = new ArrayList<>(List.of(SCENE_PAR_DEFAUT));
        setNom(nom);
        setDebut(debut);
        fin = debut.plusDays(1);
        setPrixLocation(prixLocation);
        setSuperficie(superficie);
        setEmplacement(emplacement);
        LOGGER.fine("Created Festival");
    }

    /**
     * Méthode statique pour charger un festival depuis un fichier sérialisé.
     *
     * @param fichier le chemin du fichier à charger
     * @return le festival chargé depuis le fichier
     * @throws FestivalException si le fichier n'est pas défini
     */
    public static Festival charger(File fichier) throws FestivalException {
        try (
                FileInputStream fis = new FileInputStream(fichier.getAbsolutePath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            LOGGER.info(String.format("Reading fichier %s", fichier.getAbsolutePath()));
            Festival festival = (Festival) ois.readObject();
            festival.fichier = fichier;
            festival.marquerEnregistre();
            LOGGER.info(String.format("Loaded festival \"%s\" from fichier %s", festival.getNom(), fichier.getAbsolutePath()));
            return festival;
        } catch (IOException e) {
            throw new FestivalException("Le fichier du festival ne peut pas être lu");
        } catch (ClassNotFoundException e) {
            throw new FestivalException("Le fichier du festival est corrompue");
        }
    }

    /**
     * Méthode pour sauvegarder le festival dans un fichier sérialisé.
     *
     * @throws FestivalException si le fichier n'est pas défini
     */
    public void enregistrer() throws FestivalException {
        if (fichier == null)
            throw new FestivalException("Le fichier du festival n'a pas été spécifié");
        try (FileOutputStream fos = new FileOutputStream(fichier.getAbsolutePath())) {
            byte[] etat = serialiser();
            fos.write(etat);
            etatEnregistre = etat;
            LOGGER.info(String.format("Saved festival \"%s\" to fichier %s", nom, fichier.getAbsolutePath()));
        } catch (IOException e) {
            throw new FestivalException("Le festival n'arrive pas à enregister le fichier : " + e.getMessage());
        }
    }

    /**
     * Considère l'état actuel du festival comme enregistré.
     * Les modifications ultérieures seront signalées par {@link #estModifie()}.
     *
     * @throws IllegalStateException si le festival ne peut pas être sérialisé
     */
    public void marquerEnregistre() {
        etatEnregistre = serialiser();
    }

    /**
     * Indique si le festival a été modifié depuis le dernier enregistrement ou chargement,
     * en comparant son état sérialisé à celui enregistré.
     *
     * @return true si le festival a été modifié, false sinon
     */
    public boolean estModifie() {
        return etatEnregistre == null || !Arrays.equals(etatEnregistre, serialiser());
    }

    /**
     * Sérialise le festival et tout ce qu'il contient.
     *
     * @return le festival sérialisé
     */
    private byte[] serialiser() {
        ByteArrayOutputStream octets = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(octets)) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new IllegalStateException("Le festival ne peut pas être sérialisé", e);
        }
        return octets.toByteArray();
    }

    /**
     * Retourne le fichier associé à ce festival.
     *
     * @return le fichier du festival
     */
    public File getFichier() {
        return fichier;
    }

    /**
     * Définit le fichier associé à ce festival.
     * Ce chemin est utilisé pour la sauvegarde et le chargement du festival.
     *
     * @param fichier le nouveau fichier du festival
     * @throws FestivalException si le fichier est null
     */
    public void setFichier(File fichier) throws FestivalException {
        if (fichier == null)
            throw new FestivalException("Le fichier du festival est null, doit être défini");
        this.fichier = fichier;
        LOGGER.fine("Set Festival.fichier");
    }

    /**
     * Retourne le nom du festival.
     *
     * @return le nom du festival
     */
    public String getNom() {
        return nom;
    }

    /**
     * Définit le nom du festival.
     *
     * @param nom le nom du festival
     * @throws FestivalException si le nom est null ou vide
     */
    public void setNom(String nom) throws FestivalException {
        if (nom == null || nom.isEmpty())
            throw new FestivalException("Le nom est null ou vide, doit être défini");
        this.nom = nom;
        LOGGER.fine("Set Festival.nom");
    }

    /**
     * Retourne la date de début du festival.
     *
     * @return la date de début du festival
     */
    public LocalDateTime getDebut() {
        return debut;
    }

    /**
     * Définit la date de début du festival.
     *
     * @param debut la date de début du festival
     * La date de fin est décalée d'autant : le festival garde sa durée et ses représentations.
     *
     * @throws FestivalException si la date est null ou antérieure à la date actuelle
     */
    public void setDebut(LocalDateTime debut) throws FestivalException {
        if (debut == null)
            throw new FestivalException("La date de début est null, doit être définie");
        if (LocalDate.now().isAfter(debut.toLocalDate())) {
            throw new FestivalException("La date est antérieure à la date d'aujourd'hui, doit être aujourd'hui ou plus tard");
        }
        if (fin != null)
            fin = fin.plus(Duration.between(this.debut, debut));
        this.debut = debut;
        LOGGER.fine("Set Festival.debut");
    }

    /**
     * Retourne la date de fin du festival (exclue).
     *
     * @return la date de fin du festival
     */
    public LocalDateTime getFin() {
        return fin;
    }

    /**
     * Définit la date de fin du festival (exclue).
     *
     * @param fin la date de fin du festival
     * @throws FestivalException si la date est null, ne suit pas la date de début,
     *                           ou si des représentations se terminent après elle
     */
    public void setFin(LocalDateTime fin) throws FestivalException {
        if (fin == null)
            throw new FestivalException("La date de fin est null, doit être définie");
        if (!fin.isAfter(debut))
            throw new FestivalException("La date de fin doit suivre la date de début");
        long minutes = Duration.between(debut, fin).toMinutes();
        Optional<Representation> horsFestival = representations.stream()
                .filter(r -> r.getDecalageDebut() + r.getDuree() > minutes)
                .findFirst();
        if (horsFestival.isPresent())
            throw new FestivalException(String.format("La représentation %s se termine après cette date de fin", horsFestival.get()));
        this.fin = fin;
        LOGGER.fine("Set Festival.fin");
    }

    /**
     * Retourne la durée du festival en minutes.
     *
     * @return le nombre de minutes entre le début et la fin du festival
     */
    public int getDureeMinutes() {
        return (int) Duration.between(debut, fin).toMinutes();
    }

    /**
     * Retourne les scènes du festival.
     *
     * @return la liste des scènes, non modifiable
     */
    public List<String> getScenes() {
        return Collections.unmodifiableList(scenes);
    }

    /**
     * Ajoute une scène au festival.
     *
     * @param scene le nom de la scène
     * @throws FestivalException si le nom est vide ou déjà utilisé
     */
    public void ajouterScene(String scene) throws FestivalException {
        verifierNomScene(scene);
        scenes.add(scene.strip());
        LOGGER.fine("Added scene to Festival.scenes");
    }

    /**
     * Renomme une scène, ainsi que la scène de ses représentations.
     *
     * @param scene   le nom actuel de la scène
     * @param nouveauNom le nouveau nom
     * @throws FestivalException si la scène n'existe pas, ou si le nouveau nom est vide ou déjà utilisé
     */
    public void renommerScene(String scene, String nouveauNom) throws FestivalException {
        int index = scenes.indexOf(scene);
        if (index < 0)
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas", scene));
        verifierNomScene(nouveauNom);
        String nom = nouveauNom.strip();
        for (Representation representation : representations)
            if (representation.getScene().equals(scene))
                try {
                    representation.setScene(nom);
                } catch (RepresentationException e) {
                    throw new FestivalException(e.getMessage());
                }
        scenes.set(index, nom);
        LOGGER.info("Renamed scene of Festival.scenes");
    }

    /**
     * Supprime une scène sans représentation. Le festival garde toujours au moins une scène.
     *
     * @param scene le nom de la scène
     * @throws FestivalException si la scène n'existe pas, est la dernière, ou accueille des représentations
     */
    public void supprimerScene(String scene) throws FestivalException {
        if (!scenes.contains(scene))
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas", scene));
        if (scenes.size() == 1)
            throw new FestivalException("Le festival doit garder au moins une scène");
        long utilisations = representations.stream().filter(r -> r.getScene().equals(scene)).count();
        if (utilisations > 0)
            throw new FestivalException(String.format("La scène \"%s\" accueille %d représentation(s), à déplacer ou supprimer d'abord", scene, utilisations));
        scenes.remove(scene);
        LOGGER.fine("Removed scene from Festival.scenes");
    }

    /**
     * Vérifie qu'un nom de scène est valide et libre.
     *
     * @param scene le nom à vérifier
     * @throws FestivalException si le nom est vide ou déjà utilisé
     */
    private void verifierNomScene(String scene) throws FestivalException {
        if (scene == null || scene.isBlank())
            throw new FestivalException("Le nom de la scène est vide, doit être défini");
        if (scenes.stream().anyMatch(s -> s.equalsIgnoreCase(scene.strip())))
            throw new FestivalException(String.format("La scène \"%s\" existe déjà", scene.strip()));
    }

    /**
     * Retourne le temps encore libre dans le planning, toutes scènes confondues.
     *
     * @return le nombre de minutes de scène non occupées par une représentation
     */
    public long getMinutesLibres() {
        long occupee = representations.stream().mapToLong(Representation::getDuree).sum();
        return (long) scenes.size() * getDureeMinutes() - occupee;
    }

    /**
     * Retourne le prix de emplacement du festival.
     *
     * @return le prix de emplacement du festival
     */
    public float getPrixLocation() {
        return prixLocation;
    }

    /**
     * Définit le prix de emplacement du festival.
     *
     * @param prixLocation le prix de emplacement du festival
     * @throws FestivalException si le prix est négatif
     */
    public void setPrixLocation(float prixLocation) throws FestivalException {
        if (prixLocation < 0)
            throw new FestivalException("Le prix de emplacement est négatif, doit être positif");
        this.prixLocation = prixLocation;
        LOGGER.fine("Set Festival.prixLocation");
    }

    /**
     * Retourne la superficie du festival.
     *
     * @return la superficie du festival
     */
    public float getSuperficie() {
        return superficie;
    }

    /**
     * Définit la superficie du festival.
     *
     * @param superficie la superficie du festival
     * @throws FestivalException si la superficie est négative
     */
    public void setSuperficie(float superficie) throws FestivalException {
        if (superficie <= 0)
            throw new FestivalException("La superficie est négative ou nulle (0), doit être positive");
        this.superficie = superficie;
        LOGGER.fine("Set Festival.superficie");
    }

    /**
     * Retourne l'emplacement du festival.
     *
     * @return l'emplacement du festival
     */
    public String getEmplacement() {
        return emplacement;
    }

    /**
     * Définit l'emplacement du festival.
     *
     * @param emplacement l'emplacement du festival
     * @throws FestivalException si l'emplacement est null ou vide
     */
    public void setEmplacement(String emplacement) throws FestivalException {
        if (emplacement == null || emplacement.isEmpty())
            throw new FestivalException("L'emplacement de emplacement est null ou vide, doit être défini");
        this.emplacement = emplacement;
        LOGGER.fine("Set Festival.emplacement");
    }

    /**
     * Ajoute un artiste à la liste des artistes du festival.
     *
     * @param artiste l'artiste à ajouter
     * @throws FestivalException si l'artiste est null ou déjà engagé
     */
    public void ajouterArtiste(Artiste artiste) throws FestivalException {
        if (artiste == null)
            throw new FestivalException("L'artiste est null, doit être défini");
        if (artistes.contains(artiste))
            throw new FestivalException(String.format("L'artiste \"%s\" est déjà engagé", artiste.getNom()));
        artistes.add(artiste);
        LOGGER.fine("Added Artiste to Festival.artistes");
    }

    /**
     * Supprime un artiste du festival, ainsi que toutes ses représentations.
     *
     * @param artiste l'artiste à supprimer
     * @throws FestivalException si l'artiste n'a pas été trouvé
     */
    public void retirerArtiste(Artiste artiste) throws FestivalException {
        if (!artistes.contains(artiste))
            throw new FestivalException("L'artiste n'a pas été trouvé");
        for (Representation representation : new ArrayList<>(artiste.getRepresentations()))
            supprimerRepresentation(representation);
        artistes.remove(artiste);
        LOGGER.fine("Removed Artiste from Festival.artistes");
    }

    /**
     * Retourne la liste des artistes du festival.
     *
     * @return la liste des artistes, non modifiable
     */
    public List<Artiste> getArtistes() {
        return Collections.unmodifiableList(artistes);
    }

    /**
     * Indique si une scène est libre pendant un créneau.
     *
     * @param scene      la scène
     * @param decalageDebut le début du créneau en minutes depuis le début du festival
     * @param duree   la durée du créneau en minutes
     * @return true si aucune représentation n'occupe la scène pendant ce créneau
     */
    public boolean estSceneLibre(String scene, int decalageDebut, int duree) {
        return trouverCollision(scene, decalageDebut, duree, null) == null;
    }

    /**
     * Cherche une représentation occupant la scène pendant le créneau donné.
     *
     * @param scene      la scène
     * @param decalageDebut le début du créneau en minutes depuis le début du festival
     * @param duree   la durée du créneau en minutes
     * @param ignoree    une représentation à ne pas prendre en compte (celle qu'on déplace), ou null
     * @return la première représentation en collision, ou null s'il n'y en a pas
     */
    /**
     * Vérifie qu'un créneau est sur une scène du festival et pendant le festival.
     *
     * @param scene      la scène
     * @param decalageDebut le début du créneau en minutes depuis le début du festival
     * @param duree   la durée du créneau en minutes
     * @throws FestivalException si la scène n'existe pas ou si le créneau déborde du festival
     */
    private void verifierCreneau(String scene, int decalageDebut, int duree) throws FestivalException {
        if (!scenes.contains(scene))
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas dans le festival", scene));
        if (decalageDebut + duree > getDureeMinutes())
            throw new FestivalException("La représentation doit se terminer avant la fin du festival");
    }

    private Representation trouverCollision(String scene, int decalageDebut, int duree, Representation ignoree) {
        return representations.stream()
                .filter(autre -> autre != ignoree && autre.estEnCollision(scene, decalageDebut, duree))
                .findFirst()
                .orElse(null);
    }

    /**
     * Planifie une représentation d'un artiste engagé au festival.
     *
     * @param representation la représentation à ajouter
     * @throws FestivalException si la représentation est null, déjà planifiée, concerne un artiste
     *                           non engagé ou chevauche une autre représentation sur la même scène
     */
    public void ajouterRepresentation(Representation representation) throws FestivalException {
        if (representation == null)
            throw new FestivalException("La représentation est null, doit être défini");
        if (representations.contains(representation))
            throw new FestivalException("La représentation est déjà planifiée");
        if (!artistes.contains(representation.getArtiste()))
            throw new FestivalException(String.format("L'artiste \"%s\" n'est pas engagé au festival", representation.getArtiste().getNom()));
        verifierCreneau(representation.getScene(), representation.getDecalageDebut(), representation.getDuree());

        Representation collision = trouverCollision(representation.getScene(), representation.getDecalageDebut(), representation.getDuree(), null);
        if (collision != null)
            throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifiés", collision));

        try {
            representation.getArtiste().ajouterRepresentation(representation);
        } catch (ArtisteException e) {
            throw new FestivalException(e.getMessage());
        }
        representations.add(representation);
        LOGGER.fine("Added Representation to Festival.representations");
    }

    /**
     * Déplace une représentation planifiée, en vérifiant que la scène est libre sur le nouveau créneau.
     *
     * @param representation la représentation à modifier
     * @param decalageDebut     le nouveau début en minutes depuis le début du festival
     * @param duree       la nouvelle durée en minutes
     * @param scene          la nouvelle scène
     * @throws FestivalException       si la représentation n'est pas planifiée ou si le créneau est occupé
     * @throws RepresentationException si les nouvelles valeurs ne sont pas valides
     */
    public void modifierRepresentation(Representation representation, int decalageDebut, int duree, String scene) throws FestivalException, RepresentationException {
        if (!representations.contains(representation))
            throw new FestivalException("La représentation n'a pas été trouvée");
        // Valide les nouvelles valeurs avant toute modification
        new Representation(decalageDebut, duree, scene, representation.getArtiste());
        verifierCreneau(scene, decalageDebut, duree);

        Representation collision = trouverCollision(scene, decalageDebut, duree, representation);
        if (collision != null)
            throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifiés", collision));

        representation.setDecalageDebut(decalageDebut);
        representation.setDuree(duree);
        representation.setScene(scene);
        LOGGER.info("Updated Representation of Festival.representations");
    }

    /**
     * Supprime une représentation du festival et de son artiste.
     *
     * @param representation la représentation à supprimer
     * @throws FestivalException si la représentation n'a pas été trouvée
     */
    public void supprimerRepresentation(Representation representation) throws FestivalException {
        if (!representations.remove(representation))
            throw new FestivalException("La représentation n'a pas été trouvée");
        try {
            representation.getArtiste().supprimerRepresentation(representation);
        } catch (ArtisteException e) {
            LOGGER.warning("Representation was not linked to its Artiste: " + e.getMessage());
        }
        LOGGER.fine("Removed Representation from Festival.representations");
    }

    /**
     * Retourne les représentations du festival, triées par début puis par scène.
     *
     * @return une copie triée et non modifiable des représentations
     */
    public List<Representation> getRepresentations() {
        return representations.stream()
                .sorted(Comparator.comparingInt(Representation::getDecalageDebut).thenComparing(Representation::getScene))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Ajoute un type de ticket à la liste des types de tickets du festival.
     *
     * @param typeTicket le type de ticket à ajouter
     * @throws FestivalException si le type de ticket est null
     */
    public void ajouterTypeTicket(TypeTicket typeTicket) throws FestivalException {
        if (typeTicket == null)
            throw new FestivalException("Le type de ticket est null, doit être défini");
        if (typesTicket.stream().anyMatch(t -> Objects.equals(t.getType(), typeTicket.getType())))
            throw new FestivalException("Un autre type ticket existe avec le même nom");
        typesTicket.add(typeTicket);
        LOGGER.fine("Added TypeTicket to Festival.typesTicket");
    }

    /**
     * Modifie un type de ticket en une seule fois : si une valeur est refusée, rien n'est modifié.
     *
     * @param typeTicket le type de ticket à modifier
     * @param type       le nouveau nom, unique dans le festival
     * @param quantite   la nouvelle quantité
     * @param prix      le nouveau prix
     * @param surface    la nouvelle surface par détenteur (m²)
     * @throws FestivalException si le type de ticket n'appartient pas au festival ou si une valeur est refusée
     */
    public void modifierTypeTicket(TypeTicket typeTicket, String type, int quantite, float prix, float surface) throws FestivalException {
        if (!typesTicket.contains(typeTicket))
            throw new FestivalException("Le type de ticket n'a pas été trouvé");
        if (typesTicket.stream().anyMatch(t -> t != typeTicket && Objects.equals(t.getType(), type)))
            throw new FestivalException("Un autre type ticket existe avec le même nom");

        String ancienType = typeTicket.getType();
        int ancienneQuantite = typeTicket.getQuantite();
        float ancienPrix = typeTicket.getPrix();
        float ancienneSurface = typeTicket.getSurface();
        try {
            typeTicket.setType(type);
            typeTicket.setQuantite(quantite);
            typeTicket.setPrix(prix);
            typeTicket.setSurface(surface);
        } catch (TypeTicketException e) {
            try {
                typeTicket.setType(ancienType);
                typeTicket.setQuantite(ancienneQuantite);
                typeTicket.setPrix(ancienPrix);
                typeTicket.setSurface(ancienneSurface);
            } catch (TypeTicketException impossible) {
                throw new IllegalStateException("Le type de ticket ne peut pas être rétabli", impossible);
            }
            throw new FestivalException(e.getMessage());
        }
        LOGGER.info("Updated TypeTicket of Festival.typesTicket");
    }

    /**
     * Supprime un type de ticket du festival, en détachant ses avantages des stocks.
     *
     * @param typeTicket le type de ticket à supprimer
     * @throws FestivalException si le type de ticket n'a pas été trouvé
     */
    public void supprimerTypeTicket(TypeTicket typeTicket) throws FestivalException {
        if (!typesTicket.contains(typeTicket))
            throw new FestivalException("Le type de ticket n'a pas été trouvé");
        for (Avantage avantage : new ArrayList<>(typeTicket.getAvantages()))
            avantage.detacher();
        typesTicket.remove(typeTicket);
        LOGGER.fine("Removed TypeTicket from Festival.typesTicket");
    }

    /**
     * Retourne la liste des types de tickets du festival.
     *
     * @return la liste des types de tickets, non modifiable
     */
    public List<TypeTicket> getTypesTicket() {
        return Collections.unmodifiableList(typesTicket);
    }

    /**
     * Ajoute un stock à la liste des stocks du festival.
     *
     * @param stock le stock à ajouter
     * @throws FestivalException si le stock est null
     */
    public void ajouterStock(Stock stock) throws FestivalException {
        if (stock == null)
            throw new FestivalException("Le stock est null, doit être défini");
        if (stocks.stream().anyMatch(s -> Objects.equals(s.getNom(), stock.getNom())))
            throw new FestivalException("Un autre stock existe avec le même nom");
        stocks.add(stock);
        LOGGER.fine("Added Stock to Festival.stocks");
    }

    /**
     * Renomme un stock en gardant les noms de stocks uniques.
     *
     * @param stock le stock à renommer
     * @param nom  le nouveau nom
     * @throws FestivalException si le stock n'appartient pas au festival, ou si le nom est vide ou déjà utilisé
     */
    public void renommerStock(Stock stock, String nom) throws FestivalException {
        if (!stocks.contains(stock))
            throw new FestivalException("Le stock n'a pas été trouvé");
        if (stocks.stream().anyMatch(s -> s != stock && Objects.equals(s.getNom(), nom)))
            throw new FestivalException("Un autre stock existe avec le même nom");
        try {
            stock.setNom(nom);
        } catch (StockException e) {
            throw new FestivalException(e.getMessage());
        }
    }

    /**
     * Supprime un stock du festival, en détachant ses avantages des types de tickets.
     *
     * @param stock le stock à supprimer
     * @throws FestivalException si le stock n'a pas été trouvé
     */
    public void supprimerStock(Stock stock) throws FestivalException {
        if (!stocks.contains(stock))
            throw new FestivalException("Le stock n'a pas été trouvé");
        for (Avantage avantage : new ArrayList<>(stock.getAvantages()))
            avantage.detacher();
        stocks.remove(stock);
        LOGGER.fine("Removed Stock from Festival.stocks");
    }

    /**
     * Retourne la liste des stocks du festival.
     *
     * @return la liste des stocks, non modifiable
     */
    public List<Stock> getStocks() {
        return Collections.unmodifiableList(stocks);
    }

    /**
     * Calcule la vente la plus rentable (UC3.2) : la quantité de chaque type de ticket
     * qui maximise la recette, sans dépasser la surface du festival ni les stocks limités.
     * Le festival n'est pas modifié : voir {@link #appliquerPlanDeVente(PlanDeVente)}.
     *
     * @return la recommandation de vente
     * @throws FestivalException s'il n'y a aucun type de ticket ou si le problème n'a pas de solution
     */
    public PlanDeVente optimiserVentes() throws FestivalException {
        if (typesTicket.isEmpty())
            throw new FestivalException("Aucun type de ticket à mettre en vente");
        LOGGER.info("Optimizing ticket sales");

        ExpressionsBasedModel modele = new ExpressionsBasedModel();
        Expression contrainteSurface = modele.addExpression("Surface").upper(superficie);
        Map<Stock, Expression> contraintesStocks = new HashMap<>();
        for (Stock stock : stocks)
            if (stock.estLimite())
                contraintesStocks.put(stock, modele.addExpression(stock.getNom()).upper(stock.getQuantite()));

        List<Variable> variables = new ArrayList<>();
        for (TypeTicket typeTicket : typesTicket) {
            Variable x = modele.addVariable(typeTicket.getType()).weight(typeTicket.getPrix()).lower(0).integer(true);
            contrainteSurface.set(x, typeTicket.getSurface());
            for (Avantage avantage : typeTicket.getAvantages()) {
                Expression contrainte = contraintesStocks.get(avantage.getStock());
                if (contrainte != null)
                    contrainte.set(x, avantage.getQuantiteParTicket());
            }
            variables.add(x);
        }

        Optimisation.Result resultat = modele.maximise();
        if (!resultat.getState().isFeasible())
            throw new FestivalException("Aucune vente optimale trouvée (" + resultat.getState() + ")");

        Map<TypeTicket, Integer> quantites = new LinkedHashMap<>();
        double recette = 0;
        for (int i = 0; i < typesTicket.size(); i++) {
            // Le solveur peut renvoyer 1999.9999999 pour 2000 : on arrondit au plus proche
            int quantite = resultat.get(i).setScale(0, RoundingMode.HALF_UP).intValueExact();
            quantites.put(typesTicket.get(i), quantite);
            recette += (double) quantite * typesTicket.get(i).getPrix();
        }
        LOGGER.info("Optimized ticket sales");
        return new PlanDeVente(quantites, recette);
    }

    /**
     * Applique une recommandation de vente aux types de tickets du festival.
     * Si une quantité est refusée, les quantités précédentes sont rétablies.
     *
     * @param plan la recommandation calculée par {@link #optimiserVentes()}
     * @throws FestivalException si la recommandation concerne un ticket inconnu ou ne peut pas être appliquée
     */
    public void appliquerPlanDeVente(PlanDeVente plan) throws FestivalException {
        for (TypeTicket typeTicket : plan.quantites().keySet())
            if (!typesTicket.contains(typeTicket))
                throw new FestivalException(String.format("Le type de ticket \"%s\" n'appartient pas au festival", typeTicket.getType()));

        Map<TypeTicket, Integer> precedentes = new LinkedHashMap<>();
        for (TypeTicket typeTicket : plan.quantites().keySet())
            precedentes.put(typeTicket, typeTicket.getQuantite());
        try {
            definirQuantites(plan.quantites());
        } catch (TypeTicketException e) {
            try {
                definirQuantites(precedentes);
            } catch (TypeTicketException impossible) {
                throw new IllegalStateException("Les quantités précédentes ne peuvent pas être rétablies", impossible);
            }
            throw new FestivalException("La recommandation ne peut pas être appliquée : " + e.getMessage());
        }
        LOGGER.info("Applied sales plan");
    }

    /**
     * Définit les quantités de plusieurs types de tickets, en les remettant d'abord à zéro
     * pour ne pas dépasser un stock limité pendant le changement.
     *
     * @param quantites les quantités à appliquer
     * @throws TypeTicketException si une quantité est refusée
     */
    private void definirQuantites(Map<TypeTicket, Integer> quantites) throws TypeTicketException {
        for (TypeTicket typeTicket : quantites.keySet())
            typeTicket.setQuantite(0);
        for (Map.Entry<TypeTicket, Integer> entree : quantites.entrySet())
            entree.getKey().setQuantite(entree.getValue());
    }

    /**
     * Calcule le bilan prévisionnel du festival (UC3.2) :
     * recettes des tickets, emplacement, cachets des artistes et coût des stocks nécessaires.
     *
     * @return le bilan prévisionnel
     */
    public Bilan getBilan() {
        List<Bilan.Ligne> recettes = new ArrayList<>();
        for (TypeTicket typeTicket : typesTicket)
            recettes.add(new Bilan.Ligne(
                    String.format("Tickets « %s » (%d × %s €)", typeTicket.getType(), typeTicket.getQuantite(), formaterMontant(typeTicket.getPrix())),
                    typeTicket.getRecette()
            ));

        List<Bilan.Ligne> charges = new ArrayList<>();
        charges.add(new Bilan.Ligne(String.format("Location de l'emplacement (%s)", emplacement), prixLocation));
        for (Artiste artiste : artistes)
            charges.add(new Bilan.Ligne(String.format("Cachet de %s", artiste.getNom()), artiste.getCachet()));
        for (Stock stock : stocks)
            charges.add(new Bilan.Ligne(
                    String.format("Stock « %s » (%d × %s €)", stock.getNom(), stock.getQuantiteNecessaire(), formaterMontant(stock.getPrix())),
                    stock.getQuantiteNecessaire() * stock.getPrix()
            ));
        return new Bilan(recettes, charges);
    }

    /**
     * Retourne le coût des stocks nécessaires (UC3.3).
     *
     * @return la somme, pour chaque stock, de la quantité nécessaire multipliée par le prix unitaire
     */
    public double getCoutStocks() {
        return stocks.stream().mapToDouble(stock -> stock.getQuantiteNecessaire() * stock.getPrix()).sum();
    }

    /**
     * Retourne les stocks non limités dont la quantité prévue ne couvre pas la consommation des tickets.
     *
     * @return les stocks insuffisants
     */
    public List<Stock> getStocksInsuffisants() {
        return stocks.stream().filter(stock -> !stock.estLimite() && stock.getQuantiteManquante() > 0).toList();
    }

    /**
     * Retourne le total des cachets des artistes engagés.
     *
     * @return la somme des cachets
     */
    public double getTotalCachets() {
        return artistes.stream().mapToDouble(Artiste::getCachet).sum();
    }

    /**
     * Retourne le nombre de personnes attendues : la somme des tickets mis en vente.
     *
     * @return le nombre de personnes attendues
     */
    public long getFrequentationAttendue() {
        return typesTicket.stream().mapToLong(TypeTicket::getQuantite).sum();
    }

    /**
     * Retourne la surface occupée par les détenteurs de tickets.
     *
     * @return la somme, pour chaque type de ticket, de la quantité multipliée par la surface par personne (m²)
     */
    public double getSurfaceOccupee() {
        return typesTicket.stream().mapToDouble(t -> (double) t.getQuantite() * t.getSurface()).sum();
    }

    /**
     * Retourne le nombre d'agents de sécurité recommandé : un pour 100 personnes attendues, au moins 3.
     *
     * @return le nombre d'agents de sécurité recommandé
     */
    public long getAgentsSecuriteRecommandes() {
        return Math.max(3, (getFrequentationAttendue() + 99) / 100);
    }

    /**
     * Formate un montant sans décimales inutiles.
     *
     * @param montant le montant
     * @return le montant formaté
     */
    private static String formaterMontant(double montant) {
        return new BigDecimal(Double.toString(montant)).stripTrailingZeros().toPlainString();
    }
}
