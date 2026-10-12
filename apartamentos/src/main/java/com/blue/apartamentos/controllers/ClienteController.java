package com.blue.apartamentos.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.blue.apartamentos.enums.TipoCliente;
import com.blue.apartamentos.models.ClienteModel;
import com.blue.apartamentos.services.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Obtener todos los clientes (opcional: /api/clientes?tipo=PROPIETARIO)
    @GetMapping
    public List<ClienteModel> getAllClientes(@RequestParam(required = false) TipoCliente tipo) {
        return (tipo == null) ? clienteService.getAllClientes() : clienteService.getClientesByTipo(tipo);
    }

    // Obtener un cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteModel> getClienteById(@PathVariable Long id) {
        Optional<ClienteModel> cliente = clienteService.getClienteById(id);
        return cliente.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo cliente
    @PostMapping
    public ResponseEntity<ClienteModel> createCliente(@Valid @RequestBody ClienteModel cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.saveCliente(cliente));
    }

    // Actualizar un cliente existente (solo cambia los campos que se manden)
    @PutMapping("/{id}")
    public ClienteModel updateCliente(@PathVariable Long id, @RequestBody ClienteModel clienteDetails) {
        return clienteService.updateCliente(id, clienteDetails);
    }

    // Eliminar un cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        Optional<ClienteModel> cliente = clienteService.getClienteById(id);
        if (cliente.isPresent()) {
            clienteService.deleteCliente(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
