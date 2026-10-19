package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.adapter.in.web.dto.DashboardResponseDto;
import az.corbank.mscorbank.adapter.in.web.dto.ProjectDto;
import az.corbank.mscorbank.application.port.in.GetDashboardUseCase;
import az.corbank.mscorbank.application.port.in.GetProjectUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardWebController {

    private final GetDashboardUseCase getDashboard;
    private final GetProjectUseCase getProject;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getDashboard(
            @RequestParam(value = "bank", required = false) String bank) {
        return ResponseEntity.ok(WebMapper.toDto(getDashboard.getDashboard(WebMapper.parseBank(bank))));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectDto> getProject(@PathVariable("id") String id) {
        return getProject.getProject(id).map(p -> ResponseEntity.ok(WebMapper.toDto(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
