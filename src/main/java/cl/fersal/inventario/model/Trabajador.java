package cl.fersal.inventario.model;

public class Trabajador {

    private Integer id;
    private String rut;
    private String nombre;
    private String cargo;
    private Integer activo;
    private Integer usuarioId;

    public Trabajador() {}

    public Trabajador(Integer id, String rut, String nombre, String cargo) {
        this.id = id;
        this.rut = rut;
        this.nombre = nombre;
        this.cargo = cargo;
    }

    public Trabajador(Integer usuarioId, Integer activo, String nombre, Integer id) {
        this.usuarioId = usuarioId;
        this.activo = activo;
        this.nombre = nombre;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return nombre + (cargo == null || cargo.isBlank() ? "" : " - " + cargo)
                + (rut == null || rut.isBlank() ? "" : " (" + rut + ")");
    }
}
