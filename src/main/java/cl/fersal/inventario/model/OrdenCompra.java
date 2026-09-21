package cl.fersal.inventario.model;

import java.time.LocalDateTime;

public class OrdenCompra {

    private Integer id;
    private String folio;
    private Integer proveedorId;
    private Integer usuarioId;
    private LocalDateTime fechaIngreso;

    public OrdenCompra() {
    }

    public OrdenCompra(Integer id, String folio, Integer proveedorId, Integer usuarioId, LocalDateTime fechaIngreso) {
        this.id = id;
        this.folio = folio;
        this.proveedorId = proveedorId;
        this.usuarioId = usuarioId;
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Integer getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(Integer proveedorId) {
        this.proveedorId = proveedorId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

}
