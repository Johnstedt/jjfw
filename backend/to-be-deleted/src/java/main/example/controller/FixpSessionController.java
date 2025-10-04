package Main.example.controller;

import generated.jooq.tables.pojos.FixpSession;
import Main.example.service.FixpSessionService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fixp-session")
@Tag(name = "FIXP Session", description = "FIXP Session management API")
public class FixpSessionController extends BaseController<FixpSession, FixpSessionService> {

    public FixpSessionController(FixpSessionService service) {
        super(service);
    }
}
