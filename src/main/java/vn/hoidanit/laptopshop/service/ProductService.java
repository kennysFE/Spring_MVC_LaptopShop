package vn.hoidanit.laptopshop.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {

    // DI: Dependencies injection
    private final ProductRepository productRepository;

    private final CartDetailRepository cartDetailRepository;

    private final CartRepository cartRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final OrderRepository orderRepository;

    private final UserService userService;

    public ProductService(ProductRepository productRepository, CartDetailRepository cartDetailRepository,
            CartRepository cartRepository, OrderDetailRepository orderDetailRepository, OrderRepository orderRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
    }

    public Product handleCreateProduct(Product product) {
        product = this.productRepository.save(product);
        System.out.println(product.toString());
        return product;
    }

    // public Page<Product> getProductPaginationWithFilter(Pageable page, String
    // name) {
    // return this.productRepository.findAll(ProductSpecs.nameLike(name), page);
    // }

    public Page<Product> getProductPaginationWithFilter(Pageable page, String matchMultipleString) {

        double min;
        double max;

        if (matchMultipleString.equals("10-toi-15-trieu")) {
            min = 10000000;
            max = 15000000;
        } else if (matchMultipleString.equals("15-toi-30-trieu")) {
            min = 15000000;
            max = 30000000;
        } else {
            min = 0;
            max = 0;
        }

        return max == 0 && min == 0 ? this.productRepository.findAll(page)
                : this.productRepository.findAll(ProductSpecs.matchPrice(min, max), page);
    }

    public Page<Product> getProductPaginationWithFilter(Pageable page, List<String> price) {
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        int count = 0;
        for (String p : price) {
            double min = 0;
            double max = 0;

            switch (p) {
                case "10-toi-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    count++;
                    break;
                case "15-toi-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    count++;
                    break;
                case "20-toi-30-trieu":
                    min = 20000000;
                    max = 30000000;
                    count++;
                    break;
                // Add more cases as needed

            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiplePrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        if (count == 0) {
            return this.productRepository.findAll(page);
        }

        return this.productRepository.findAll(combinedSpec, page);

    }

    public List<Product> getAllProduct() {
        return this.productRepository.findAll();
    }

    public Page<Product> getProductPagination(Pageable page) {
        return this.productRepository.findAll(page);
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

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        // Check user has cart ? if not -> create new cart
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // if user already has cart on db => get cart from db
            Cart cart = this.cartRepository.findByUser(user);

            // if cart not found -> create new cart
            if (cart == null) {

                Cart ortherCart = new Cart();
                ortherCart.setUser(user);
                ortherCart.setSum(0);

                cart = this.cartRepository.save(ortherCart);
            }

            // Find product by id

            Optional<Product> product = this.productRepository.findById(productId);

            // Product not empty
            if (product.isPresent()) {

                // Get product
                Product detailProduct = product.get();

                // Check product is existing on cart already ?
                CartDetail oldDetailCart = this.cartDetailRepository.findByCartAndProduct(cart, detailProduct);

                // if cartDetail is null
                if (oldDetailCart == null) {
                    // Create new Cart_detail
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProduct(detailProduct);
                    cartDetail.setQuantity(quantity);
                    cartDetail.setPrice(detailProduct.getPrice());

                    // Update sum (Cart)
                    int s = cart.getSum() + 1;
                    cart.setSum(s);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", s);

                    // Save cart_detail
                    this.cartDetailRepository.save(cartDetail);
                } else {
                    oldDetailCart.setQuantity(oldDetailCart.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetailCart);
                }

            }

        }

    }

    public Cart handleGetCardByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleDeleteProductOnCart(long cartDetailId, HttpSession session) {
        // Get Cart-Detail
        Optional<CartDetail> cartDetail = this.cartDetailRepository.findById(cartDetailId);

        if (cartDetail.isPresent()) {
            Cart cart = cartDetail.get().getCart();

            // Delete Cart-Detail
            this.cartDetailRepository.deleteById(cartDetailId);

            // Delete Cart
            if (cart.getSum() > 1) {
                int s = cart.getSum() - 1;
                cart.setSum(s);
                session.setAttribute("sum", s);
            } else {
                this.cartRepository.deleteById(cart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateProductBeforeCheckOut(List<CartDetail> cartDetails) {
        // for cart detail => gt cart detail by id =>
        for (CartDetail cartDetail : cartDetails) {
            // get cartDetail by id
            Optional<CartDetail> cartDetailById = this.cartDetailRepository.findById(cartDetail.getId());
            if (cartDetailById.isPresent()) {
                CartDetail cd = cartDetailById.get();
                cd.setQuantity(cartDetail.getQuantity());
                // Save cart detail by id
                this.cartDetailRepository.save(cd);
            }
        }
    }

    public void handlePlaceOrder(User currentUser, String receiverName, String receiverAddress, String receiverPhone,
            HttpSession session) {

        // Get cart by user id
        Cart cart = this.cartRepository.findByUser(currentUser);

        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {
                // Create Order
                Order order = new Order();
                order.setUser(currentUser);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverName(receiverName);
                order.setStatus("PENDING");
                order.setReceiverPhone(receiverPhone);

                // Get totalPrice for Order
                double sum = 0;
                for (CartDetail cartDetail : cartDetails) {
                    sum += cartDetail.getQuantity() * cartDetail.getPrice();
                }
                order.setTotalPrice(sum);

                // Save Order
                order = this.orderRepository.save(order);

                for (CartDetail cartDetail : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setProduct(cartDetail.getProduct());
                    orderDetail.setQuantity(cartDetail.getQuantity());
                    orderDetail.setPrice(cartDetail.getPrice());
                    orderDetail.setOrder(order);

                    this.orderDetailRepository.save(orderDetail);
                }

                // Delete Cart Detail
                for (CartDetail cartDetail : cartDetails) {
                    this.cartDetailRepository.deleteById(cartDetail.getId());
                }

                // Delete Cart
                this.cartRepository.deleteById(cart.getId());

                // Update Session sum
                session.setAttribute("sum", 0);
            }

        }

    }

    public long countProducts() {
        return this.productRepository.count();
    }
}
