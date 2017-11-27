package hello;

/**
 * Created by Lotus on 10/5/2017.
 */
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/classDetail/{class}").setViewName("classDetail");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/version").setViewName("version");
    }

}
