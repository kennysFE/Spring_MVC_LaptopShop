package vn.hoidanit.laptopshop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;

@Service
public class OrderService {

    // DI: Dependencies injection
    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;

    }

    public void saveOrder(Order order) {
        this.orderRepository.save(order);
    }

    public List<Order> handleGetAllOrder() {
        return this.orderRepository.findAll();
    }

    public Optional<Order> getOrderById(long id) {
        return this.orderRepository.findById(id);
    }

    public List<OrderDetail> handleGetListOrderDetailsByOrderId(long id) {

        Optional<Order> order = this.orderRepository.findById(id);

        List<OrderDetail> orderDetails = order == null ? new ArrayList<>() : order.get().getOrderDetails();

        return orderDetails;
    }

    public void handleUpdateOrderDetailByOrderId(Order currentOrder) {

        Optional<Order> order = this.orderRepository.findById(currentOrder.getId());

        if (order.isPresent()) {
            Order orderInformation = order.get();
            orderInformation.setStatus(currentOrder.getStatus());
            this.orderRepository.save(orderInformation);
        }

    }

    public void handleDeleteOrderById(long id) {

        // Get order
        Optional<Order> order = this.orderRepository.findById(id);

        // delete order details
        if (order.isPresent()) {
            // get order detail
            Order currentOrder = order.get();
            List<OrderDetail> orderDetails = currentOrder.getOrderDetails();

            for (OrderDetail orderDetail : orderDetails) {
                this.orderDetailRepository.deleteById(orderDetail.getId());
            }
            this.orderRepository.deleteById(currentOrder.getId());
        }

    }

    public long countOrders() {
        return this.orderRepository.count();
    }
}
