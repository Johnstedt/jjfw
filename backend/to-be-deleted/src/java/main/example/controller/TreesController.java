package Main.example.controller;

import generated.jooq.tables.pojos.Trees;
import Main.example.service.TreesService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/trees")
@Tag(name = "Trees", description = "Trees management API")
public class TreesController extends BaseController<Trees, TreesService> {

    public TreesController(TreesService service) {
        super(service);
    }
}
