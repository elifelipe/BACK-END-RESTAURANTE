package br.com.elftech.elftech.config;

import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.RestauranteRepository;
import br.com.elftech.elftech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Variável para guardar a referência do nosso restaurante de teste
        Restaurante restauranteTeste;

        // Bloco para criar ou carregar o restaurante de teste
        if (restauranteRepository.count() == 0) {
            restauranteTeste = new Restaurante();
            restauranteTeste.setNome("Praça da Rippa");
            restauranteTeste.setCnpj("12345678000199");
            restauranteRepository.save(restauranteTeste);
            System.out.println(">>> Restaurante de teste criado com sucesso! ID: " + restauranteTeste.getId());
        } else {
            restauranteTeste = restauranteRepository.findAll().get(0);
            System.out.println(">>> Restaurante de teste já existe. Use o ID: " + restauranteTeste.getId());
        }

        // Bloco para criar o usuário de teste, se ele não existir
        if (usuarioRepository.findByLogin("admin").isEmpty()) {
            Usuario adminUser = new Usuario();
            adminUser.setLogin("admin");
            adminUser.setSenha(passwordEncoder.encode("123456"));

            // --- CORREÇÃO IMPORTANTE AQUI ---
            // Associamos o usuário 'admin' ao restaurante de teste
            adminUser.setRestaurante(restauranteTeste);

            usuarioRepository.save(adminUser);
            System.out.println(">>> Usuário 'admin' criado e associado ao restaurante 'Praça da Rippa'.");
        }
    }
}