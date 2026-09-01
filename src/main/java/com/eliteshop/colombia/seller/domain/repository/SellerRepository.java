package com.eliteshop.colombia.seller.domain.repository;

import com.eliteshop.colombia.seller.domain.model.Seller;
import com.eliteshop.colombia.seller.domain.model.SellerDniNumber;
import com.eliteshop.colombia.seller.domain.model.SellerId;
import com.eliteshop.colombia.shared.domain.PageResult;
import java.util.List;
import java.util.Optional;

public interface SellerRepository {
  Seller save(Seller seller);

  void update(Seller seller);

  void delete(SellerId id);

  List<Seller> findAll();

  PageResult<Seller> findPage(int page, int size);

  Optional<Seller> findById(SellerId id);

  Optional<Seller> findByDniNumber(SellerDniNumber dniNumber);

  Optional<Seller> findByEmail(String email);
}
