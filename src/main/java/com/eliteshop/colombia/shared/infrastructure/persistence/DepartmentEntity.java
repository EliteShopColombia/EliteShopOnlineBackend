package com.eliteshop.colombia.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "department")
public class DepartmentEntity {

  @Id
  @Column(name = "id")
  private Integer id;

  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;
}
