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
 * La classe Festival représente un festival avec un nom, une date de début, un prix de location,
 * une superficie, un emplacement et des ressources, comme des artistes,
 * des types de tickets, de stocks et de représentations.
 */
public class Festival implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Nom de la scène créée avec un nouveau festival.
     */
    public static final String DEFAULT_SCENE = "Scène principale";

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
    private final ArrayList<TypeTicket> ticketTypes;
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
     * Non finale pour pouvoir être initialisée à la lecture d'un ancien fichier.
     */
    private ArrayList<String> scenes;    /**
     * Fichier associé à ce festival, renseigné au chargement ou à l'enregistrement.
     */
    private transient File file = null;
    /**
     * État sérialisé du festival lors du dernier enregistrement, pour détecter les modifications.
     */
    private transient byte[] savedState = null;
    /**
     * Nom du festival.
     */
    private String name;
    /**
     * Date de début du festival.
     */
    private LocalDateTime start;
    /**
     * Date de fin du festival (exclue).
     */
    private LocalDateTime end;
    /**
     * Prix de location du festival.
     */
    private float locationPrice;
    /**
     * Emplacement du festival.
     */
    private String location;
    /**
     * Superficie du festival (m2)
     */
    private float area;

    /**
     * Constructeur de la classe Festival.
     *
     * @param name          le nom du festival
     * @param start         la date de début du festival ; le festival dure un jour et compte une scène par défaut
     * @param locationPrice le prix de location du festival
     * @param area          la superficie du festival
     * @param location      l'emplacement du festival
     * @throws FestivalException si les valeurs des paramètres ne sont pas valides
     */
    public Festival(String name, LocalDateTime start, float locationPrice, float area, String location) throws FestivalException {
        LOGGER.info("Initialize Festival");
        artistes = new ArrayList<>();
        ticketTypes = new ArrayList<>();
        stocks = new ArrayList<>();
        representations = new ArrayList<>();
        scenes = new ArrayList<>(List.of(DEFAULT_SCENE));
        setName(name);
        setStart(start);
        end = start.plusDays(1);
        setLocationPrice(locationPrice);
        setArea(area);
        setLocation(location);
        LOGGER.info("Created Festival");
    }

    /**
     * Complète un festival enregistré avant l'ajout de la date de fin et des scènes.
     *
     * @param in le flux de lecture
     * @throws IOException            si la lecture échoue
     * @throws ClassNotFoundException si une classe du fichier est inconnue
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (end == null) {
            int lastMinute = representations.stream().mapToInt(r -> r.getStartDelta() + r.getDuration()).max().orElse(0);
            end = start.plusDays(Math.max(1, (lastMinute + 24 * 60 - 1) / (24 * 60)));
        }
        if (scenes == null) {
            scenes = new ArrayList<>();
            representations.stream().map(Representation::getScene).distinct().forEach(scenes::add);
            if (scenes.isEmpty())
                scenes.add(DEFAULT_SCENE);
        }
    }

    /**
     * Méthode statique pour charger un festival depuis un fichier sérialisé.
     *
     * @param file le chemin du fichier à charger
     * @return le festival chargé depuis le fichier
     * @throws FestivalException si le fichier n'est pas défini
     */
    public static Festival Festival(File file) throws FestivalException {
        try (
                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            LOGGER.info(String.format("Reading file %s", file.getAbsolutePath()));
            Festival festival = (Festival) ois.readObject();
            festival.file = file;
            festival.markSaved();
            LOGGER.info(String.format("Loaded festival \"%s\" from file %s", festival.getName(), file.getAbsolutePath()));
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
    public void save() throws FestivalException {
        if (file == null)
            throw new FestivalException("Le fichier du festival n'a pas été spécifié");
        try (FileOutputStream fos = new FileOutputStream(file.getAbsolutePath())) {
            byte[] state = serialize();
            fos.write(state);
            savedState = state;
            LOGGER.info(String.format("Saved festival \"%s\" to file %s", name, file.getAbsolutePath()));
        } catch (IOException e) {
            throw new FestivalException("Le festival n'arrive pas à enregister le fichier : " + e.getMessage());
        }
    }

    /**
     * Considère l'état actuel du festival comme enregistré.
     * Les modifications ultérieures seront signalées par {@link #isModified()}.
     *
     * @throws IllegalStateException si le festival ne peut pas être sérialisé
     */
    public void markSaved() {
        savedState = serialize();
    }

    /**
     * Indique si le festival a été modifié depuis le dernier enregistrement ou chargement,
     * en comparant son état sérialisé à celui enregistré.
     *
     * @return true si le festival a été modifié, false sinon
     */
    public boolean isModified() {
        return savedState == null || !Arrays.equals(savedState, serialize());
    }

    /**
     * Sérialise le festival et tout ce qu'il contient.
     *
     * @return le festival sérialisé
     */
    private byte[] serialize() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new IllegalStateException("Le festival ne peut pas être sérialisé", e);
        }
        return bytes.toByteArray();
    }

    /**
     * Retourne le fichier associé à ce festival.
     *
     * @return le fichier du festival
     */
    public File getFile() {
        return file;
    }

    /**
     * Définit le fichier associé à ce festival.
     * Ce chemin est utilisé pour la sauvegarde et le chargement du festival.
     *
     * @param file le nouveau fichier du festival
     * @throws FestivalException si le fichier est null
     */
    public void setFile(File file) throws FestivalException {
        if (file == null)
            throw new FestivalException("Le fichier du festival est null, doit être défini");
        this.file = file;
        LOGGER.info("Set Festival.file");
    }

    /**
     * Retourne le nom du festival.
     *
     * @return le nom du festival
     */
    public String getName() {
        return name;
    }

    /**
     * Définit le nom du festival.
     *
     * @param name le nom du festival
     * @throws FestivalException si le nom est null ou vide
     */
    public void setName(String name) throws FestivalException {
        if (name == null || name.isEmpty())
            throw new FestivalException("Le nom est null ou vide, doit être défini");
        this.name = name;
        LOGGER.info("Set Festival.name");
    }

    /**
     * Retourne la date de début du festival.
     *
     * @return la date de début du festival
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Définit la date de début du festival.
     *
     * @param start la date de début du festival
     * La date de fin est décalée d'autant : le festival garde sa durée et ses représentations.
     *
     * @throws FestivalException si la date est null ou antérieure à la date actuelle
     */
    public void setStart(LocalDateTime start) throws FestivalException {
        if (start == null)
            throw new FestivalException("La date de début est null, doit être définie");
        if (LocalDate.now().isAfter(start.toLocalDate())) {
            throw new FestivalException("La date est antérieure à la date d'aujourd'hui, doit être aujourd'hui ou plus tard");
        }
        if (end != null)
            end = end.plus(Duration.between(this.start, start));
        this.start = start;
        LOGGER.info("Set Festival.start");
    }

    /**
     * Retourne la date de fin du festival (exclue).
     *
     * @return la date de fin du festival
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Définit la date de fin du festival (exclue).
     *
     * @param end la date de fin du festival
     * @throws FestivalException si la date est null, ne suit pas la date de début,
     *                           ou si des représentations se terminent après elle
     */
    public void setEnd(LocalDateTime end) throws FestivalException {
        if (end == null)
            throw new FestivalException("La date de fin est null, doit être définie");
        if (!end.isAfter(start))
            throw new FestivalException("La date de fin doit suivre la date de début");
        long minutes = Duration.between(start, end).toMinutes();
        Optional<Representation> outside = representations.stream()
                .filter(r -> r.getStartDelta() + r.getDuration() > minutes)
                .findFirst();
        if (outside.isPresent())
            throw new FestivalException(String.format("La représentation %s se termine après cette date de fin", outside.get()));
        this.end = end;
        LOGGER.info("Set Festival.end");
    }

    /**
     * Retourne la durée du festival en minutes.
     *
     * @return le nombre de minutes entre le début et la fin du festival
     */
    public int getDurationMinutes() {
        return (int) Duration.between(start, end).toMinutes();
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
    public void addScene(String scene) throws FestivalException {
        checkNewSceneName(scene);
        scenes.add(scene.strip());
        LOGGER.info("Added scene to Festival.scenes");
    }

    /**
     * Renomme une scène, ainsi que la scène de ses représentations.
     *
     * @param scene   le nom actuel de la scène
     * @param newName le nouveau nom
     * @throws FestivalException si la scène n'existe pas, ou si le nouveau nom est vide ou déjà utilisé
     */
    public void renameScene(String scene, String newName) throws FestivalException {
        int index = scenes.indexOf(scene);
        if (index < 0)
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas", scene));
        checkNewSceneName(newName);
        String name = newName.strip();
        for (Representation representation : representations)
            if (representation.getScene().equals(scene))
                try {
                    representation.setScene(name);
                } catch (RepresentationException e) {
                    throw new FestivalException(e.getMessage());
                }
        scenes.set(index, name);
        LOGGER.info("Renamed scene of Festival.scenes");
    }

    /**
     * Supprime une scène sans représentation. Le festival garde toujours au moins une scène.
     *
     * @param scene le nom de la scène
     * @throws FestivalException si la scène n'existe pas, est la dernière, ou accueille des représentations
     */
    public void removeScene(String scene) throws FestivalException {
        if (!scenes.contains(scene))
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas", scene));
        if (scenes.size() == 1)
            throw new FestivalException("Le festival doit garder au moins une scène");
        long used = representations.stream().filter(r -> r.getScene().equals(scene)).count();
        if (used > 0)
            throw new FestivalException(String.format("La scène \"%s\" accueille %d représentation(s), à déplacer ou supprimer d'abord", scene, used));
        scenes.remove(scene);
        LOGGER.info("Removed scene from Festival.scenes");
    }

    /**
     * Vérifie qu'un nom de scène est valide et libre.
     *
     * @param scene le nom à vérifier
     * @throws FestivalException si le nom est vide ou déjà utilisé
     */
    private void checkNewSceneName(String scene) throws FestivalException {
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
    public long getFreeMinutes() {
        long occupied = representations.stream().mapToLong(Representation::getDuration).sum();
        return (long) scenes.size() * getDurationMinutes() - occupied;
    }

    /**
     * Retourne le prix de location du festival.
     *
     * @return le prix de location du festival
     */
    public float getLocationPrice() {
        return locationPrice;
    }

    /**
     * Définit le prix de location du festival.
     *
     * @param locationPrice le prix de location du festival
     * @throws FestivalException si le prix est négatif
     */
    public void setLocationPrice(float locationPrice) throws FestivalException {
        if (locationPrice < 0)
            throw new FestivalException("Le prix de location est négatif, doit être positif");
        this.locationPrice = locationPrice;
        LOGGER.info("Set Festival.locationPrice");
    }

    /**
     * Retourne la superficie du festival.
     *
     * @return la superficie du festival
     */
    public float getArea() {
        return area;
    }

    /**
     * Définit la superficie du festival.
     *
     * @param area la superficie du festival
     * @throws FestivalException si la superficie est négative
     */
    public void setArea(float area) throws FestivalException {
        if (area <= 0)
            throw new FestivalException("La superficie est négative ou nulle (0), doit être positive");
        this.area = area;
        LOGGER.info("Set Festival.area");
    }

    /**
     * Retourne l'emplacement du festival.
     *
     * @return l'emplacement du festival
     */
    public String getLocation() {
        return location;
    }

    /**
     * Définit l'emplacement du festival.
     *
     * @param location l'emplacement du festival
     * @throws FestivalException si l'emplacement est null ou vide
     */
    public void setLocation(String location) throws FestivalException {
        if (location == null || location.isEmpty())
            throw new FestivalException("L'emplacement de location est null ou vide, doit être défini");
        this.location = location;
        LOGGER.info("Set Festival.location");
    }

    /**
     * Ajoute un artiste à la liste des artistes du festival.
     *
     * @param artiste l'artiste à ajouter
     * @throws FestivalException si l'artiste est null ou déjà engagé
     */
    public void addArtiste(Artiste artiste) throws FestivalException {
        if (artiste == null)
            throw new FestivalException("L'artiste est null, doit être défini");
        if (artistes.contains(artiste))
            throw new FestivalException(String.format("L'artiste \"%s\" est déjà engagé", artiste.getName()));
        artistes.add(artiste);
        LOGGER.info("Added Artiste to Festival.artistes");
    }

    /**
     * Supprime un artiste du festival, ainsi que toutes ses représentations.
     *
     * @param artiste l'artiste à supprimer
     * @throws FestivalException si l'artiste n'a pas été trouvé
     */
    public void removeArtiste(Artiste artiste) throws FestivalException {
        if (!artistes.contains(artiste))
            throw new FestivalException("L'artiste n'a pas été trouvé");
        for (Representation representation : new ArrayList<>(artiste.getRepresentations()))
            removeRepresentation(representation);
        artistes.remove(artiste);
        LOGGER.info("Removed Artiste from Festival.artistes");
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
     * @param startDelta le début du créneau en minutes depuis le début du festival
     * @param duration   la durée du créneau en minutes
     * @return true si aucune représentation n'occupe la scène pendant ce créneau
     */
    public boolean isSceneFree(String scene, int startDelta, int duration) {
        return findCollision(scene, startDelta, duration, null) == null;
    }

    /**
     * Cherche une représentation occupant la scène pendant le créneau donné.
     *
     * @param scene      la scène
     * @param startDelta le début du créneau en minutes depuis le début du festival
     * @param duration   la durée du créneau en minutes
     * @param ignored    une représentation à ne pas prendre en compte (celle qu'on déplace), ou null
     * @return la première représentation en collision, ou null s'il n'y en a pas
     */
    /**
     * Vérifie qu'un créneau est sur une scène du festival et pendant le festival.
     *
     * @param scene      la scène
     * @param startDelta le début du créneau en minutes depuis le début du festival
     * @param duration   la durée du créneau en minutes
     * @throws FestivalException si la scène n'existe pas ou si le créneau déborde du festival
     */
    private void checkSlot(String scene, int startDelta, int duration) throws FestivalException {
        if (!scenes.contains(scene))
            throw new FestivalException(String.format("La scène \"%s\" n'existe pas dans le festival", scene));
        if (startDelta + duration > getDurationMinutes())
            throw new FestivalException("La représentation doit se terminer avant la fin du festival");
    }

    private Representation findCollision(String scene, int startDelta, int duration, Representation ignored) {
        return representations.stream()
                .filter(other -> other != ignored && other.isColliding(scene, startDelta, duration))
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
    public void addRepresentation(Representation representation) throws FestivalException {
        if (representation == null)
            throw new FestivalException("La représentation est null, doit être défini");
        if (representations.contains(representation))
            throw new FestivalException("La représentation est déjà planifiée");
        if (!artistes.contains(representation.getArtiste()))
            throw new FestivalException(String.format("L'artiste \"%s\" n'est pas engagé au festival", representation.getArtiste().getName()));
        checkSlot(representation.getScene(), representation.getStartDelta(), representation.getDuration());

        Representation collision = findCollision(representation.getScene(), representation.getStartDelta(), representation.getDuration(), null);
        if (collision != null)
            throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifiés", collision));

        try {
            representation.getArtiste().addRepresentation(representation);
        } catch (ArtisteException e) {
            throw new FestivalException(e.getMessage());
        }
        representations.add(representation);
        LOGGER.info("Added Representation to Festival.representations");
    }

    /**
     * Déplace une représentation planifiée, en vérifiant que la scène est libre sur le nouveau créneau.
     *
     * @param representation la représentation à modifier
     * @param startDelta     le nouveau début en minutes depuis le début du festival
     * @param duration       la nouvelle durée en minutes
     * @param scene          la nouvelle scène
     * @throws FestivalException       si la représentation n'est pas planifiée ou si le créneau est occupé
     * @throws RepresentationException si les nouvelles valeurs ne sont pas valides
     */
    public void updateRepresentation(Representation representation, int startDelta, int duration, String scene) throws FestivalException, RepresentationException {
        if (!representations.contains(representation))
            throw new FestivalException("La représentation n'a pas été trouvée");
        // Valide les nouvelles valeurs avant toute modification
        new Representation(startDelta, duration, scene, representation.getArtiste());
        checkSlot(scene, startDelta, duration);

        Representation collision = findCollision(scene, startDelta, duration, representation);
        if (collision != null)
            throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifiés", collision));

        representation.setStartDelta(startDelta);
        representation.setDuration(duration);
        representation.setScene(scene);
        LOGGER.info("Updated Representation of Festival.representations");
    }

    /**
     * Supprime une représentation du festival et de son artiste.
     *
     * @param representation la représentation à supprimer
     * @throws FestivalException si la représentation n'a pas été trouvée
     */
    public void removeRepresentation(Representation representation) throws FestivalException {
        if (!representations.remove(representation))
            throw new FestivalException("La représentation n'a pas été trouvée");
        try {
            representation.getArtiste().removeRepresentation(representation);
        } catch (ArtisteException e) {
            LOGGER.warning("Representation was not linked to its Artiste: " + e.getMessage());
        }
        LOGGER.info("Removed Representation from Festival.representations");
    }

    /**
     * Retourne les représentations du festival, triées par début puis par scène.
     *
     * @return une copie triée et non modifiable des représentations
     */
    public List<Representation> getRepresentations() {
        return representations.stream()
                .sorted(Comparator.comparingInt(Representation::getStartDelta).thenComparing(Representation::getScene))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Ajoute un type de ticket à la liste des types de tickets du festival.
     *
     * @param ticketType le type de ticket à ajouter
     * @throws FestivalException si le type de ticket est null
     */
    public void addTicketType(TypeTicket ticketType) throws FestivalException {
        if (ticketType == null)
            throw new FestivalException("Le type de ticket est null, doit être défini");
        if (ticketTypes.stream().anyMatch(t -> Objects.equals(t.getType(), ticketType.getType())))
            throw new FestivalException("Un autre type ticket existe avec le même nom");
        ticketTypes.add(ticketType);
        LOGGER.info("Added TypeTicket to Festival.ticketTypes");
    }

    /**
     * Modifie un type de ticket en une seule fois : si une valeur est refusée, rien n'est modifié.
     *
     * @param ticketType le type de ticket à modifier
     * @param type       le nouveau nom, unique dans le festival
     * @param quantity   la nouvelle quantité
     * @param price      le nouveau prix
     * @param surface    la nouvelle surface par détenteur (m²)
     * @throws FestivalException si le type de ticket n'appartient pas au festival ou si une valeur est refusée
     */
    public void updateTicketType(TypeTicket ticketType, String type, int quantity, float price, float surface) throws FestivalException {
        if (!ticketTypes.contains(ticketType))
            throw new FestivalException("Le type de ticket n'a pas été trouvé");
        if (ticketTypes.stream().anyMatch(t -> t != ticketType && Objects.equals(t.getType(), type)))
            throw new FestivalException("Un autre type ticket existe avec le même nom");

        String oldType = ticketType.getType();
        int oldQuantity = ticketType.getQuantity();
        float oldPrice = ticketType.getPrice();
        float oldSurface = ticketType.getSurface();
        try {
            ticketType.setType(type);
            ticketType.setQuantity(quantity);
            ticketType.setPrice(price);
            ticketType.setSurface(surface);
        } catch (TypeTicketException e) {
            try {
                ticketType.setType(oldType);
                ticketType.setQuantity(oldQuantity);
                ticketType.setPrice(oldPrice);
                ticketType.setSurface(oldSurface);
            } catch (TypeTicketException impossible) {
                throw new IllegalStateException("Le type de ticket ne peut pas être rétabli", impossible);
            }
            throw new FestivalException(e.getMessage());
        }
        LOGGER.info("Updated TypeTicket of Festival.ticketTypes");
    }

    /**
     * Supprime un type de ticket du festival, en détachant ses avantages des stocks.
     *
     * @param ticketType le type de ticket à supprimer
     * @throws FestivalException si le type de ticket n'a pas été trouvé
     */
    public void removeTicketType(TypeTicket ticketType) throws FestivalException {
        if (!ticketTypes.contains(ticketType))
            throw new FestivalException("Le type de ticket n'a pas été trouvé");
        for (Avantage avantage : new ArrayList<>(ticketType.getAvantages()))
            avantage.detach();
        ticketTypes.remove(ticketType);
        LOGGER.info("Removed TypeTicket from Festival.ticketTypes");
    }

    /**
     * Retourne la liste des types de tickets du festival.
     *
     * @return la liste des types de tickets, non modifiable
     */
    public List<TypeTicket> getTicketTypes() {
        return Collections.unmodifiableList(ticketTypes);
    }

    /**
     * Ajoute un stock à la liste des stocks du festival.
     *
     * @param stock le stock à ajouter
     * @throws FestivalException si le stock est null
     */
    public void addStock(Stock stock) throws FestivalException {
        if (stock == null)
            throw new FestivalException("Le stock est null, doit être défini");
        if (stocks.stream().anyMatch(s -> Objects.equals(s.getName(), stock.getName())))
            throw new FestivalException("Un autre stock existe avec le même nom");
        stocks.add(stock);
        LOGGER.info("Added Stock to Festival.stocks");
    }

    /**
     * Renomme un stock en gardant les noms de stocks uniques.
     *
     * @param stock le stock à renommer
     * @param name  le nouveau nom
     * @throws FestivalException si le stock n'appartient pas au festival, ou si le nom est vide ou déjà utilisé
     */
    public void renameStock(Stock stock, String name) throws FestivalException {
        if (!stocks.contains(stock))
            throw new FestivalException("Le stock n'a pas été trouvé");
        if (stocks.stream().anyMatch(s -> s != stock && Objects.equals(s.getName(), name)))
            throw new FestivalException("Un autre stock existe avec le même nom");
        try {
            stock.setName(name);
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
    public void removeStock(Stock stock) throws FestivalException {
        if (!stocks.contains(stock))
            throw new FestivalException("Le stock n'a pas été trouvé");
        for (Avantage avantage : new ArrayList<>(stock.getAvantages()))
            avantage.detach();
        stocks.remove(stock);
        LOGGER.info("Removed Stock from Festival.stocks");
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
     * Le festival n'est pas modifié : voir {@link #applySalesPlan(SalesPlan)}.
     *
     * @return la recommandation de vente
     * @throws FestivalException s'il n'y a aucun type de ticket ou si le problème n'a pas de solution
     */
    public SalesPlan optimizeSales() throws FestivalException {
        if (ticketTypes.isEmpty())
            throw new FestivalException("Aucun type de ticket à mettre en vente");
        LOGGER.info("Optimizing ticket sales");

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        Expression areaConstraint = model.addExpression("Surface").upper(area);
        Map<Stock, Expression> stockConstraints = new HashMap<>();
        for (Stock stock : stocks)
            if (stock.isFixed())
                stockConstraints.put(stock, model.addExpression(stock.getName()).upper(stock.getQuantity()));

        List<Variable> variables = new ArrayList<>();
        for (TypeTicket ticketType : ticketTypes) {
            Variable x = model.addVariable(ticketType.getType()).weight(ticketType.getPrice()).lower(0).integer(true);
            areaConstraint.set(x, ticketType.getSurface());
            for (Avantage avantage : ticketType.getAvantages()) {
                Expression constraint = stockConstraints.get(avantage.getStock());
                if (constraint != null)
                    constraint.set(x, avantage.getQuantityByTicket());
            }
            variables.add(x);
        }

        Optimisation.Result result = model.maximise();
        if (!result.getState().isFeasible())
            throw new FestivalException("Aucune vente optimale trouvée (" + result.getState() + ")");

        Map<TypeTicket, Integer> quantities = new LinkedHashMap<>();
        double revenue = 0;
        for (int i = 0; i < ticketTypes.size(); i++) {
            // Le solveur peut renvoyer 1999.9999999 pour 2000 : on arrondit au plus proche
            int quantity = result.get(i).setScale(0, RoundingMode.HALF_UP).intValueExact();
            quantities.put(ticketTypes.get(i), quantity);
            revenue += (double) quantity * ticketTypes.get(i).getPrice();
        }
        LOGGER.info("Optimized ticket sales");
        return new SalesPlan(quantities, revenue);
    }

    /**
     * Applique une recommandation de vente aux types de tickets du festival.
     * Si une quantité est refusée, les quantités précédentes sont rétablies.
     *
     * @param plan la recommandation calculée par {@link #optimizeSales()}
     * @throws FestivalException si la recommandation concerne un ticket inconnu ou ne peut pas être appliquée
     */
    public void applySalesPlan(SalesPlan plan) throws FestivalException {
        for (TypeTicket ticketType : plan.quantities().keySet())
            if (!ticketTypes.contains(ticketType))
                throw new FestivalException(String.format("Le type de ticket \"%s\" n'appartient pas au festival", ticketType.getType()));

        Map<TypeTicket, Integer> previous = new LinkedHashMap<>();
        for (TypeTicket ticketType : plan.quantities().keySet())
            previous.put(ticketType, ticketType.getQuantity());
        try {
            setQuantities(plan.quantities());
        } catch (TypeTicketException e) {
            try {
                setQuantities(previous);
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
     * @param quantities les quantités à appliquer
     * @throws TypeTicketException si une quantité est refusée
     */
    private void setQuantities(Map<TypeTicket, Integer> quantities) throws TypeTicketException {
        for (TypeTicket ticketType : quantities.keySet())
            ticketType.setQuantity(0);
        for (Map.Entry<TypeTicket, Integer> entry : quantities.entrySet())
            entry.getKey().setQuantity(entry.getValue());
    }

    /**
     * Calcule le bilan prévisionnel du festival (UC3.2) :
     * recettes des tickets, location, cachets des artistes et coût des stocks nécessaires.
     *
     * @return le bilan prévisionnel
     */
    public Budget getBudget() {
        List<Budget.Line> revenues = new ArrayList<>();
        for (TypeTicket ticketType : ticketTypes)
            revenues.add(new Budget.Line(
                    String.format("Tickets « %s » (%d × %s €)", ticketType.getType(), ticketType.getQuantity(), formatAmount(ticketType.getPrice())),
                    (double) ticketType.getQuantity() * ticketType.getPrice()
            ));

        List<Budget.Line> costs = new ArrayList<>();
        costs.add(new Budget.Line(String.format("Location de l'emplacement (%s)", location), locationPrice));
        for (Artiste artiste : artistes)
            costs.add(new Budget.Line(String.format("Cachet de %s", artiste.getName()), artiste.getPrice()));
        for (Stock stock : stocks)
            costs.add(new Budget.Line(
                    String.format("Stock « %s » (%d × %s €)", stock.getName(), stock.getRequiredQuantity(), formatAmount(stock.getPrix())),
                    stock.getRequiredQuantity() * stock.getPrix()
            ));
        return new Budget(revenues, costs);
    }

    /**
     * Retourne le nombre de personnes attendues : la somme des tickets mis en vente.
     *
     * @return le nombre de personnes attendues
     */
    public long getExpectedAttendance() {
        return ticketTypes.stream().mapToLong(TypeTicket::getQuantity).sum();
    }

    /**
     * Retourne la surface occupée par les détenteurs de tickets.
     *
     * @return la somme, pour chaque type de ticket, de la quantité multipliée par la surface par personne (m²)
     */
    public double getOccupiedSurface() {
        return ticketTypes.stream().mapToDouble(t -> (double) t.getQuantity() * t.getSurface()).sum();
    }

    /**
     * Retourne le nombre d'agents de sécurité recommandé : un pour 100 personnes attendues, au moins 3.
     *
     * @return le nombre d'agents de sécurité recommandé
     */
    public long getRecommendedSecurityAgents() {
        return Math.max(3, (getExpectedAttendance() + 99) / 100);
    }

    /**
     * Formate un montant sans décimales inutiles.
     *
     * @param amount le montant
     * @return le montant formaté
     */
    private static String formatAmount(double amount) {
        return new BigDecimal(Double.toString(amount)).stripTrailingZeros().toPlainString();
    }
}
