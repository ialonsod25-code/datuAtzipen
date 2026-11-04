import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Datu-baseko kanpinak CSV eta XML formatuetara esportatzen ditu,
 * probintzia eta herri konkretu baten arabera.
 */
public class Esportazioa {

    public static final String KARPETA = "esportazioak";
    public static final String CSV_FITXATEGIA = "3PAG2_E1_export.csv";
    public static final String XML_FITXATEGIA = "3PAG2_E1_export.xml";

    private static class Aukera {
        final int kodea;
        final String izena;

        Aukera(int kodea, String izena) {
            this.kodea = kodea;
            this.izena = izena;
        }
    }

    private static Connection konektatu(Konfigurazioa conf) throws Exception {
        return DriverManager.getConnection(conf.getUrl(), conf.getUser(), conf.getPassword());
    }

    /** Menu interaktiboa: probintzia eta herria kontsolatik aukeratzen dira. */
    public static int esportatu(Konfigurazioa conf, Scanner sc, String karpeta) throws Exception {
        try (Connection con = konektatu(conf)) {

            List<Aukera> probintziak = irakurriAukerak(con,
                    "SELECT kodea, izena FROM probintziak ORDER BY izena");
            if (probintziak.isEmpty()) {
                System.out.println("Datu-basean ez dago daturik. Inportatu lehenengo.");
                return Importazioa.DATURIK_EZ;
            }

            System.out.println("Probintziak:");
            for (int i = 0; i < probintziak.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + probintziak.get(i).izena);
            }
            Aukera probintzia = probintziak.get(
                    irakurriZenbakia(sc, "Aukeratu probintzia: ", 1, probintziak.size()) - 1);

            // Herri-kodea = probintzia * 1000 + herria (ikusi Irakurlea.leerKanpinak)
            List<Aukera> herriak = irakurriAukerak(con,
                    "SELECT kodea, izena FROM herriak WHERE kodea >= ? AND kodea < ? ORDER BY izena",
                    probintzia.kodea * 1000, probintzia.kodea * 1000 + 1000);

            System.out.println("Herriak (" + probintzia.izena + "):");
            System.out.println("  0. Guztiak");
            for (int i = 0; i < herriak.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + herriak.get(i).izena);
            }
            int herriAukera = irakurriZenbakia(sc, "Aukeratu herria: ", 0, herriak.size());
            int herriKodea = (herriAukera == 0) ? 0 : herriak.get(herriAukera - 1).kodea;

            return esportatu(con, probintzia.kodea, herriKodea, karpeta);
        }
    }

    /**
     * Ez-interaktiboa (C# aplikaziotik deitzeko): kodeak argumentu gisa.
     * herriKodea = HERRIAK taulako kodea; 0 = probintziako herri guztiak.
     */
    public static int esportatu(Konfigurazioa conf, int probintziaKodea, int herriKodea, String karpeta) throws Exception {
        try (Connection con = konektatu(conf)) {
            return esportatu(con, probintziaKodea, herriKodea, karpeta);
        }
    }

    /**
     * Probintziak ("P;kodea;izena") eta herriak ("H;probintzia;kodea;izena") idazten ditu.
     * Datu-basea hutsik badago DATURIK_EZ itzultzen du.
     */
    public static int zerrendatu(Konfigurazioa conf, PrintStream out) throws Exception {
        try (Connection con = konektatu(conf)) {
            List<Aukera> probintziak = irakurriAukerak(con,
                    "SELECT kodea, izena FROM probintziak ORDER BY izena");
            for (Aukera p : probintziak) {
                out.println("P;" + p.kodea + ";" + p.izena);
            }
            List<Aukera> herriak = irakurriAukerak(con,
                    "SELECT kodea, izena FROM herriak ORDER BY TRUNC(kodea / 1000), izena");
            for (Aukera h : herriak) {
                out.println("H;" + (h.kodea / 1000) + ";" + h.kodea + ";" + h.izena);
            }
            return probintziak.isEmpty() ? Importazioa.DATURIK_EZ : Importazioa.OK;
        }
    }

    private static int esportatu(Connection con, int probintziaKodea, int herriKodea, String karpeta) throws Exception {
        List<Kanpina> kanpinak = irakurriKanpinak(con, probintziaKodea, herriKodea);
        if (kanpinak.isEmpty()) {
            System.out.println("Ez dago kanpinik aukeratutako lekuan.");
            return Importazioa.DATURIK_EZ;
        }

        Path dir = Paths.get(karpeta);
        Files.createDirectories(dir);
        Path csv = dir.resolve(CSV_FITXATEGIA).toAbsolutePath();
        Path xml = dir.resolve(XML_FITXATEGIA).toAbsolutePath();
        idatziCsv(kanpinak, csv);
        idatziXml(kanpinak, xml);

        System.out.println("Esportazioa amaituta: " + kanpinak.size() + " kanpin.");
        System.out.println("CSV: " + csv);
        System.out.println("XML: " + xml);
        return Importazioa.OK;
    }

    private static int irakurriZenbakia(Scanner sc, String mezua, int min, int max) {
        while (true) {
            System.out.print(mezua);
            String lerroa = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(lerroa);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException e) {
                // berriro galdetuko da
            }
            System.out.println("Aukera okerra (" + min + " - " + max + ").");
        }
    }

    private static List<Aukera> irakurriAukerak(Connection con, String sql, int... parametroak) throws Exception {
        List<Aukera> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametroak.length; i++) {
                ps.setInt(i + 1, parametroak[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Aukera(rs.getInt("kodea"), rs.getString("izena")));
                }
            }
        }
        return lista;
    }

    private static List<Kanpina> irakurriKanpinak(Connection con, int probintziaKodea, int herriKodea) throws Exception {
        String sql = "SELECT k.kodea, k.izena, k.deskribapena, k.kokalekua, k.telefonoa, k.helbidea, " +
                     "k.emaila, k.webgunea, k.kategoria, k.edukiera, k.postakodea, k.herri_kodea, " +
                     "k.probintzia_kodea, k.friendly_url, k.physical_url, k.data_xml, k.metadata_xml, " +
                     "k.zip_file, h.izena AS herria, p.izena AS probintzia " +
                     "FROM kanpinak k " +
                     "JOIN herriak h ON h.kodea = k.herri_kodea " +
                     "JOIN probintziak p ON p.kodea = k.probintzia_kodea " +
                     "WHERE k.probintzia_kodea = ?" +
                     (herriKodea != 0 ? " AND k.herri_kodea = ?" : "") +
                     " ORDER BY k.kodea";

        List<Kanpina> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, probintziaKodea);
            if (herriKodea != 0) {
                ps.setInt(2, herriKodea);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Kanpina k = new Kanpina();
                    k.setKodea(rs.getString("kodea"));
                    k.setIzena(rs.getString("izena"));
                    k.setDeskribapena(rs.getString("deskribapena"));
                    k.setKokalekua(rs.getString("kokalekua"));
                    k.setTelefonoa(rs.getString("telefonoa"));
                    k.setHelbidea(rs.getString("helbidea"));
                    k.setEmaila(rs.getString("emaila"));
                    k.setWebgunea(rs.getString("webgunea"));
                    k.setKategoria(rs.getString("kategoria"));
                    k.setEdukiera(rs.getInt("edukiera"));
                    k.setPostakodea(rs.getString("postakodea"));
                    k.setHerriKodea(rs.getInt("herri_kodea"));
                    k.setProbintziaKodea(rs.getInt("probintzia_kodea"));
                    k.setFriendlyUrl(rs.getString("friendly_url"));
                    k.setPhysicalUrl(rs.getString("physical_url"));
                    k.setDataXml(rs.getString("data_xml"));
                    k.setMetadataXml(rs.getString("metadata_xml"));
                    k.setZipFile(rs.getString("zip_file"));
                    k.setHerriIzena(rs.getString("herria"));
                    k.setProbintziaIzena(rs.getString("probintzia"));
                    lista.add(k);
                }
            }
        }
        return lista;
    }

    // ---------------------------------------------------------------- CSV

    private static void idatziCsv(List<Kanpina> kanpinak, Path fitxategia) throws Exception {
        try (BufferedWriter w = Files.newBufferedWriter(fitxategia, StandardCharsets.UTF_8)) {
            w.write("Kodea;Izena;Kokalekua;Helbidea;PostaKodea;Herria;Probintzia;Kategoria;Edukiera");
            w.newLine();
            for (Kanpina k : kanpinak) {
                String[] zutabeak = {
                    k.getKodea(), k.getIzena(), k.getKokalekua(), k.getHelbidea(),
                    k.getPostakodea(), k.getHerriIzena(), k.getProbintziaIzena(),
                    k.getKategoria(), String.valueOf(k.getEdukiera())
                };
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < zutabeak.length; i++) {
                    if (i > 0) {
                        sb.append(';');
                    }
                    sb.append(csv(zutabeak[i]));
                }
                w.write(sb.toString());
                w.newLine();
            }
        }
    }

    private static String csv(String balioa) {
        if (balioa == null) {
            return "";
        }
        if (balioa.contains(";") || balioa.contains("\"") || balioa.contains("\n") || balioa.contains("\r")) {
            return "\"" + balioa.replace("\"", "\"\"") + "\"";
        }
        return balioa;
    }

    // ---------------------------------------------------------------- XML

    private static void idatziXml(List<Kanpina> kanpinak, Path fitxategia) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element erroa = doc.createElement("kanpinak");
        doc.appendChild(erroa);

        for (Kanpina k : kanpinak) {
            Element kanpina = doc.createElement("kanpina");
            kanpina.setAttribute("id", k.getKodea());

            gehitu(doc, kanpina, "izena", k.getIzena());
            gehitu(doc, kanpina, "deskribapena", k.getDeskribapena());
            gehitu(doc, kanpina, "kategoria", k.getKategoria());
            gehitu(doc, kanpina, "edukiera", String.valueOf(k.getEdukiera()));
            gehitu(doc, kanpina, "kokalekua", k.getKokalekua());

            Element helbidea = doc.createElement("helbidea");
            gehitu(doc, helbidea, "kalea", k.getHelbidea());
            gehitu(doc, helbidea, "postaKodea", k.getPostakodea());
            Element herria = gehitu(doc, helbidea, "herria", k.getHerriIzena());
            herria.setAttribute("id", String.valueOf(k.getHerriKodea() % 1000));
            Element probintzia = gehitu(doc, helbidea, "probintzia", k.getProbintziaIzena());
            probintzia.setAttribute("id", String.valueOf(k.getProbintziaKodea()));
            kanpina.appendChild(helbidea);

            gehitu(doc, kanpina, "telefonoa", k.getTelefonoa());
            gehitu(doc, kanpina, "emaila", k.getEmaila());
            gehitu(doc, kanpina, "webgunea", k.getWebgunea());

            Element estekak = doc.createElement("estekak");
            gehitu(doc, estekak, "friendly", k.getFriendlyUrl());
            gehitu(doc, estekak, "physical", k.getPhysicalUrl());
            kanpina.appendChild(estekak);

            Element fitxategiak = doc.createElement("fitxategiak");
            gehitu(doc, fitxategiak, "dataXML", k.getDataXml());
            gehitu(doc, fitxategiak, "metadataXML", k.getMetadataXml());
            gehitu(doc, fitxategiak, "zipFile", k.getZipFile());
            kanpina.appendChild(fitxategiak);

            erroa.appendChild(kanpina);
        }

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.transform(new DOMSource(doc), new StreamResult(fitxategia.toFile()));
    }

    private static Element gehitu(Document doc, Element guraso, String izena, String testua) {
        Element e = doc.createElement(izena);
        e.setTextContent(testua == null ? "" : testua);
        guraso.appendChild(e);
        return e;
    }
}