package hello.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String greeting( Model model) {
       // model.addAttribute("name", name);
        model.addAttribute("module", "home");
        return "home";
    }
}
