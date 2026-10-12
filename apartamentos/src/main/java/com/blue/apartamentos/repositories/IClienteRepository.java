package com.blue.apartamentos.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blue.apartamentos.enums.TipoCliente;
import com.blue.apartamentos.models.ClienteModel;

@Repository
public interface IClienteRepository extends JpaRepository<ClienteModel, Long> {

    @Query("SELECT c FROM ClienteModel c WHERE c.tipo = :tipo ORDER BY c.apellidos, c.nombres")
    List<ClienteModel> buscarPorTipo(@Param("tipo") TipoCliente tipo);
}
