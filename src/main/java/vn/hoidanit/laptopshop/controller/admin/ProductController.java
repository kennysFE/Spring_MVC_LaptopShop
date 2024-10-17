package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.service.ProductService;
import vn.hoidanit.laptopshop.service.UploadService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@Controller
public class ProductController {

    private ProductService productService;

    private UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.productService = productService;
        this.uploadService = uploadService;
    }

    @GetMapping("/admin/product")
    public String getProduct(Model model) {
        List<Product> listProduct = this.productService.getAllProduct();
        model.addAttribute("listProduct", listProduct);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping("/admin/product/create")
    public String postCreateProductPage(Model model, @ModelAttribute("newProduct") @Valid Product newProduct,
            BindingResult newProductBindingResult, @RequestParam("imageProduct") MultipartFile file) {

        // Check field error => get message
        List<FieldError> errors = newProductBindingResult.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println(error.getField() + " - " + error.getDefaultMessage());
        }

        // Check validation file image => if true =>
        if (file.isEmpty()) {
            model.addAttribute("errorImageProduct", " Image Product can not be empty");
            return "/admin/product/create";
        }

        // Check validation variable new product => if true => print error message
        if (newProductBindingResult.hasErrors()) {
            return "/admin/product/create";
        }

        // Handle create product
        String avatarImageUpload = this.uploadService.handleSaveUploadFile(file, "product");
        newProduct.setImage(avatarImageUpload);
        this.productService.handleCreateProduct(newProduct);
        return "redirect:/admin/product";
    }
}
