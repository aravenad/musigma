import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.calendarfx.view.DayEntryView;
import com.musigma.controllers.MainController;
import com.musigma.controllers.WorkspaceController;
import com.musigma.controllers.workspaces.*;
import com.musigma.models.*;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuBar;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;

/**
 * Génère les captures d'écran du guide d'utilisation à partir de la vraie fenêtre de Musigma,
 * ouverte hors de l'écran sur un festival de démonstration.
 * <p>
 * Usage (depuis la racine du projet, voir guide/generer.py) : {@code java CapturesGuide guide/images}
 */
public class CapturesGuide {

    private static final double ECHELLE = 2;
    private static File dossier;
    private static Stage stage;
    private static MainController controleur;
    private static final Deque<Runnable> etapes = new ArrayDeque<>();
    private static final CountDownLatch fin = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        dossier = new File(args[0]);
        CountDownLatch demarre = new CountDownLatch(1);
        Platform.startup(demarre::countDown);
        demarre.await();
        Platform.runLater(CapturesGuide::preparer);
        fin.await();
        Platform.exit();
        System.exit(0);
    }

    /**
     * Festival de démonstration : celui de l'exemple du rapport de méthodes numériques, sur trois jours.
     */
    private static Festival festival() throws Exception {
        LocalDate vendredi = LocalDate.now().plusWeeks(4).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        Festival f = new Festival("Musigma Fest", vendredi.atStartOfDay(), 25000, 14000, "Esplanade, Grenoble");
        f.setFin(vendredi.plusDays(3).atStartOfDay());
        f.ajouterScene("Chapiteau");

        Stock gobelet = new Stock("Gobelet", 8000, true, 0.5);
        Stock parking = new Stock("Place de parking", 3000, true, 2);
        Stock tshirt = new Stock("T-shirt", 1500, false, 6);
        f.ajouterStock(gobelet);
        f.ajouterStock(parking);
        f.ajouterStock(tshirt);

        TypeTicket vip = new TypeTicket("VIP", 1500, 50);
        vip.setSurface(4);
        TypeTicket nonVip = new TypeTicket("Non-VIP", 2500, 35);
        nonVip.setSurface(2);
        f.ajouterTypeTicket(vip);
        f.ajouterTypeTicket(nonVip);
        new Avantage(vip, gobelet, 1).connecter();
        new Avantage(vip, parking, 1).connecter();
        new Avantage(vip, tshirt, 1).connecter();
        new Avantage(nonVip, gobelet, 2).connecter();

        Artiste gorillaz = new Artiste("Gorillaz", "Pop", 40000);
        Artiste daftPunk = new Artiste("Daft Punk", "Électro", 60000);
        Artiste maalouf = new Artiste("Ibrahim Maalouf", "Jazz", 15000);
        Artiste orelsan = new Artiste("Orelsan", "Rap", 30000);
        for (Artiste a : new Artiste[]{gorillaz, daftPunk, maalouf, orelsan})
            f.ajouterArtiste(a);
        int jour = 24 * 60;
        f.ajouterRepresentation(new Representation(19 * 60, 90, Festival.SCENE_PAR_DEFAUT, orelsan));
        f.ajouterRepresentation(new Representation(21 * 60, 120, Festival.SCENE_PAR_DEFAUT, daftPunk));
        f.ajouterRepresentation(new Representation(18 * 60, 75, "Chapiteau", maalouf));
        f.ajouterRepresentation(new Representation(jour + 20 * 60, 90, Festival.SCENE_PAR_DEFAUT, gorillaz));
        f.ajouterRepresentation(new Representation(jour + 18 * 60 + 30, 60, "Chapiteau", maalouf));
        f.ajouterRepresentation(new Representation(2 * jour + 19 * 60, 90, "Chapiteau", orelsan));
        f.ajouterRepresentation(new Representation(2 * jour + 21 * 60, 90, Festival.SCENE_PAR_DEFAUT, gorillaz));
        return f;
    }

    private static void preparer() {
        try {
            FXMLLoader loader = new FXMLLoader(WorkspaceController.class.getResource("/com/musigma/views/main-view.fxml"));
            Parent racine = loader.load();
            stage = new Stage();
            stage.setScene(new Scene(racine, 1280, 800));
            stage.setX(-8000);
            stage.setY(-8000);
            controleur = loader.getController();
            controleur.initialize(stage, null);
            // Affichage de référence, quelles que soient les préférences de l'utilisateur
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
            prive("applyZoom", int.class).invoke(controleur, 100);
            Node aide = racine.lookup(".helpBar");
            aide.setVisible(true);
            aide.setManaged(true);
            prive("loadFestival", Festival.class).invoke(controleur, festival());
        } catch (Exception e) {
            e.printStackTrace();
            fin.countDown();
            return;
        }

        espace(HomeController.REGISTER, "01-accueil");
        espace(ArtistController.REGISTER, null);
        etapes.add(() -> selectionnerPremiereLigne());
        etapes.add(() -> capturer("02-artistes"));
        espace(CalendarController.REGISTER, null);
        etapes.add(() -> attendreRepresentations(0));
        etapes.add(() -> capturer("03-planning"));
        espace(StockController.REGISTER, "04-stocks");
        espace(TicketController.REGISTER, "05-tickets");
        espace(AccountingController.REGISTER, null);
        etapes.add(() -> cliquer("Calculer la vente optimale"));
        etapes.add(() -> capturer("06-prevision"));
        etapes.add(() -> menu(0, "07-menu-fichier"));
        etapes.add(() -> menu(1, "08-menu-affichage"));
        etapes.add(() -> Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet()));
        etapes.add(() -> capturer("09-theme-sombre"));
        etapes.add(fin::countDown);
        suivante();
    }

    private static Method prive(String nom, Class<?>... types) throws NoSuchMethodException {
        Method m = MainController.class.getDeclaredMethod(nom, types);
        m.setAccessible(true);
        return m;
    }

    /**
     * Ajoute l'ouverture d'un espace de travail, suivie de sa capture si un nom est donné.
     */
    private static void espace(WorkspaceController.WorkspaceRegister espace, String capture) {
        etapes.add(() -> {
            try {
                prive("loadWorkspace", WorkspaceController.WorkspaceRegister.class).invoke(controleur, espace);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        if (capture != null)
            etapes.add(() -> capturer(capture));
    }

    /**
     * Exécute l'étape suivante après une pause, le temps que l'affichage se mette à jour.
     */
    private static void suivante() {
        PauseTransition pause = new PauseTransition(Duration.millis(900));
        pause.setOnFinished(e -> {
            Runnable etape = etapes.poll();
            if (etape == null)
                return;
            try {
                etape.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (!etapes.isEmpty())
                suivante();
        });
        pause.play();
    }

    private static void attendreRepresentations(int essais) {
        if (trouver(stage.getScene().getRoot(), DayEntryView.class) == null && essais < 20) {
            etapes.addFirst(() -> attendreRepresentations(essais + 1));
            return;
        }
        System.out.println("représentations affichées après " + essais + " attente(s)");
    }

    private static void selectionnerPremiereLigne() {
        javafx.scene.control.TableView<?> table = trouver(stage.getScene().getRoot(), javafx.scene.control.TableView.class);
        if (table != null)
            table.getSelectionModel().selectFirst();
    }

    private static void cliquer(String texte) {
        Button bouton = trouverBouton(stage.getScene().getRoot(), texte);
        if (bouton != null)
            bouton.fire();
    }

    private static void menu(int index, String capture) {
        MenuBar barre = trouver(stage.getScene().getRoot(), MenuBar.class);
        barre.getMenus().get(index).show();
        etapes.addFirst(() -> {
            for (Window w : Window.getWindows())
                if (w instanceof ContextMenu && w.isShowing())
                    enregistrer(w.getScene().getRoot(), capture);
            barre.getMenus().get(index).hide();
        });
    }

    private static void capturer(String nom) {
        enregistrer(stage.getScene().getRoot(), nom);
    }

    private static void enregistrer(Node noeud, String nom) {
        SnapshotParameters parametres = new SnapshotParameters();
        parametres.setTransform(Transform.scale(ECHELLE, ECHELLE));
        WritableImage image = noeud.snapshot(parametres, null);
        int w = (int) image.getWidth(), h = (int) image.getHeight();
        BufferedImage sortie = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader pixels = image.getPixelReader();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                sortie.setRGB(x, y, pixels.getArgb(x, y));
        try {
            ImageIO.write(sortie, "png", new File(dossier, nom + ".png"));
            System.out.println("capture " + nom);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T trouver(Node noeud, Class<T> type) {
        if (type.isInstance(noeud))
            return (T) noeud;
        if (noeud instanceof Parent parent)
            for (Node enfant : parent.getChildrenUnmodifiable()) {
                T trouve = trouver(enfant, type);
                if (trouve != null)
                    return trouve;
            }
        return null;
    }

    private static Button trouverBouton(Node noeud, String texte) {
        if (noeud instanceof Button b && texte.equals(((Labeled) b).getText()))
            return b;
        if (noeud instanceof Parent parent)
            for (Node enfant : parent.getChildrenUnmodifiable()) {
                Button trouve = trouverBouton(enfant, texte);
                if (trouve != null)
                    return trouve;
            }
        return null;
    }
}
