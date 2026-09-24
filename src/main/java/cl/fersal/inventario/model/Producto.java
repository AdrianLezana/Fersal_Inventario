package cl.fersal.inventario.model;

public class Producto {
    private Integer id;
    private String codigoInterno;
    private Integer categoriaId;
    private String nombre;
    private String dimensiones;
    private String unidadMedida;
    private String ubicacion;
    private Double stockActual;
    private Double precioVenta;
    private Double precioVentaMetro;
    private Double precioTrabajadoMetro;
    private Double costoPromedio;
    private Integer usuarioId; // Trazabilidad

    public Producto() {}

    public Producto(Integer id, String codigoInterno, Integer categoriaId, String nombre, String dimensiones, String unidadMedida, String ubicacion, Double stockActual, Double precioVenta, Double precioVentaMetro, Double precioTrabajadoMetro, Double costoPromedio, Integer usuarioId) {
        this.id = id;
        this.codigoInterno = codigoInterno;
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.dimensiones = dimensiones;
        this.unidadMedida = unidadMedida;
        this.ubicacion = ubicacion;
        this.stockActual = stockActual;
        this.precioVenta = precioVenta;
        this.precioVentaMetro = precioVentaMetro;
        this.precioTrabajadoMetro = precioTrabajadoMetro;
        this.costoPromedio = costoPromedio;
        this.usuarioId = usuarioId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDimensiones() {
        return dimensiones;
    }

    public void setDimensiones(String dimensiones) {
        this.dimensiones = dimensiones;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Double getStockActual() {
        return stockActual;
    }

    public void setStockActual(Double stockActual) {
        this.stockActual = stockActual;
    }

    public Double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(Double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Double getPrecioVentaMetro() {
        return precioVentaMetro;
    }

    public void setPrecioVentaMetro(Double precioVentaMetro) {
        this.precioVentaMetro = precioVentaMetro;
    }

    public Double getPrecioTrabajadoMetro() {
        return precioTrabajadoMetro;
    }

    public void setPrecioTrabajadoMetro(Double precioTrabajadoMetro) {
        this.precioTrabajadoMetro = precioTrabajadoMetro;
    }

    public Double getCostoPromedio() {
        return costoPromedio;
    }

    public void setCostoPromedio(Double costoPromedio) {
        this.costoPromedio = costoPromedio;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return (codigoInterno == null || codigoInterno.isBlank()
                ? ""
                : codigoInterno + " - ")
                + nombre
                + " (Stock: " + (stockActual == null ? 0.0 : stockActual) + ")";
    }
}