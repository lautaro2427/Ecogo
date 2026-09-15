package com.ecogo.demo.service;

import org.springframework.stereotype.Service;

import com.ecogo.demo.model.entity.Estacion;
import com.ecogo.demo.repository.EstacionRepository;

import java.util.List;

@Service
public class EstacionService {

    private final EstacionRepository repository;

    public EstacionService(EstacionRepository repository) {
        this.repository = repository;
    }

    public List<Estacion> listarTodas() {
        return repository.findAll();
    }

    public Estacion buscarPorId(Long id) {
        return repository.findById(id)
            .orElseThrow();
    }
}