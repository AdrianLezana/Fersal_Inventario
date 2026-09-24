package cl.fersal.inventario.model;

public class DetalleOrdenTrabajo {

    private Integer id;
    private Integer ordenTrabajoId;
    private Integer productoId;
    private Double cantidadUtilizada;
    private String nombreProducto;
    private String unidadMedida;

    public DetalleOrdenTrabajo() {
    }

    public DetalleOrdenTrabajo(Integer id, Integer ordenTrabajoId, Integer productoId, Double cantidadUtilizada) {
        this.id = id;
        this.ordenTrabajoId = ordenTrabajoId;
        this.productoId = productoId;
        this.cantidadUtilizada = cantidadUtilizada;
    }

    public DetalleOrdenTrabajo(
            Integer productoId,
            String nombreProducto,
            String unidadMedida,
            Double cantidadUtilizada) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.unidadMedida = unidadMedida;
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

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}
