

public class Kanpina {
    private String kodea;
    private String izena;
    private String deskribapena;
    private String kokalekua;
    private String telefonoa;
    private String helbidea;
    private String emaila;
    private String webgunea;
    private String kategoria;
    private int edukiera;
    private String postakodea;
    private int herriKodea;
    private int probintziaKodea;
    private String friendlyUrl;
    private String physicalUrl;
    private String dataXml;
    private String metadataXml;
    private String zipFile;

    public Kanpina() {}

    public String getKodea() { return kodea; }
    public void setKodea(String kodea) { this.kodea = kodea; }
    public String getIzena() { return izena; }
    public void setIzena(String izena) { this.izena = izena; }
    public String getDeskribapena() { return deskribapena; }
    public void setDeskribapena(String deskribapena) { this.deskribapena = deskribapena; }
    public String getKokalekua() { return kokalekua; }
    public void setKokalekua(String kokalekua) { this.kokalekua = kokalekua; }
    public String getTelefonoa() { return telefonoa; }
    public void setTelefonoa(String telefonoa) { this.telefonoa = telefonoa; }
    public String getHelbidea() { return helbidea; }
    public void setHelbidea(String helbidea) { this.helbidea = helbidea; }
    public String getEmaila() { return emaila; }
    public void setEmaila(String emaila) { this.emaila = emaila; }
    public String getWebgunea() { return webgunea; }
    public void setWebgunea(String webgunea) { this.webgunea = webgunea; }
    public String getKategoria() { return kategoria; }
    public void setKategoria(String kategoria) { this.kategoria = kategoria; }
    public int getEdukiera() { return edukiera; }
    public void setEdukiera(int edukiera) { this.edukiera = edukiera; }
    public String getPostakodea() { return postakodea; }
    public void setPostakodea(String postakodea) { this.postakodea = postakodea; }
    public int getHerriKodea() { return herriKodea; }
    public void setHerriKodea(int herriKodea) { this.herriKodea = herriKodea; }
    public int getProbintziaKodea() { return probintziaKodea; }
    public void setProbintziaKodea(int probintziaKodea) { this.probintziaKodea = probintziaKodea; }
    public String getFriendlyUrl() { return friendlyUrl; }
    public void setFriendlyUrl(String friendlyUrl) { this.friendlyUrl = friendlyUrl; }
    public String getPhysicalUrl() { return physicalUrl; }
    public void setPhysicalUrl(String physicalUrl) { this.physicalUrl = physicalUrl; }
    public String getDataXml() { return dataXml; }
    public void setDataXml(String dataXml) { this.dataXml = dataXml; }
    public String getMetadataXml() { return metadataXml; }
    public void setMetadataXml(String metadataXml) { this.metadataXml = metadataXml; }
    public String getZipFile() { return zipFile; }
    public void setZipFile(String zipFile) { this.zipFile = zipFile; }

    @Override
    public String toString() {
        return "Kanpina [kodea=" + kodea + ", izena=" + izena + ", helbidea=" + helbidea + "]";
    }
}
