package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;

@Service
public class ProductService {

    // DI: Dependencies injection
    private final ProductRepository productRepository;

    private final CartDetailRepository cartDetailRepository;

    private final CartRepository cartRepository;

    private final UserService userService;

    public ProductService(ProductRepository productRepository, CartDetailRepository cartDetailRepository,
            CartRepository cartRepository, UserService userService) {
        this.productRepository = productRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
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

    public void handleAddProductToCart(String email, long productId) {
        // Check user has cart ? if not -> create new cart
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // if user already has cart on db => get cart from db
            Cart cart = this.cartRepository.findByUser(user);

            // if cart not found -> create new cart
            if (cart == null) {

                Cart ortherCart = new Cart();
                ortherCart.setUser(user);
                ortherCart.setSum(1);

                cart = this.cartRepository.save(ortherCart);
            }

            // Find product by id

            Optional<Product> product = this.productRepository.findById(productId);

            // Product not empty
            if (product.isPresent()) {

                // Get product
                Product detailProduct = product.get();

                // Create new Cart_detail
                CartDetail cartDetail = new CartDetail();
                cartDetail.setCart(cart);
                cartDetail.setProduct(detailProduct);
                cartDetail.setQuantity(1);
                cartDetail.setPrice(detailProduct.getPrice());

                // Save cart_detail
                this.cartDetailRepository.save(cartDetail);

            }

        }

    }
}
