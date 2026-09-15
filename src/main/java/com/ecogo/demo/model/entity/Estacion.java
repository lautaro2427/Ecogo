package com.ecogo.demo.model.entity;
import com.ecogo.demo.model.enums.TipoConector;
import com.ecogo.demo.model.enums.EstadoEstacion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estacion")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Estacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String ciudad;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    private Double potenciaKw;

    @Enumerated(EnumType.STRING)
    private TipoConector tipoConector;

    @Enumerated(EnumType.STRING)
    private EstadoEstacion estado = EstadoEstacion.DESCONOCIDO;

    @Column(unique = true)
    private Integer idExterno;
}