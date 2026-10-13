package procureai.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import procureai.supplier.entity.Product;
import procureai.supplier.entity.Supplier;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByStatus(String status);

    Optional<Supplier> findByCin(String cin);

    boolean existsByCin(String cin);

}
