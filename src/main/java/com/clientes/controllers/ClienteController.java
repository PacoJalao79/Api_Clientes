package com.clientes.controllers;

import com.clientes.dto.ClienteDTO;
import com.clientes.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @PostMapping
    public ResponseEntity<ClienteDTO> crear(@RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(service.guardar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

   @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Integer id) {
        return service.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> actualizar(@PathVariable Integer id, @RequestBody ClienteDTO dto) {
        return service.actualizar(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        return service.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }


        //METODO HATEOAS para buscar por ID
    @GetMapping("/hateoas/{id}")
    public ResponseEntity<ClienteDTO> obtenerHATEOAS(@PathVariable Integer id) {
        return service.obtenerPorId(id)
            .map(dto -> {
                // Agregar los links HATEOAS
                dto.add(linkTo(methodOn(ClienteController.class).obtenerHATEOAS(id)).withSelfRel());
                dto.add(linkTo(methodOn(ClienteController.class).obtenerTodosHATEOAS()).withRel("todos"));
                dto.add(linkTo(methodOn(ClienteController.class).eliminar(id)).withRel("eliminar"));

                dto.add(Link.of("http://localhost:8888/api/proxy/productos/" + dto.getIdCliente()).withSelfRel());
                dto.add(Link.of("http://localhost:8888/api/proxy/productos/" + dto.getIdCliente()).withRel("Modificar HATEOAS").withType("PUT"));
                dto.add(Link.of("http://localhost:8888/api/proxy/productos/" + dto.getIdCliente()).withRel("Eliminar HATEOAS").withType("DELETE"));

                return ResponseEntity.ok(dto);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
}

    //METODO HATEOAS para listar todos los productos utilizando HATEOAS
    @GetMapping("/hateoas")
    public List<ClienteDTO> obtenerTodosHATEOAS() {
        List<ClienteDTO> lista = service.listar();

        for (ClienteDTO dto : lista) {
            //link url de la misma API
            dto.add(linkTo(methodOn(ClienteController.class).obtenerHATEOAS(dto.getIdCliente())).withSelfRel());

            //link HATEOAS para API Gateway "A mano"
            dto.add(Link.of("http://localhost:8888/api/proxy/productos").withRel("Get todos HATEOAS"));
            dto.add(Link.of("http://localhost:8888/api/proxy/productos/" + dto.getIdCliente()).withRel("Crear HATEOAS").withType("POST"));
        }

        return lista;
    }

}