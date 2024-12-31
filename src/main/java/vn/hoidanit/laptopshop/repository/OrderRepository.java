package vn.hoidanit.laptopshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Order save(Order order);

    List<Order> findAll();

    Optional<Order> findById(long id);

    void deleteById(Long id);

    List<Order> findByUser(User user);

    Page<Order> findAll(Pageable page);

    Optional<Order> findByPaymentRef(String paymentRef);
}
