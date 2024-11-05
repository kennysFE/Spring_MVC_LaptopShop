package vn.hoidanit.laptopshop.service.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;

public class ProductSpecs {
    // filter search by name
    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

    // filter by min price product
    public static Specification<Product> minPrice(double minPrice) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.PRICE),
                minPrice);
    }

    // filter by max price product
    public static Specification<Product> maxPrice(double maxPrice) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(Product_.PRICE), maxPrice);
    }

    // filter by factory name
    public static Specification<Product> nameFactoryLike(String nameFactory) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), nameFactory);
    }

    // Filter by factory name on list of products (Hp , Dell , MacBook )
    public static Specification<Product> nameFactorysLike(List<String> nameFactoryList) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get(Product_.FACTORY)).value(nameFactoryList);
    }

    // Filter by 10 000 000 <= price <= 15 000 000
    public static Specification<Product> matchPrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(root.get(Product_.PRICE), min),
                criteriaBuilder.lessThanOrEqualTo(root.get(Product_.PRICE), max));
    }

    // filter by 10 000 000 <= price <= 15 000 000 and 16 000 000 <= price <= 20 000
    // 000
    public static Specification<Product> matchMultiplePrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(Product_.PRICE), min, max);
    }

}
