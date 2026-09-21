package cl.fersal.inventario.model;

public class Usuario {
    private Integer id;
    private String username;
    private String passwordHash;

    // Constructor vacío (Requerido por los DAOs para instanciar y luego llenar)
    public Usuario() {}

    // Constructor completo (Para crear un usuario nuevo antes de insertarlo)
    public Usuario(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}