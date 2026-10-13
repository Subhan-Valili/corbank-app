package az.ingress.mspashabank.entity;

import az.ingress.mspashabank.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerEntity extends BaseEntity {

    @Column(name = "customer_no", unique = true, nullable = false)
    String customerNo; // "CUST-998877"

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false, length = 20)
    CustomerType customerType;

    // Fiziki şəxs və fərdi sahibkar üçün. Korporativdə null ola bilər.
    @Column(name = "pin", unique = true, length = 7)
    String pin; // FİN, 7 simvol

    @Column(name = "name")
    String name;

    @Column(name = "surname")
    String surname;

    // Korporativ müştəri və fərdi sahibkar üçün.
    @Column(name = "tin", unique = true, length = 10)
    String tin; // VÖEN, 10 rəqəm

    @Column(name = "company_name")
    String companyName;

    @Column(name = "mobile_number", nullable = false, length = 20)
    String mobileNumber; // boşluqsuz: "+994501112233"

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    Set<PashaAccountEntity> accounts = new LinkedHashSet<>();

}