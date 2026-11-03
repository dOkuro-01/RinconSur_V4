package cl.duoc.rinconsur.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "eventos")
public class Evento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tipo;
    @Column(length = 500) private String detalle;
    private Instant fecha = Instant.now();

    public Evento() {}
    public Evento(String tipo, String detalle) { this.tipo = tipo; this.detalle = detalle; }
    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
}
