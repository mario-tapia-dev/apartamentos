package com.blue.apartamentos.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blue.apartamentos.enums.TipoCliente;
import com.blue.apartamentos.exceptions.RecursoNoEncontradoException;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.repositories.IClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private IClienteRepository clienteRepository;

    public List<ClienteModel> getAllClientes() {
        return clienteRepository.findAll();
    }

    public List<ClienteModel> getClientesByTipo(TipoCliente tipo) {
        return clienteRepository.buscarPorTipo(tipo);
    }

    public Optional<ClienteModel> getClienteById(Long id) {
        return clienteRepository.findById(id);
    }

    public ClienteModel saveCliente(ClienteModel cliente) {
        cliente.setId(null); // al crear nunca se debe sobreescribir un registro existente
        return clienteRepository.saveAndFlush(cliente);
    }

    /** Actualiza solo los campos que vengan en el body (null = no cambiar). */
    @Transactional
    public ClienteModel updateCliente(Long id, ClienteModel d) {
        ClienteModel c = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));

        if (d.getTipo() != null) c.setTipo(d.getTipo());
        if (d.getEstatus() != null) c.setEstatus(d.getEstatus());
        if (d.getNombres() != null) c.setNombres(d.getNombres());
        if (d.getApellidos() != null) c.setApellidos(d.getApellidos());
        if (d.getEmail() != null) c.setEmail(d.getEmail());
        if (d.getTelefono() != null) c.setTelefono(d.getTelefono());
        if (d.getFechaNacimiento() != null) c.setFechaNacimiento(d.getFechaNacimiento());
        if (d.getIne() != null) c.setIne(d.getIne());
        if (d.getDireccion() != null) c.setDireccion(d.getDireccion());
        if (d.getClabe() != null) c.setClabe(d.getClabe().isBlank() ? null : d.getClabe());
        if (d.getPassword() != null && !d.getPassword().isBlank()) c.setPassword(d.getPassword());

        return clienteRepository.saveAndFlush(c);
    }

    public void deleteCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}
