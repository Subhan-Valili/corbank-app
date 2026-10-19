package az.corbank.mscorbank.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleEntity extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    String name;

    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    @Builder.Default
    Set<UserEntity> users = new LinkedHashSet<>();

    @ToString.Exclude
    @ManyToMany(fetch =  FetchType.EAGER)
    @JoinTable(name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    Set<PermissionEntity> permissions = new LinkedHashSet<>();

}