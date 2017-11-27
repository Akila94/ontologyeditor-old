package hello.controller;

import hello.service.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class VersionController {

    @Autowired
    DBService dbService;

    @RequestMapping("/version")
    public String getDataPropertyHierarchy(Model model){
        model.addAttribute("module", "versionInfo");
        return "version";
    }

    @GetMapping(value = "/mainChanges")
    public ResponseEntity<?>getMainChanges(Model model, HttpSession session){

        return ResponseEntity.ok(dbService.getAllChanges());

    }

}
