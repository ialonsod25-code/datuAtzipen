
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Scanner;

public class Importazioa {

    public static void main(String[] args) {
        try {
            Konfigurazioa conf = new Konfigurazioa("KONFIGURAZIOA.txt");
            Scanner sc = new Scanner(System.in);

            System.out.println("1. Inportatu");
            System.out.println("2. Esportatu");
            System.out.print("Aukeratu: ");
            int op = Integer.parseInt(sc.nextLine());

            if (op == 1) {
                inportatu(conf);
            } else if (op == 2) {
                System.out.println("Esportazioa oraindik ez dago eginda.");
            } else {
                System.out.println("Aukera okerra.");
            }

        } catch (Exception e) {
            System.err.println("Errorea: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void inportatu(Konfigurazioa conf) throws Exception {
       
        List<Kanpina> kanpinak = Irakurlea.leerKanpinak(conf.getXmlKanpinak());
        List<Bezeroa> bezeroak = Irakurlea.leerBezeroak(conf.getXmlBezeroak());

        System.out.println("Kanpinak irakurrita: " + kanpinak.size());
        System.out.println("Bezeroak irakurrita: " + bezeroak.size());

       
        try (Connection con = DriverManager.getConnection(
                conf.getUrl(), conf.getUser(), conf.getPassword())) {

            con.setAutoCommit(false);
            try {
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
