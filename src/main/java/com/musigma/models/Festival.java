package com.musigma.models;

import com.musigma.models.exception.FestivalException;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeSet;
import java.util.logging.Logger;

import static com.musigma.utils.Log.getLogger;

/**
 * La classe Festival représente un festival avec un nom, une date de début, un prix de location,
 * une superficie, un emplacement et des ressources, comme des artistes,
 * des types de tickets, de stocks et de représentations.
 */
public class Festival implements Serializable {

    // Logger de la class
    private static final Logger LOGGER = getLogger(Festival.class);

    // Fichier du festival
    private File file = null;

    // Nom du festival
    private String name;

    // Date de début du festival
    private LocalDateTime start;

    // Prix de l'emplacement
    private float locationPrice;

    // Nom de l'emplacement
    private String location;

    // Aire de l'emplacement
    private double area;

    // Liste des artistes embauchés pour le festival
    private final ArrayList<Artiste> artistes;

    // Liste des types de tickets proposé (VIP, Standard, ...)
    private final ArrayList<TypeTicket> ticketTypes;

    // Liste des stocks disponibles ou requis (Place de parking, bouteille d'eau, ...)
    private final ArrayList<Stock> stocks;

    // Liste des représentations du festival, soit le planning
    private final TreeSet<Representation> representations;

    /**
     * Constructeur de la classe Festival.
     *
     * @param name le nom du festival
     * @param start la date de début du festival
     * @param locationPrice le prix de location du festival
     * @param area la superficie du festival
     * @param location l'emplacement du festival
     * @throws FestivalException si les valeurs des paramètres ne sont pas valides
     */
    public Festival(String name, LocalDateTime start, float locationPrice, double area, String location) throws FestivalException {
        LOGGER.info(String.format("Initialize Festival 0x%x", super.hashCode()));
        artistes = new ArrayList<>();
        ticketTypes = new ArrayList<>();
        stocks = new ArrayList<>();
        representations = new TreeSet<>();
        setName(name);
        setStart(start);
        setLocationPrice(locationPrice);
        setArea(area);
        setLocation(location);
        LOGGER.info(String.format("Created Festival 0x%x", super.hashCode()));
    }

    /**
     * Méthode statique pour charger un festival depuis un fichier sérialisé.
     *
     * @param file le chemin du fichier à charger
     * @return le festival chargé depuis le fichier
     * @throws IOException            en cas d'erreur d'entrée/sortie
     * @throws ClassNotFoundException si la classe n'est pas trouvée lors de la désérialisation
     */
    public static Festival Festival(File file) throws FestivalException {
        try (
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            ObjectInputStream ois = new ObjectInputStream(fis);
        ){
            LOGGER.info(String.format("Reading file %s", file.getAbsolutePath()));
            Festival festival = (Festival) ois.readObject();
            festival.file = file;
            LOGGER.info(String.format("Loaded festival 0x%x from file %s", festival.hashCode(), file.getAbsolutePath()));
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
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public void save() throws FestivalException {
        if (file == null)
            throw new FestivalException("Le fichier du festival n'a pas été spécifié");
        try (
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(this);
            LOGGER.info(String.format("Saved festival 0x%x to file %s", super.hashCode(), file.getAbsolutePath()));
        } catch (Exception e) {
            throw new FestivalException("Le festival n'arrive pas à enregister le fichier");
        }
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
     */
    public void setFile(File file) throws FestivalException {
        if (file == null)
            throw new FestivalException("Le fichier du festival est null, doit être défini");
        this.file = file;
        LOGGER.info(String.format("Set Festival.file 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Festival.name 0x%x", super.hashCode()));
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
     * @throws FestivalException si la date est antérieure à la date actuelle
     */
    public void setStart(LocalDateTime start) throws FestivalException {
        if (LocalDate.now().isAfter(start.toLocalDate())) {
            throw new FestivalException("La date est antérieure à la date d'aujourd'hui, doit précéder la date d'aujourd'hui");
        }
        this.start = start;
        LOGGER.info(String.format("Set Festival.name 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Festival.locationPrice 0x%x", super.hashCode()));
    }

    /**
     * Retourne la superficie du festival.
     *
     * @return la superficie du festival
     */
    public double getArea() {
        return area;
    }

    /**
     * Définit la superficie du festival.
     *
     * @param area la superficie du festival
     * @throws FestivalException si la superficie est négative
     */
    public void setArea(double area) throws FestivalException {
        if (area <= 0)
            throw new FestivalException("La superficie est négative ou nulle (0), doit être positive");
        this.area = area;
        LOGGER.info(String.format("Set Festival.area 0x%x", super.hashCode()));
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
        LOGGER.info(String.format("Set Festival.location 0x%x", super.hashCode()));
    }

    /**
     * Ajoute un artiste à la liste des artistes du festival.
     *
     * @param artiste l'artiste à ajouter
     * @throws FestivalException si l'artiste est null
     */
    public void addArtiste(Artiste artiste) throws FestivalException {
        if (artiste == null)
            throw new FestivalException("L'artiste est null, doit être défini");
        artistes.add(artiste);
        LOGGER.info(String.format("Added Artiste to Festival.artistes 0x%x", super.hashCode()));
    }

    /**
     * Supprime un artiste de la liste des artistes du festival.
     *
     * @param artiste l'artiste à supprimer
     * @throws FestivalException si l'artiste n'a pas été trouvé
     */
    public void removeArtiste(Artiste artiste) throws FestivalException {
        if (!artistes.remove(artiste))
            throw new FestivalException("L'artiste n'a pas été trouvé");
        LOGGER.info(String.format("Removed Artiste from Festival.artistes 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des artistes du festival.
     *
     * @return la liste des artistes
     */
    public ArrayList<Artiste> getArtistes() {
        return artistes;
    }

    /**
     * Ajoute une représentation à la liste des représentations du festival.
     *
     * @param representation la représentation à ajouter
     * @throws FestivalException si la représentation est null ou en collision avec une autre
     */
    public void addRepresentation(Representation representation) throws FestivalException {
        if (representation == null)
            throw new FestivalException("La représentation est null, doit être défini");

        Representation top = representations.ceiling(representation),
                bottom = representations.floor(representation);

        if (top != null && top.isColliding(representation))
            if (bottom != null && bottom.isColliding(representation))
                throw new FestivalException(String.format("La représentation rentre en collision avec %s et %s, les départs/durées doivent être modifié", bottom, top));
             else
                throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifié", top));
        else if (bottom != null && bottom.isColliding(representation))
            throw new FestivalException(String.format("La représentation rentre en collision avec %s, les départs/durées doivent être modifié", bottom));

        representations.add(representation);
        LOGGER.info(String.format("Added Representation to Festival.representations 0x%x", super.hashCode()));
    }

    /**
     * Supprime une représentation de la liste des représentations du festival.
     *
     * @param representation la représentation à supprimer
     * @throws FestivalException si la représentation n'a pas été trouvée
     */
    public void removeRepresentation(Representation representation) throws FestivalException {
        if (!representations.remove(representation))
            throw new FestivalException("La représentation n'a pas été trouvée");
        LOGGER.info(String.format("Removed Representation from Festival.representations 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des représentations du festival.
     *
     * @return la liste des représentations
     */
    public TreeSet<Representation> getRepresentations() {
        return representations;
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
        ticketTypes.add(ticketType);
        LOGGER.info(String.format("Added TypeTicket to Festival.ticketTypes 0x%x", super.hashCode()));
    }

    /**
     * Supprime un type de ticket de la liste des types de tickets du festival.
     *
     * @param ticketType le type de ticket à supprimer
     * @throws FestivalException si le type de ticket n'a pas été trouvé
     */
    public void removeTicketType(TypeTicket ticketType) throws FestivalException {
        if (!ticketTypes.remove(ticketType))
            throw new FestivalException("Le type de ticket n'a pas été trouvé");
        LOGGER.info(String.format("Removed TypeTicket from.ticketTypes 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des types de tickets du festival.
     *
     * @return la liste des types de tickets
     */
    public ArrayList<TypeTicket> getTicketTypes() {
        return ticketTypes;
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
        stocks.add(stock);
        LOGGER.info(String.format("Added Stock to Festival.stocks 0x%x", super.hashCode()));
    }

    /**
     * Supprime un stock de la liste des stocks du festival.
     *
     * @param stock le stock à supprimer
     * @throws FestivalException si le stock n'a pas été trouvé
     */
    public void removeStock(Stock stock) throws FestivalException {
        if (!stocks.remove(stock))
            throw new FestivalException("Le stock n'a pas été trouvé");
        LOGGER.info(String.format("Removed Stock from Festival.stocks 0x%x", super.hashCode()));
    }

    /**
     * Retourne la liste des stocks du festival.
     *
     * @return la liste des stocks
     */
    public ArrayList<Stock> getStocks() {
        return stocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Festival festival = (Festival) o;
        return Float.compare(locationPrice, festival.locationPrice) == 0 && Double.compare(area, festival.area) == 0 && Objects.equals(file, festival.file) && Objects.equals(name, festival.name) && Objects.equals(start, festival.start) && Objects.equals(location, festival.location) && Objects.equals(artistes, festival.artistes) && Objects.equals(ticketTypes, festival.ticketTypes) && Objects.equals(stocks, festival.stocks) && Objects.equals(representations, festival.representations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, name, start, locationPrice, location, area, artistes, ticketTypes, stocks, representations);
    }
}
