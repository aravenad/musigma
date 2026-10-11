package com.musigma.models;

import com.musigma.models.exception.ArtisteException;
import com.musigma.models.exception.FestivalException;
import com.musigma.models.exception.RepresentationException;
import com.musigma.models.exception.TypeTicketException;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;

import java.io.*;
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

    private static final float AVERAGE_AREA_BY_PEOPLE = .42f;

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
     * @param start         la date de début du festival
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
        setName(name);
        setStart(start);
        setLocationPrice(locationPrice);
        setArea(area);
        setLocation(location);
        LOGGER.info("Created Festival");
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
     * @throws FestivalException si la date est null ou antérieure à la date actuelle
     */
    public void setStart(LocalDateTime start) throws FestivalException {
        if (start == null)
            throw new FestivalException("La date de début est null, doit être définie");
        if (LocalDate.now().isAfter(start.toLocalDate())) {
            throw new FestivalException("La date est antérieure à la date d'aujourd'hui, doit être aujourd'hui ou plus tard");
        }
        this.start = start;
        LOGGER.info("Set Festival.start");
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
     * Optimise les coûts du festival en calculant les quantités respectives
     * de tickets à vendre les plus rentables avec le modèle d'optimisation d'Ojalgo.
     *
     * @return le prix total du festival
     * @throws TypeTicketException si un ticket ne veux pas changer de quantité
     */
    public double optimizeResult() throws TypeTicketException {
        LOGGER.info("Calculated best quantity of ticket to sold");

        ExpressionsBasedModel model = new ExpressionsBasedModel();
        HashMap<Stock, Expression> constraints = new HashMap<>();

        LOGGER.info("Adding constraint stocks");
        for (Stock stock : stocks)
            if (stock.isFixed()) {
                Expression constraint = model.addExpression(stock.getName())
                        .upper(stock.getQuantity())
                        .lower(0);
                constraints.put(stock, constraint);
                LOGGER.info(String.format("Added variable \"%s\"", stock.getName()));
            }

        Expression areaConstraint = model.addExpression("Area")
                .upper(area)
                .lower(0);

        LOGGER.info("Definition of the variables");
        for (TypeTicket ticketType : ticketTypes) {
            Variable x = model.addVariable(ticketType.getType()).weight(ticketType.getPrice());
            for (Avantage avantage : ticketType.getAvantages()) {
                Stock stock = avantage.getStock();
                if (stock.isFixed()) {
                    constraints.get(stock).set(x, avantage.getQuantityByTicket());
                    LOGGER.info(String.format("Adding equation \"%s\" on ticket \"%s\"", avantage.getStock().getName(), ticketType.getType()));
                }
                areaConstraint.set(x, AVERAGE_AREA_BY_PEOPLE);
                LOGGER.info(String.format("Adding area equation on ticket \"%s\"", ticketType.getType()));
            }
        }

        LOGGER.info("Calculating optimized result");
        Optimisation.Result results = model.maximise();

        LOGGER.info("Setting optimized quantity of ticket to sold");
        for (int i = 0; i < ticketTypes.size(); i++)
            ticketTypes.get(i).setQuantity(results.get(i).intValueExact());

        LOGGER.info("Calculated best quantity of ticket to sold");
        return results.getValue();
    }
}
