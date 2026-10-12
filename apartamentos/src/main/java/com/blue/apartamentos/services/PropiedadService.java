package com.blue.apartamentos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.enums.TipoCliente;
import com.blue.apartamentos.enums.TipoPropiedad;
import com.blue.apartamentos.exceptions.RecursoNoEncontradoException;
import com.blue.apartamentos.exceptions.ReglaNegocioException;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.models.PropiedadModel;
import com.blue.apartamentos.repositories.IClienteRepository;
import com.blue.apartamentos.repositories.IDetallePropiedadRepository;
import com.blue.apartamentos.repositories.IPropiedadRepository;

@Service
public class PropiedadService {

    @Autowired
    private IPropiedadRepository propiedadRepository;

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private IDetallePropiedadRepository detalleRepository;

    public List<PropiedadModel> getAllPropiedades() {
        return propiedadRepository.findAll();
    }

    public Optional<PropiedadModel> getPropiedadById(Long id) {
        return propiedadRepository.findById(id);
    }

    public List<PropiedadModel> getPropiedadesByTipo(TipoPropiedad tipo) {
        return propiedadRepository.buscarPorTipo(tipo);
    }

    public List<PropiedadModel> getPropiedadesByPropietario(Long idPropietario) {
        if (!clienteRepository.existsById(idPropietario)) {
            throw new RecursoNoEncontradoException("Propietario", idPropietario);
        }
        return propiedadRepository.buscarPorPropietario(idPropietario);
    }

    @Transactional
    public PropiedadModel savePropiedad(PropiedadModel propiedad) {
        propiedad.setId_propiedad(null);
        propiedad.setPropietario(resolverPropietario(propiedad.getPropietario()));
        return propiedadRepository.saveAndFlush(propiedad);
    }

    @Transactional
    public PropiedadModel updatePropiedad(Long id, PropiedadModel d) {
        PropiedadModel p = propiedadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Propiedad", id));

        if (d.getPropietario() != null) p.setPropietario(resolverPropietario(d.getPropietario()));
        p.setTipo(d.getTipo());
        p.setTitulo(d.getTitulo());
        p.setDescripcion(d.getDescripcion());
        p.setDireccion(d.getDireccion());
        p.setCiudad(d.getCiudad());
        p.setEstidadFederativa(d.getEstidadFederativa());
        p.setCodigo_postal(d.getCodigo_postal());
        p.setPais(d.getPais());
        p.setLatitud(d.getLatitud());
        p.setLongitud(d.getLongitud());
        p.setPrecio_noche(d.getPrecio_noche());
        p.setCapacidad(d.getCapacidad());
        p.setNum_habitaciones(d.getNum_habitaciones());
        p.setNum_banos(d.getNum_banos());
        p.setMetro_cuadrados(d.getMetro_cuadrados());
        p.setComodidades(d.getComodidades());
        p.setReglas(d.getReglas());
        p.setEstado(d.getEstado());
        return propiedadRepository.saveAndFlush(p);
    }

    @Transactional
    public void deletePropiedad(Long id) {
        if (!propiedadRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Propiedad", id);
        }
        detalleRepository.eliminarPorPropiedad(id); // primero las habitaciones
        propiedadRepository.deleteById(id);
        propiedadRepository.flush(); // si tiene reservaciones, falla aquí con 409
    }

    /** El propietario debe existir y ser PROPIETARIO o EMPRESA. */
    private ClienteModel resolverPropietario(ClienteModel ref) {
        if (ref == null || ref.getId() == null) {
            throw new ReglaNegocioException("Debes indicar el propietario: \"propietario\": {\"id\": 1}");
        }
        ClienteModel propietario = clienteRepository.findById(ref.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Propietario", ref.getId()));
        if (propietario.getTipo() == TipoCliente.CLIENTE) {
            throw new ReglaNegocioException(
                    "El cliente " + propietario.getId() + " es de tipo CLIENTE; para ser dueño de una propiedad "
                            + "debe ser PROPIETARIO o EMPRESA");
        }
        return propietario;
    }
}
