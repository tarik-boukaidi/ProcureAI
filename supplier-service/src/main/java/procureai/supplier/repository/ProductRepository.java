package procureai.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import procureai.supplier.entity.Product;

import java.util.List;
import java.util.Optional;

// "Supplier_Id" et pas "SupplierId" : Product a aussi un getter getSupplierId() (pour le JSON),
// le "_" force Spring Data a passer par la relation supplier -> id
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySupplier_Id(Long supplierId);

    // Le produit doit appartenir a ce fournisseur, sinon on le considere introuvable
    Optional<Product> findByIdAndSupplier_Id(Long id, Long supplierId);
}
