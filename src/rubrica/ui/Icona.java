package rubrica.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;

/**
 * Icone dei bottoni delle toolbar, disegnate via codice: non servono file
 * immagine dentro il JAR e restano nitide anche sugli schermi ad alta risoluzione.
 */
final class Icona implements Icon {

    enum Tipo { NUOVO, MODIFICA, ELIMINA, SALVA, ANNULLA }

    private static final int LATO = 20;

    private static final Color VERDE = new Color(0x2E9E44);
    private static final Color ROSSO = new Color(0xD64545);
    private static final Color BLU = new Color(0x3B6FD4);
    private static final Color GRIGIO = new Color(0x7D838C);
    private static final Stroke TRATTO_SPESSO =
            new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Stroke TRATTO_SOTTILE =
            new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final Tipo tipo;

    Icona(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public int getIconWidth() {
        return LATO;
    }

    @Override
    public int getIconHeight() {
        return LATO;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            switch (tipo) {
                case NUOVO:
                    disegnaNuovo(g2);
                    break;
                case MODIFICA:
                    disegnaModifica(g2);
                    break;
                case ELIMINA:
                    disegnaElimina(g2);
                    break;
                case SALVA:
                    disegnaSalva(g2);
                    break;
                case ANNULLA:
                    disegnaAnnulla(g2);
                    break;
                default:
                    break;
            }
        } finally {
            g2.dispose();
        }
    }

    /** Cerchio verde con il segno più. */
    private static void disegnaNuovo(Graphics2D g) {
        g.setColor(VERDE);
        g.fill(new Ellipse2D.Double(1, 1, 18, 18));
        g.setColor(Color.WHITE);
        g.setStroke(TRATTO_SPESSO);
        g.draw(new Line2D.Double(10, 5.5, 10, 14.5));
        g.draw(new Line2D.Double(5.5, 10, 14.5, 10));
    }

    /** Matita in diagonale, con la punta in basso a sinistra. */
    private static void disegnaModifica(Graphics2D g) {
        g.rotate(-Math.PI / 4, 10, 10);

        g.setColor(new Color(0xF2A93B));
        g.fill(new Rectangle2D.Double(5, 7.5, 9, 5));
        g.setColor(new Color(0xA7ADB5));
        g.fill(new Rectangle2D.Double(14, 7.5, 1.5, 5));
        g.setColor(new Color(0xE8798B));
        g.fill(new RoundRectangle2D.Double(15.5, 7.5, 3, 5, 1.5, 1.5));

        Path2D legno = new Path2D.Double();
        legno.moveTo(5, 7.5);
        legno.lineTo(1, 10);
        legno.lineTo(5, 12.5);
        legno.closePath();
        g.setColor(new Color(0xF3D9A8));
        g.fill(legno);

        Path2D mina = new Path2D.Double();
        mina.moveTo(2.6, 9);
        mina.lineTo(1, 10);
        mina.lineTo(2.6, 11);
        mina.closePath();
        g.setColor(new Color(0x3A3A3A));
        g.fill(mina);

        g.setColor(new Color(0xB07A22));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Line2D.Double(5, 10, 14, 10));
    }

    /** Cestino rosso. */
    private static void disegnaElimina(Graphics2D g) {
        g.setColor(ROSSO);
        g.setStroke(new BasicStroke(1.6f));
        g.draw(new RoundRectangle2D.Double(7.5, 1.8, 5, 2.6, 1.5, 1.5));
        g.fill(new RoundRectangle2D.Double(3, 4, 14, 2.6, 1.5, 1.5));

        Path2D corpo = new Path2D.Double();
        corpo.moveTo(4.5, 7.6);
        corpo.lineTo(15.5, 7.6);
        corpo.lineTo(14.5, 18.5);
        corpo.lineTo(5.5, 18.5);
        corpo.closePath();
        g.fill(corpo);

        g.setColor(Color.WHITE);
        g.setStroke(TRATTO_SOTTILE);
        g.draw(new Line2D.Double(8, 10, 8.3, 16.3));
        g.draw(new Line2D.Double(10, 10, 10, 16.3));
        g.draw(new Line2D.Double(12, 10, 11.7, 16.3));
    }

    /** Dischetto blu. */
    private static void disegnaSalva(Graphics2D g) {
        g.setColor(BLU);
        g.fill(new RoundRectangle2D.Double(2, 2, 16, 16, 3, 3));

        g.setColor(new Color(0xDDE3EC));
        g.fill(new Rectangle2D.Double(6, 2, 8, 5.5));
        g.setColor(BLU);
        g.fill(new Rectangle2D.Double(11, 3, 2, 3.5));

        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(5, 10.5, 10, 6, 1.2, 1.2));
        g.setColor(new Color(0xB9C6DE));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(6.5, 12.8, 13.5, 12.8));
        g.draw(new Line2D.Double(6.5, 14.8, 13.5, 14.8));
    }

    /** Cerchio grigio con la X. */
    private static void disegnaAnnulla(Graphics2D g) {
        g.setColor(GRIGIO);
        g.fill(new Ellipse2D.Double(1, 1, 18, 18));
        g.setColor(Color.WHITE);
        g.setStroke(TRATTO_SPESSO);
        g.draw(new Line2D.Double(6.5, 6.5, 13.5, 13.5));
        g.draw(new Line2D.Double(13.5, 6.5, 6.5, 13.5));
    }
}
