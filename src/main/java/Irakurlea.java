
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Irakurlea {


    public static List<Bezeroa> leerBezeroak(String ruta) throws Exception {
        List<Bezeroa> lista = new ArrayList<>();

        NodeList nodos = abrirXml(ruta).getElementsByTagName("row");

        for (int i = 0; i < nodos.getLength(); i++) {
            Element e = (Element) nodos.item(i);

            Bezeroa b = new Bezeroa();
            b.setId(Integer.parseInt(getTexto(e, "id")));
            b.setIzena(getTexto(e, "izena"));
            b.setAbizena(getTexto(e, "abizena"));
            b.setEmail(getTexto(e, "emaila"));
            b.setTelefonoa(getTexto(e, "telefonoa"));
            b.setOstatu_mota(getTexto(e, "ostatu_mota"));
            b.setSortzeEguna(getTexto(e, "sortzeEguna"));

            lista.add(b);
        }
        return lista;
    }

    public static List<Kanpina> leerKanpinak(String ruta) throws Exception {
        List<Kanpina> lista = new ArrayList<>();

        NodeList nodos = abrirXml(ruta).getElementsByTagName("row");

        for (int i = 0; i < nodos.getLength(); i++) {
            Element e = (Element) nodos.item(i);

            Kanpina k = new Kanpina();
            k.setKodea(getTexto(e, "signatura"));
            k.setIzena(getTexto(e, "documentName"));
            k.setDeskribapena(recortar(getTexto(e, "documentDescription"), 255));
            k.setKokalekua(getTexto(e, "country"));

            String tel = getTexto(e, "phone");
            k.setTelefonoa(tel == null ? null : tel.replace(" ", ""));

            k.setHelbidea(getTexto(e, "address"));
            k.setEmaila(getTexto(e, "tourismEmail"));
            k.setWebgunea(getTexto(e, "web"));
            k.setKategoria(getTexto(e, "category"));
            k.setEdukiera(Integer.parseInt(getTexto(e, "capacity")));
            k.setPostakodea(getTexto(e, "postalCode"));
            int probKodea = Integer.parseInt(getTexto(e, "territorycode"));
            int herriKodea = Integer.parseInt(getTexto(e, "municipalitycode"));
            // Herri-kodeak ez dira bakarrak probintzien artean (051, 063...): INE kodea erabiltzen da (probintzia*1000 + herria)
            k.setHerriKodea(probKodea * 1000 + herriKodea);
            k.setProbintziaIzena(getTexto(e, "territory"));
            k.setHerriIzena(getTexto(e, "municipality"));
            k.setProbintziaKodea(probKodea);
            k.setFriendlyUrl(getTexto(e, "friendlyUrl"));
            k.setPhysicalUrl(getTexto(e, "physicalUrl"));
            k.setDataXml(getTexto(e, "dataXML"));
            k.setMetadataXml(getTexto(e, "metadataXML"));
            k.setZipFile(getTexto(e, "zipFile"));

            lista.add(k);
        }
        return lista;
    }

   
    public static void guardarBezeroak(Connection con, List<Bezeroa> lista) throws Exception {
        String sql = "INSERT INTO BEZEROAK " +
                     "(id, izena, abizenak, emaila, telefonoa, ostatu_mota, sortze_eguna) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Bezeroa b : lista) {
                ps.setInt(1, b.getId());
                ps.setString(2, b.getIzena());
                ps.setString(3, b.getAbizena());
                ps.setString(4, b.getEmail());
                ps.setString(5, b.getTelefonoa());
                ps.setString(6, b.getOstatu_mota());
                ps.setDate(7, java.sql.Date.valueOf(b.getSortzeEguna()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void guardarProbintziak(Connection con, List<Kanpina> lista) throws Exception {
        java.util.Map<Integer, String> probintziak = new java.util.LinkedHashMap<>();
        for (Kanpina k : lista) {
            probintziak.putIfAbsent(k.getProbintziaKodea(), k.getProbintziaIzena());
        }
        String sql = "MERGE INTO probintziak p USING (SELECT ? AS kodea, ? AS izena FROM dual) s " +
                     "ON (p.kodea = s.kodea) " +
                     "WHEN NOT MATCHED THEN INSERT (kodea, izena) VALUES (s.kodea, s.izena)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (java.util.Map.Entry<Integer, String> p : probintziak.entrySet()) {
                ps.setInt(1, p.getKey());
                ps.setString(2, p.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void guardarHerriak(Connection con, List<Kanpina> lista) throws Exception {
        java.util.Map<Integer, String> herriak = new java.util.LinkedHashMap<>();
        for (Kanpina k : lista) {
            herriak.putIfAbsent(k.getHerriKodea(), k.getHerriIzena());
        }
        String sql = "MERGE INTO herriak h USING (SELECT ? AS kodea, ? AS izena FROM dual) s " +
                     "ON (h.kodea = s.kodea) " +
                     "WHEN NOT MATCHED THEN INSERT (kodea, izena) VALUES (s.kodea, s.izena)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (java.util.Map.Entry<Integer, String> h : herriak.entrySet()) {
                ps.setInt(1, h.getKey());
                ps.setString(2, h.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public static void guardarKanpinak(Connection con, List<Kanpina> lista) throws Exception {
        String sql = "INSERT INTO kanpinak (kodea, izena, deskribapena, kokalekua, telefonoa, " +
                     "helbidea, emaila, webgunea, kategoria, edukiera, postakodea, herri_kodea, " +
                     "probintzia_kodea, friendly_url, physical_url, data_xml, metadata_xml, zip_file) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Kanpina k : lista) {
                ps.setString(1, k.getKodea());
                ps.setString(2, k.getIzena());
                ps.setString(3, k.getDeskribapena());
                ps.setString(4, k.getKokalekua());
                ps.setString(5, k.getTelefonoa());
                ps.setString(6, k.getHelbidea());
                ps.setString(7, k.getEmaila());
                ps.setString(8, k.getWebgunea());
                ps.setString(9, k.getKategoria());
                ps.setInt(10, k.getEdukiera());
                ps.setString(11, k.getPostakodea());
                ps.setInt(12, k.getHerriKodea());
                ps.setInt(13, k.getProbintziaKodea());
                ps.setString(14, k.getFriendlyUrl());
                ps.setString(15, k.getPhysicalUrl());
                ps.setString(16, k.getDataXml());
                ps.setString(17, k.getMetadataXml());
                ps.setString(18, k.getZipFile());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

  

    private static Document abrirXml(String ruta) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(ruta));
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static String getTexto(Element padre, String etiqueta) {
        NodeList nl = padre.getElementsByTagName(etiqueta);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent().trim();
    }

    private static String recortar(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max) : s;
    }
}