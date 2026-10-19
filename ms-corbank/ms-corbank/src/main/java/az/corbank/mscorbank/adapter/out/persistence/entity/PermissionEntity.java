package az.corbank.mscorbank.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.security.Permission;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    String name;

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    @Builder.Default
    Set<RoleEntity> roles = new LinkedHashSet<>();

}