package cl.fersal.inventario.model;

import java.time.LocalDateTime;

public class OrdenTrabajo {

    private Integer id;
    private Integer trabajadorId;
    private Integer usuarioId;
    private LocalDateTime fecha;
    private String tipoTrabajo;
    private String fechaTexto;
    private String nombreTrabajador;
    private String descripcionTrabajo;

    public OrdenTrabajo() {
    }

    public OrdenTrabajo(Integer id, Integer trabajadorId, Integer usuarioId, LocalDateTime fecha, String tipoTrabajo) {
        this.id = id;
        this.trabajadorId = trabajadorId;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.tipoTrabajo = tipoTrabajo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(Integer trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoTrabajo() {
        return tipoTrabajo;
    }

    public void setTipoTrabajo(String tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    public String getFechaTexto() {
        return fechaTexto;
    }

    public void setFechaTexto(String fechaTexto) {
        this.fechaTexto = fechaTexto;
    }

    public String getNombreTrabajador() {
        return nombreTrabajador;
    }

    public void setNombreTrabajador(String nombreTrabajador) {
        this.nombreTrabajador = nombreTrabajador;
    }

    public String getDescripcionTrabajo() {
        return descripcionTrabajo;
    }

    public void setDescripcionTrabajo(String descripcionTrabajo) {
        this.descripcionTrabajo = descripcionTrabajo;
    }
}
