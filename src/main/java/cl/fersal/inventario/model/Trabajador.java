package cl.fersal.inventario.model;

public class Trabajador {

    private Integer id;
    private String nombre;
    private Integer activo;
    private Integer usuarioId;

    public Trabajador() {}

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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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
}
