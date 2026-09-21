package cl.fersal.inventario.model;

public class Categoria {
    private Integer id;
    private String nombre;
    private Integer usuarioId; // Trazabilidad

    public Categoria() {}

    public Categoria(String nombre, Integer usuarioId) {
        this.nombre = nombre;
        this.usuarioId = usuarioId;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    // Útil para cuando JavaFX intente mostrar la categoría en un ComboBox
    @Override
    public String toString() {
        return this.nombre;
    }

}