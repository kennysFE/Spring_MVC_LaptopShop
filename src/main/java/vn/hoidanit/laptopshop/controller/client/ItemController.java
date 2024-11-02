package vn.hoidanit.laptopshop.controller.client;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.service.OrderService;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ItemController {

    private final ProductService productService;

    private final UserService userService;

    private final OrderService orderService;

    public ItemController(ProductService productService, UserService userService, OrderService orderService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        Product productItem = this.productService.getProductById(id).get();
        model.addAttribute("id", id);
        model.addAttribute("productItem", productItem);
        return "client/product/detail";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String postMethodName(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long productId = id;

        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, productId, session, 1);

        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {

        User currentUser = new User();
        // Get User
        HttpSession session = request.getSession(false);
        long userId = (long) session.getAttribute("id");
        currentUser.setId(userId);
        User userLoginDetail = this.userService.getUserById(userId);

        // Get Card by User
        Cart cartByUser = this.productService.handleGetCardByUser(userLoginDetail);

        // Get detail card => if cardDetail is null => create a new array with empty
        // values
        List<CartDetail> cartDetails = cartByUser == null ? new ArrayList<CartDetail>() : cartByUser.getCartDetails();

        // Total price
        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        model.addAttribute("cart", cartByUser);
        // Return card
        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteProductOnCartPage(Model model, @PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        long cartDetailId = id;
        this.productService.handleDeleteProductOnCart(cartDetailId, session);
        return "redirect:/cart";
    }

    @PostMapping("/confirm-checkout")
    public String postMethodName(Model model, @ModelAttribute("cart") Cart cart) {
        // Get list cart detail
        List<CartDetail> cartDetails = cart == null ? new ArrayList<CartDetail>() : cart.getCartDetails();

        // handle change quantity with each cart detailUser
        this.productService.handleUpdateProductBeforeCheckOut(cartDetails);

        return "redirect:/checkout";
    }

    @GetMapping("/checkout")
    public String getCheckOutPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        // Get User
        HttpSession session = request.getSession(false);
        long userId = (long) session.getAttribute("id");
        currentUser.setId(userId);
        User userLoginDetail = this.userService.getUserById(userId);

        // Get Card by User
        Cart cartByUser = this.productService.handleGetCardByUser(userLoginDetail);

        // Get detail card => if cardDetail is null => create a new array with empty
        // values
        List<CartDetail> cartDetails = cartByUser == null ? new ArrayList<CartDetail>() : cartByUser.getCartDetails();

        // Total price
        double totalPrice = 0;
        for (CartDetail cartDetail : cartDetails) {
            totalPrice += cartDetail.getPrice() * cartDetail.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        return "client/cart/checkout";
    }

    @PostMapping("/place-order")
    public String postPlaceOrder(Model model, HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone) {

        User currentUser = new User(); // null
        HttpSession session = request.getSession(false);
        currentUser.setId((long) session.getAttribute("id"));

        this.productService.handlePlaceOrder(currentUser, receiverName, receiverAddress, receiverPhone, session);

        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String getThanksPage() {
        return "client/cart/thanks";
    }

    @GetMapping("/order-history")
    public String getOrderHistory(Model model, HttpServletRequest request) {
        // Get user id
        User user = new User();
        HttpSession session = request.getSession(false);
        long id = (long) session.getAttribute("id");
        user.setId(id);

        // handle get order by user

        List<Order> orders = this.orderService.getOrdersByUser(user);

        model.addAttribute("orders", orders);

        return "client/cart/order-history";
    }

    @PostMapping("/add-product-from-view-detail")
    public String postAddProductFromViewDetail(Model model, HttpServletRequest request, @RequestParam("id") long id,
            @RequestParam("quantity") long quantity) {

        HttpSession session = request.getSession(false);

        String email = (String) session.getAttribute("email");

        this.productService.handleAddProductToCart(email, id, session, quantity);

        return "redirect:/product/" + id;
    }

    @GetMapping("/products")
    public String getProductsPage(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;

        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        Pageable pageable = PageRequest.of(page - 1, 6);
        Page<Product> pageProduct = this.productService.getProductPagination(pageable);
        List<Product> products = pageProduct.getContent();

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageProduct.getTotalPages());

        return "client/product/show";
    }

}
