package procureai.supplier;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import procureai.supplier.repository.ProductRepository;
import procureai.supplier.repository.SupplierRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SupplierControllerTests {

    private static final String ATLAS = """
            {"name": "Atlas Informatique", "cin": "BK482913", "ice": "001525874000062",
             "email": "contact@atlas-info.ma", "city": "Casablanca", "status": "ACTIVE"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    private long createAtlas() throws Exception {
        String json = mockMvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content(ATLAS))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.parse(json).read("$.id", Long.class);
    }

    @Test
    void createAndUpdateSupplier() throws Exception {
        long id = createAtlas();

        mockMvc.perform(put("/api/suppliers/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(ATLAS.replace("Casablanca", "Rabat")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.city").value("Rabat"))
                .andExpect(jsonPath("$.ice").value("001525874000062"));
    }

    @Test
    void invalidSupplierReturns400() throws Exception {
        mockMvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content(ATLAS.replace("001525874000062", "12345")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON)
                        .content(ATLAS.replace("\"cin\": \"BK482913\",", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateCinReturns409() throws Exception {
        createAtlas();
        mockMvc.perform(post("/api/suppliers").contentType(MediaType.APPLICATION_JSON).content(ATLAS))
                .andExpect(status().isConflict());
    }

    @Test
    void adminAddsProductToCatalog() throws Exception {
        long id = createAtlas();
        String product = """
                {"name": "Écran Dell 27 pouces", "price": 2490.00, "category": "IT"}
                """;

        mockMvc.perform(post("/api/suppliers/" + id + "/products").header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/suppliers/" + id + "/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON).content(product))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplierId").value(id))
                .andExpect(jsonPath("$.price").value(2490.00));

        mockMvc.perform(get("/api/suppliers/" + id + "/products"))
                .andExpect(jsonPath("$[0].name").value("Écran Dell 27 pouces"))
                .andExpect(jsonPath("$[0].supplier").doesNotExist());
    }
}
