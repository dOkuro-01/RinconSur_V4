package cl.duoc.rinconsur.service;

import cl.duoc.rinconsur.model.Evento;
import cl.duoc.rinconsur.repository.EventoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventoService {
    private final EventoRepository repositorio;
    public EventoService(EventoRepository repositorio) { this.repositorio = repositorio; }
    public Evento registrar(Evento e) { return repositorio.save(new Evento(e.getTipo(), e.getDetalle())); }
    public List<Evento> listar() { return repositorio.findAll(); }
}
