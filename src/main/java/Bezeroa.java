

public class Bezeroa {
	private int id;
	private String izena;
	private String abizena;
	private String email;
	private String telefonoa;
	private String ostatu_mota;
	private String sortzeEguna;
	
	
	public Bezeroa(int id, String izena, String abizena, String email, String telefonoa, String ostatu_mota,
			String sortzeEguna) {
		
		this.id = id;
		this.izena = izena;
		this.abizena = abizena;
		this.email = email;
		this.telefonoa = telefonoa;
		this.ostatu_mota = ostatu_mota;
		this.sortzeEguna = sortzeEguna;
	}
	
	public Bezeroa() {}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getIzena() {
		return izena;
	}


	public void setIzena(String izena) {
		this.izena = izena;
	}


	public String getAbizena() {
		return abizena;
	}


	public void setAbizena(String abizena) {
		this.abizena = abizena;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getTelefonoa() {
		return telefonoa;
	}


	public void setTelefonoa(String telefonoa) {
		this.telefonoa = telefonoa;
	}


	public String getOstatu_mota() {
		return ostatu_mota;
	}


	public void setOstatu_mota(String ostatu_mota) {
		this.ostatu_mota = ostatu_mota;
	}


	public String getSortzeEguna() {
		return sortzeEguna;
	}


	public void setSortzeEguna(String sortzeEguna) {
		this.sortzeEguna = sortzeEguna;
	}


	@Override
	public String toString() {
		return "Bezeroa [id=" + id + ", izena=" + izena + ", abizena=" + abizena + ", email=" + email + ", telefonoa="
				+ telefonoa + ", ostatu_mota=" + ostatu_mota + ", sortzeEguna=" + sortzeEguna + "]";
	}
	
	
	

}
