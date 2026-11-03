package cl.duoc.rinconsur.api;

import cl.duoc.rinconsur.model.Evento;
import cl.duoc.rinconsur.service.EventoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventoController {
    private final EventoService servicio;
    public EventoController(EventoService servicio) { this.servicio = servicio; }
    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("estado", "OK", "servicio", "Rincón Sur API"); }
    @PostMapping("/eventos")
    public Evento guardar(@RequestBody Evento evento) { return servicio.registrar(evento); }
    @GetMapping("/eventos")
    public List<Evento> listar() { return servicio.listar(); }
}
