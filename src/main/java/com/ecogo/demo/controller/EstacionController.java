package com.ecogo.demo.controller;

import com.ecogo.demo.model.entity.Estacion;
import com.ecogo.demo.service.EstacionService;
import com.ecogo.demo.service.OcmImportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionController {

    private final EstacionService service;
    private final OcmImportService ocmImportService;

    public EstacionController(EstacionService service, OcmImportService ocmImportService) {
        this.service = service;
        this.ocmImportService = ocmImportService;
    }

    @GetMapping
    public List<Estacion> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Estacion obtener(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    // TEMPORAL: sacar o proteger cuando esté Spring Security
    @PostMapping("/importar")
    public String importar() {
        return "Estaciones importadas: " + ocmImportService.importarArgentina();
    }
}