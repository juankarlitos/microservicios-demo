package cl.inostratech.productos;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @GetMapping
    public List<String> listar() {
        return List.of("Notebook", "Mouse", "Teclado");
    }
}