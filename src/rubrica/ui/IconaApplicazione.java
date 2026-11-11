package rubrica.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Icona delle finestre (barra del titolo e barra delle applicazioni): una rubrica
 * ad anelli con la sagoma di una persona in copertina e le linguette colorate.
 * È disegnata via codice in più dimensioni, il sistema sceglie quella adatta.
 */
final class IconaApplicazione {

    private static final int[] DIMENSIONI = {16, 20, 24, 32, 40, 48, 64, 128, 256};
    /** Il disegno è definito su una griglia 64x64 e scalato a ogni dimensione. */
    private static final double GRIGLIA = 64.0;

    private static final Color COPERTINA = new Color(0x2F6FB5);
    private static final Color DORSO = new Color(0x24589A);
    private static final Color ANELLO = new Color(0xE3E7ED);
    private static final Color BORDO_ANELLO = new Color(0x6B7684);
    private static final Color[] LINGUETTE = {new Color(0xF2A93B), new Color(0x2E9E44), new Color(0xD64545)};

    private static List<Image> immagini;

    private IconaApplicazione() {
    }

    static synchronized List<Image> immagini() {
        if (immagini == null) {
            List<Image> lista = new ArrayList<>();
            for (int lato : DIMENSIONI) {
                lista.add(disegna(lato));
            }
            immagini = Collections.unmodifiableList(lista);
        }
        return immagini;
    }

    private static BufferedImage disegna(int lato) {
        BufferedImage img = new BufferedImage(lato, lato, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.scale(lato / GRIGLIA, lato / GRIGLIA);
            disegnaRubrica(g);
        } finally {
            g.dispose();
        }
        return img;
    }

    private static void disegnaRubrica(Graphics2D g) {
        // linguette dell'indice, sporgono dal bordo destro della copertina
        for (int i = 0; i < LINGUETTE.length; i++) {
            g.setColor(LINGUETTE[i]);
            g.fill(new RoundRectangle2D.Double(46, 12 + i * 13, 13, 10, 5, 5));
        }

        // copertina con il dorso più scuro
        RoundRectangle2D copertina = new RoundRectangle2D.Double(9, 4, 43, 56, 9, 9);
        g.setColor(COPERTINA);
        g.fill(copertina);
        Area dorso = new Area(copertina);
        dorso.intersect(new Area(new Rectangle2D.Double(9, 4, 9, 56)));
        g.setColor(DORSO);
        g.fill(dorso);

        // anelli della rilegatura
        g.setStroke(new BasicStroke(1.6f));
        for (int i = 0; i < 4; i++) {
            RoundRectangle2D anello = new RoundRectangle2D.Double(4, 11 + i * 12, 12, 5.5, 5.5, 5.5);
            g.setColor(ANELLO);
            g.fill(anello);
            g.setColor(BORDO_ANELLO);
            g.draw(anello);
        }

        // sagoma della persona: testa e spalle
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(27.5, 14, 16, 16));
        Area spalle = new Area(new Ellipse2D.Double(21.5, 33, 28, 26));
        spalle.intersect(new Area(new Rectangle2D.Double(0, 0, GRIGLIA, 48)));
        g.fill(spalle);
    }
}
