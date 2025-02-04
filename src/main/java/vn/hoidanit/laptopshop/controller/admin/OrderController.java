package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Order_;
import vn.hoidanit.laptopshop.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/order")
    public String getOrder(Model model, @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;

        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception e) {

        }

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Order_.ID).descending());
        Page<Order> pageOrder = this.orderService.getPaginationAllOrder(pageable);

        List<Order> orders = pageOrder.getContent();
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageOrder.getTotalPages());
        return "admin/order/show";
    }

    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {
        // Get order by id
        Order order = this.orderService.getOrderById(id).get();
        model.addAttribute("order", order);
        return "admin/order/update";
    }

    @PostMapping("admin/order/update")
    public String postUpdateOrderPage(Model model, @ModelAttribute("order") Order currentOrder) {
        this.orderService.handleUpdateOrderDetailByOrderId(currentOrder);
        return "redirect:/admin/order";
    }

    // Detail
    @GetMapping("/admin/order/{id}")
    public String getDetailOrderPage(Model model, @PathVariable long id) {
        long orderId = id;
        List<OrderDetail> orderDetails = this.orderService.handleGetListOrderDetailsByOrderId(orderId);
        model.addAttribute("orderDetails", orderDetails);
        return "admin/order/detail";
    }

    // Delete
    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrderPage(Model model, @PathVariable long id) {
        model.addAttribute("order", new Order());
        model.addAttribute("id", id);
        return "admin/order/delete";
    }

    @PostMapping("/admin/order/delete")
    public String postDeleteOrderPage(Model model, @ModelAttribute("order") Order order) {
        this.orderService.handleDeleteOrderById(order.getId());
        return "redirect:/admin/order";
    }

}
