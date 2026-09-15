package com.ecogo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecogo.demo.model.entity.Estacion;

import java.util.List;

public interface EstacionRepository extends JpaRepository<Estacion, Long> {

    List<Estacion> findByCiudadIgnoreCase(String ciudad);

    List<Estacion> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByIdExterno(Integer idExterno);
}
