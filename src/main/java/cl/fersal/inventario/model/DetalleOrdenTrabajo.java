package cl.fersal.inventario.model;

public class DetalleOrdenTrabajo {

    private Integer id;
    private Integer ordenTrabajoId;
    private Integer productoId;
    private Double cantidadUtilizada;

    public DetalleOrdenTrabajo() {
    }

    public DetalleOrdenTrabajo(Integer id, Integer ordenTrabajoId, Integer productoId, Double cantidadUtilizada) {
        this.id = id;
        this.ordenTrabajoId = ordenTrabajoId;
        this.productoId = productoId;
        this.cantidadUtilizada = cantidadUtilizada;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrdenTrabajoId() {
        return ordenTrabajoId;
    }

    public void setOrdenTrabajoId(Integer ordenTrabajoId) {
        this.ordenTrabajoId = ordenTrabajoId;
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public Double getCantidadUtilizada() {
        return cantidadUtilizada;
    }

    public void setCantidadUtilizada(Double cantidadUtilizada) {
        this.cantidadUtilizada = cantidadUtilizada;
    }
}
