package com.blue.apartamentos.models;

import java.time.LocalDateTime;

import com.blue.apartamentos.enums.EstadoHab;
import com.blue.apartamentos.enums.TipoPropiedad;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "propiedades")
public class PropiedadModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_propiedad;

    //private int id_propietario;
    //AJUSTE PARA ENLAZAR CON CLIENTE MODEL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_propietario", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})   
    private ClienteModel propietario;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_propiedad", nullable = false)
    private TipoPropiedad tipo;
    
    @NotBlank
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotBlank
    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @NotBlank
    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @NotBlank
    @Column(name = "ciudad", nullable = false, length = 50)
    private String ciudad;
    
    @NotBlank
    @Column(name = "estado", nullable = false, length = 50)
    private String estidadFederativa;

    @Column(name = "codigo_postal", nullable = false, length = 5)
    private int codigo_postal;

    @NotBlank
    @Column(name = "pais", nullable = false, length = 50)
    private String pais;
    
    @Column(name = "latitud", nullable = false)
    private double latitud;
    
    @Column(name = "longitud", nullable = false)
    private double longitud;
    
    @Positive
    @Column(name = "precio_noche", nullable = false)
    private double precio_noche;

    @Positive
    @Column(name = "capacidad", nullable = false)
    private int capacidad;

    @Positive
    @Column(name = "num_habitaciones", nullable = false)
    private int num_habitaciones;

    @Column(name = "num_banos", nullable = false)
    private int num_banos;
    
    @Column(name = "metro_cuadrados", nullable = false)
    private int metro_cuadrados;

    @Column(name = "comodidades", length = 300)
    private String comodidades;
    
    @Column(name = "reglas", length = 300)
    private String reglas;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_hab", nullable = false)
    private EstadoHab estado = EstadoHab.DISPONIBLE;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fecha_registro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fecha_actualizacion;

    // Callback JPA: antes de insertar
    @PrePersist
    public void onPrePersist() {
        if (this.fecha_registro == null) {
            this.fecha_registro = LocalDateTime.now();
        }
        this.fecha_actualizacion = LocalDateTime.now();
    }

    // Callback JPA: antes de actualizar
    @PreUpdate
    public void onPreUpdate() {
        this.fecha_actualizacion = LocalDateTime.now();
    }

    //Constructor completo
    public PropiedadModel(Long id_propiedad, ClienteModel propietario, TipoPropiedad tipo, String titulo,
            String descripcion, String direccion, String ciudad, String estidadFederativa, int codigo_postal,
            String pais, double latitud, double longitud, double precio_noche, int capacidad, int num_habitaciones,
            int num_banos, int metro_cuadrados, String comodidades, String reglas, EstadoHab estado,
            LocalDateTime fecha_registro, LocalDateTime fecha_actualizacion) {
        this.id_propiedad = id_propiedad;
        this.propietario = propietario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estidadFederativa = estidadFederativa;
        this.codigo_postal = codigo_postal;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
        this.precio_noche = precio_noche;
        this.capacidad = capacidad;
        this.num_habitaciones = num_habitaciones;
        this.num_banos = num_banos;
        this.metro_cuadrados = metro_cuadrados;
        this.comodidades = comodidades;
        this.reglas = reglas;
        this.estado = estado;
        this.fecha_registro = LocalDateTime.now();
        this.fecha_actualizacion = LocalDateTime.now();
    }

    //Constructor sin id_propiedad, fecha_registro y fecha_actualizacion
    public PropiedadModel(ClienteModel propietario, TipoPropiedad tipo, String titulo,
            String descripcion, String direccion, String ciudad, String estidadFederativa, int codigo_postal,
            String pais, double latitud, double longitud, double precio_noche, int capacidad, int num_habitaciones,
            int num_banos, int metro_cuadrados, String comodidades, String reglas, EstadoHab estado) {
        this.propietario = propietario;
        this.tipo = tipo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.estidadFederativa = estidadFederativa;
        this.codigo_postal = codigo_postal;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
        this.precio_noche = precio_noche;
        this.capacidad = capacidad;
        this.num_habitaciones = num_habitaciones;
        this.num_banos = num_banos;
        this.metro_cuadrados = metro_cuadrados;
        this.comodidades = comodidades;
        this.reglas = reglas;
        this.estado = estado;
    }

    //Constructor vacio
    public PropiedadModel() {
    }

    // Constucton con id_propiedad solo
    public PropiedadModel(Long id_propiedad) {
        this.id_propiedad = id_propiedad;
    }

    // Getters and Setters
    public Long getId_propiedad() {
        return id_propiedad;
    }

    public void setId_propiedad(Long id_propiedad) {
        this.id_propiedad = id_propiedad;
    }

    public ClienteModel getPropietario() {
        return propietario;
    }

    public void setPropietario(ClienteModel propietario) {
        this.propietario = propietario;
    }

    public TipoPropiedad getTipo() {
        return tipo;
    }

    public void setTipo(TipoPropiedad tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstidadFederativa() {
        return estidadFederativa;
    }

    public void setEstidadFederativa(String estidadFederativa) {
        this.estidadFederativa = estidadFederativa;
    }

    public int getCodigo_postal() {
        return codigo_postal;
    }

    public void setCodigo_postal(int codigo_postal) {
        this.codigo_postal = codigo_postal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getPrecio_noche() {
        return precio_noche;
    }

    public void setPrecio_noche(double precio_noche) {
        this.precio_noche = precio_noche;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getNum_habitaciones() {
        return num_habitaciones;
    }

    public void setNum_habitaciones(int num_habitaciones) {
        this.num_habitaciones = num_habitaciones;
    }

    public int getNum_banos() {
        return num_banos;
    }

    public void setNum_banos(int num_banos) {
        this.num_banos = num_banos;
    }

    public int getMetro_cuadrados() {
        return metro_cuadrados;
    }

    public void setMetro_cuadrados(int metro_cuadrados) {
        this.metro_cuadrados = metro_cuadrados;
    }

    public String getComodidades() {
        return comodidades;
    }

    public void setComodidades(String comodidades) {
        this.comodidades = comodidades;
    }

    public String getReglas() {
        return reglas;
    }

    public void setReglas(String reglas) {
        this.reglas = reglas;
    }

    public EstadoHab getEstado() {
        return estado;
    }

    public void setEstado(EstadoHab estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha_registro() {
        return fecha_registro;
    }

    public void setFecha_registro(LocalDateTime fecha_registro) {
        this.fecha_registro = fecha_registro;
    }

    public LocalDateTime getFecha_actualizacion() {
        return fecha_actualizacion;
    }

    public void setFecha_actualizacion(LocalDateTime fecha_actualizacion) {
        this.fecha_actualizacion = fecha_actualizacion;
    }


    
}


