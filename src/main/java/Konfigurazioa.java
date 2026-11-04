
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Konfigurazioa {
	
    private final Properties props = new Properties();
    public Konfigurazioa(String ruta) throws IOException {
        try (FileInputStream in = new FileInputStream(ruta)) {
            props.load(in);
        }
    }

    public String getUrl()         { return props.getProperty("url"); }
    public String getUser()        { return props.getProperty("erabiltzailea"); }
    public String getPassword()    { return props.getProperty("pasahitza"); }
    public String getXmlBezeroak() { return props.getProperty("bezeroak"); }
    public String getXmlKanpinak() { return props.getProperty("kanpinak"); }
}