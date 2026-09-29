package az.corbank.mscorbank.adapter.out.demo;

import az.corbank.mscorbank.domain.model.Project;
import az.corbank.mscorbank.domain.model.Project.Detail;
import az.corbank.mscorbank.domain.model.ProjectType;
import az.corbank.mscorbank.application.port.out.ProjectPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Credit / deposit / payroll have no counterpart in any integrated bank yet — static demo data. */
@Component
class StaticProjectAdapter implements ProjectPort {

    private static final List<Project> PROJECTS = List.of(
            new Project("credit-01", ProjectType.CREDIT, "Biznes krediti", new BigDecimal("50000"), "AZN", null,
                    List.of(new Detail("Qalıq məbləğ", "50 000 AZN"),
                            new Detail("Faiz dərəcəsi", "14%"),
                            new Detail("Növbəti ödəniş", "01.05.2025"))),
            new Project("deposit-01", ProjectType.DEPOSIT, "Müddətli depozit", new BigDecimal("120000"), "AZN", null,
                    List.of(new Detail("Əsas məbləğ", "120 000 AZN"),
                            new Detail("Faiz dərəcəsi", "9,5%"),
                            new Detail("Bitmə tarixi", "14.10.2025"))),
            new Project("payroll-01", ProjectType.PAYROLL, "Əmək haqqı layihəsi", null, null, "24 əməkdaş",
                    List.of(new Detail("Əməkdaş sayı", "24 əməkdaş"),
                            new Detail("Son köçürmə", "01.04.2025"),
                            new Detail("Növbəti köçürmə", "01.05.2025"))));

    @Override
    public List<Project> findAll() {
        return PROJECTS;
    }

    @Override
    public Optional<Project> findById(String id) {
        return PROJECTS.stream().filter(p -> p.id().equals(id)).findFirst();
    }
}
