package cl.fersal.inventario.model;

public class Proveedor {

    private Integer id;
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private Integer usuarioId; // Trazabilidad

    public Proveedor() {}

    public Proveedor(Integer id, String nombre, String rut, String telefono, Integer usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.telefono = telefono;
        this.usuarioId = usuarioId;
    }

    public Proveedor(
            Integer id,
            String nombre,
            String rut,
            String telefono,
            String email,
            Integer usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.telefono = telefono;
        this.email = email;
        this.usuarioId = usuarioId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return nombre + (rut == null || rut.isBlank() ? "" : " (" + rut + ")");
    }

}
