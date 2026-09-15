package com.ecogo.demo.service;

import com.ecogo.demo.dto.OcmPoi;
import com.ecogo.demo.model.entity.Estacion;
import com.ecogo.demo.model.enums.EstadoEstacion;
import com.ecogo.demo.model.enums.TipoConector;
import com.ecogo.demo.repository.EstacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;

@Service
public class OcmImportService {

    private final EstacionRepository repository;
    private final RestClient restClient;
    private final String apiKey;

    public OcmImportService(EstacionRepository repository,
                            @Value("${ocm.api.key}") String apiKey) {
        this.repository = repository;
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://api.openchargemap.io");
    }

    public int importarArgentina() {
        List<OcmPoi> pois = restClient.get()
            .uri(uri -> uri.path("/v3/poi")
                .queryParam("countrycode", "AR")
                .queryParam("maxresults", 2000)
                .queryParam("compact", false)
                .queryParam("key", apiKey)
                .build())
            .retrieve()
            .body(new ParameterizedTypeReference<List<OcmPoi>>() {});

        if (pois == null) return 0;

        int guardadas = 0;
        for (OcmPoi poi : pois) {
            if (!esValido(poi)) continue;
            if (repository.existsByIdExterno(poi.id())) continue;
            repository.save(mapear(poi));
            guardadas++;
        }
        return guardadas;
    }

    private boolean esValido(OcmPoi poi) {
        return poi.id() != null
            && poi.addressInfo() != null
            && poi.addressInfo().latitude() != null
            && poi.addressInfo().longitude() != null;
    }

    private Estacion mapear(OcmPoi poi) {
        var info = poi.addressInfo();
        var estacion = new Estacion();

        estacion.setIdExterno(poi.id());
        estacion.setNombre(info.title() != null ? info.title() : "Sin nombre");
        estacion.setDireccion(info.addressLine1());
        estacion.setCiudad(resolverCiudad(info));
        estacion.setLatitud(info.latitude());
        estacion.setLongitud(info.longitude());

        var mejor = mejorConexion(poi);
        if (mejor != null) {
            estacion.setPotenciaKw(mejor.powerKw());
            estacion.setTipoConector(mapearConector(mejor.connectionTypeId()));
        } else {
            estacion.setTipoConector(TipoConector.OTRO);
        }

        boolean operativa = poi.statusType() != null
            && Boolean.TRUE.equals(poi.statusType().isOperational());
        estacion.setEstado(operativa
            ? EstadoEstacion.DESCONOCIDO
            : EstadoEstacion.FUERA_DE_SERVICIO);

        return estacion;
    }

    private String resolverCiudad(OcmPoi.AddressInfo info) {
        if (tieneTexto(info.town())) return info.town().trim();
        if (tieneTexto(info.stateOrProvince())) return info.stateOrProvince().trim();
        if (tieneTexto(info.addressLine1())) {
            String[] partes = info.addressLine1().split(",");
            if (partes.length >= 2) return partes[1].trim();
        }
        return null;
    }

    private boolean tieneTexto(String s) {
        return s != null && !s.isBlank();
    }

    private OcmPoi.Connection mejorConexion(OcmPoi poi) {
        if (poi.connections() == null) return null;
        return poi.connections().stream()
            .filter(c -> c.powerKw() != null)
            .max(Comparator.comparing(OcmPoi.Connection::powerKw))
            .orElse(poi.connections().isEmpty() ? null : poi.connections().get(0));
    }

    private TipoConector mapearConector(Integer id) {
        if (id == null) return TipoConector.OTRO;
        return switch (id) {
            case 25, 1036 -> TipoConector.TIPO2;
            case 32, 33 -> TipoConector.CCS;
            case 2 -> TipoConector.CHADEMO;
            case 28 -> TipoConector.SCHUKO;
            default -> TipoConector.OTRO;
        };
    }
}