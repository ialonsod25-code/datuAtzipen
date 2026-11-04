import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Datu-atzipena programaren sarrera-puntua.
 *
 * Bi erabilera:
 *  - Argumenturik gabe: menu interaktiboa (kontsolatik).
 *  - Argumentuekin (adibidez, C# aplikaziotik):
 *        java -jar kanpinak.jar inportatu [aukerak]
 *        java -jar kanpinak.jar esportatu <probintziaKodea> <herriKodea|0> [aukerak]
 *        java -jar kanpinak.jar zerrendatu [aukerak]
 *
 *    herriKodea = HERRIAK taulako kodea (probintzia*1000 + herria); 0 = probintziako herri guztiak.
 *    zerrendatu: probintziak ("P;kodea;izena") eta herriak ("H;probintzia;kodea;izena") idazten ditu, UTF-8.
 *
 *    Aukerak:  --konfig=fitxategia   (lehenetsia: KONFIGURAZIOA.txt)
 *              --irteera=karpeta     (lehenetsia: esportazioak)
 *
 * Irteera-kodeak: 0 ondo | 1 errorea | 2 argumentu okerrak | 3 daturik ez.
 */
public class Importazioa {

    public static final int OK = 0;
    public static final int ERROREA = 1;
    public static final int ARGUMENTU_OKERRAK = 2;
    public static final int DATURIK_EZ = 3;

    public static final String KONFIG_LEHENETSIA = "KONFIGURAZIOA.txt";

    public static void main(String[] args) {
        int irteera;
        try {
            irteera = (args.length == 0) ? menuInteraktiboa() : komandoa(args);
        } catch (Exception e) {
            System.err.println("Errorea: " + e.getMessage());
            e.printStackTrace();
            irteera = ERROREA;
        }
        System.exit(irteera);
    }

    // ------------------------------------------------------------ menu interaktiboa

    private static int menuInteraktiboa() throws Exception {
        Konfigurazioa conf = new Konfigurazioa(KONFIG_LEHENETSIA);
        Scanner sc = new Scanner(System.in);

        System.out.println("1. Inportatu");
        System.out.println("2. Esportatu");
        System.out.print("Aukeratu: ");
        int op = Integer.parseInt(sc.nextLine().trim());

        if (op == 1) {
            inportatu(conf);
            return OK;
        } else if (op == 2) {
            return Esportazioa.esportatu(conf, sc, Esportazioa.KARPETA);
        } else {
            System.out.println("Aukera okerra.");
            return ARGUMENTU_OKERRAK;
        }
    }

    // ------------------------------------------------------------ komando-lerroa

    private static int komandoa(String[] args) throws Exception {
        String konfig = KONFIG_LEHENETSIA;
        String irteera = Esportazioa.KARPETA;
        List<String> posizionalak = new ArrayList<>();

        for (String arg : args) {
            if (arg.startsWith("--konfig=")) {
                konfig = arg.substring("--konfig=".length());
            } else if (arg.startsWith("--irteera=")) {
                irteera = arg.substring("--irteera=".length());
            } else {
                posizionalak.add(arg);
            }
        }

        if (posizionalak.isEmpty()) {
            erabilera();
            return ARGUMENTU_OKERRAK;
        }
        String agindua = posizionalak.get(0).toLowerCase();

        if (agindua.equals("inportatu") && posizionalak.size() == 1) {
            inportatu(new Konfigurazioa(konfig));
            return OK;
        }

        if (agindua.equals("zerrendatu") && posizionalak.size() == 1) {
            PrintStream out = new PrintStream(System.out, true, "UTF-8");
            int emaitza = Esportazioa.zerrendatu(new Konfigurazioa(konfig), out);
            out.flush();
            return emaitza;
        }

        if (agindua.equals("esportatu") && posizionalak.size() == 3) {
            int probintzia;
            int herria;
            try {
                probintzia = Integer.parseInt(posizionalak.get(1));
                herria = Integer.parseInt(posizionalak.get(2));
            } catch (NumberFormatException e) {
                erabilera();
                return ARGUMENTU_OKERRAK;
            }
            return Esportazioa.esportatu(new Konfigurazioa(konfig), probintzia, herria, irteera);
        }

        erabilera();
        return ARGUMENTU_OKERRAK;
    }

    private static void erabilera() {
        System.err.println("Erabilera:");
        System.err.println("  java -jar kanpinak.jar                                    (menu interaktiboa)");
        System.err.println("  java -jar kanpinak.jar inportatu [aukerak]");
        System.err.println("  java -jar kanpinak.jar esportatu <probintziaKodea> <herriKodea|0> [aukerak]");
        System.err.println("  java -jar kanpinak.jar zerrendatu [aukerak]");
        System.err.println("Aukerak: --konfig=fitxategia  --irteera=karpeta");
        System.err.println("Irteera-kodeak: 0 ondo | 1 errorea | 2 argumentu okerrak | 3 daturik ez");
    }

    // ------------------------------------------------------------ inportazioa

    public static void inportatu(Konfigurazioa conf) throws Exception {

        List<Kanpina> kanpinak = Irakurlea.leerKanpinak(conf.getXmlKanpinak());
        List<Bezeroa> bezeroak = Irakurlea.leerBezeroak(conf.getXmlBezeroak());

        System.out.println("Kanpinak irakurrita: " + kanpinak.size());
        System.out.println("Bezeroak irakurrita: " + bezeroak.size());

        try (Connection con = DriverManager.getConnection(
                conf.getUrl(), conf.getUser(), conf.getPassword())) {

            con.setAutoCommit(false);
            try {
                Irakurlea.guardarProbintziak(con, kanpinak);
                Irakurlea.guardarHerriak(con, kanpinak);
                Irakurlea.guardarKanpinak(con, kanpinak);
                Irakurlea.guardarBezeroak(con, bezeroak);
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }

        System.out.println("Inportazioa amaituta.");
    }
}