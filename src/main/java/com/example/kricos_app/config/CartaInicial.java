package com.example.kricos_app.config;

import com.example.kricos_app.model.Plato;
import com.example.kricos_app.repository.PlatoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Component
public class CartaInicial implements CommandLineRunner {

    private final PlatoRepository platoRepository;

    public CartaInicial(PlatoRepository platoRepository) {
        this.platoRepository = platoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/carta.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            boolean cabecera = true;
            int count = 0;
            while ((linea = reader.readLine()) != null) {
                if (cabecera) {
                    cabecera = false;
                    continue;
                }
                if (linea.isBlank()) continue;
                String[] partes = parseCsvLine(linea);
                if (partes.length >= 5) {
                    String categoria = partes[0].trim();
                    String nombre = partes[1].trim();
                    BigDecimal precio = new BigDecimal(partes[2].trim());
                    String descripcion = partes[3].trim();
                    boolean disponible = Boolean.parseBoolean(partes[4].trim());

                    Plato plato = new Plato(null, categoria, nombre, precio, descripcion, disponible);
                    platoRepository.save(plato);
                    count++;
                }
            }
            System.out.println("Carta cargada: " + count + " platos.");
        }
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().replaceAll("^\"|\"$", ""));
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().replaceAll("^\"|\"$", ""));
        return tokens.toArray(new String[0]);
    }
}
