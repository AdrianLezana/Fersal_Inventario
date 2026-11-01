package cl.fersal.inventario.model;

public class Proveedor {

    private Integer id;
    private String rut;
    private String razonSocial;
    private String giro;
    private String telefono;
    private String email;
    private String direccion;
    private Integer usuarioId;

    public Proveedor() {}

    public Proveedor(Integer id, String nombre, String rut, String telefono, Integer usuarioId) {
        this.id = id;
        this.razonSocial = nombre;
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
        this.razonSocial = nombre;
        this.rut = rut;
        this.telefono = telefono;
        this.email = email;
        this.usuarioId = usuarioId;
    }

    public Proveedor(
            Integer id,
            String rut,
            String razonSocial,
            String giro,
            String telefono,
            String email,
            String direccion) {
        this.id = id;
        this.rut = rut;
        this.razonSocial = razonSocial;
        this.giro = giro;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return razonSocial;
    }

    public void setNombre(String nombre) {
        this.razonSocial = nombre;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getGiro() {
        return giro;
    }

    public void setGiro(String giro) {
        this.giro = giro;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return razonSocial
                + (rut == null || rut.isBlank() ? "" : " (" + rut + ")");
    }

}
