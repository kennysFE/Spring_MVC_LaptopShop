package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {

    // DI: Dependencies injection
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product handleCreateProduct(Product product) {
        product = this.productRepository.save(product);
        System.out.println(product.toString());
        return product;
    }

    public List<Product> getAllProduct() {
        return this.productRepository.findAll();
    }

    public Optional<Product> getProductById(long id) {
        return this.productRepository.findById(id);
    }

    public Product handleUpdateProduct(Product product) {
        product = this.productRepository.save(product);
        System.out.println("Information product updated" + product.toString());
        return product;
    }

    public void handleDeleteProduct(long id) {
        this.productRepository.deleteById(id);
    }
}
