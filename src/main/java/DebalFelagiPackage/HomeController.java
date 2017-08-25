package DebalFelagiPackage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.cloudinary.utils.ObjectUtils;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
//For Email Service
import com.google.common.collect.Lists;
import it.ozimov.springboot.mail.model.defaultimpl.DefaultEmail;
import it.ozimov.springboot.mail.service.EmailService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.mail.internet.InternetAddress;


@Controller
public class HomeController {

    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CloudinaryConfig cloudc;
    @Autowired
    private TweeterRepository tweeterRepository;

    String ReceiverFullNameSession, SenderFullNameSession;

    @RequestMapping("/")
    public String index(Model model, User user)
    {
        model.addAttribute("user",new User());
        return "home";
    }
    @RequestMapping("/login")
    public String login(){
        return "login";
    }

    @RequestMapping(value="/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new User());
        return "register";
    }
    @RequestMapping(value="/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") User user, BindingResult result, Model model){
        model.addAttribute("user", user);
        userValidator.validate(user, result);
        if (result.hasErrors()) {
            return "register";
        } else {
            userService.saveUser(user);
            model.addAttribute("message", "User Account Successfully Created");
        }
        user = userRepository.findByUsername(user.getUsername());
        model.addAttribute("register1", user);
        return "/login";
    }

    @RequestMapping(value = "/loginSuccess", method = RequestMethod.GET)
    public String displayTemp(Model model)
    {
        Iterable<Tweeter> mList = tweeterRepository.findAll();
        model.addAttribute("mList", mList);
        return "/profile";
    }

    @RequestMapping(value = "/tweet", method = RequestMethod.GET)
    public String tweetGet(Model model){
        model.addAttribute("messaging", new Tweeter());
        return "tweet";
    }
    @RequestMapping(value = "/tweet", method = RequestMethod.POST)
    public String housePOST(@RequestParam("file") MultipartFile file, Tweeter tweeter, RedirectAttributes redirectAttributes,
                            Model model,  Principal principal, BindingResult result){

        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("message","Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {
            Map uploadResult =  cloudc.upload(file.getBytes(), ObjectUtils.asMap("resourcetype", "auto"));

            model.addAttribute("message","You successfully uploaded '" + file.getOriginalFilename() + "'");
            String filename = uploadResult.get("public_id").toString() + "." + uploadResult.get("format").toString();
            //String effect = p.getTitle();
            tweeter.setImage("<img src='http://res.cloudinary.com/henokzewdie/image/upload/" +filename+"' width='100px'/>");
          //  house.setDetailphoto("<img src='http://res.cloudinary.com/henokzewdie/image/upload/" +filename+"' width='500px'/>");

            //System.out.printf("%s\n", cloudc.createUrl(filename,900,900, "fit"));

        } catch (IOException e){
            e.printStackTrace();
            model.addAttribute("message", "Sorry I can't upload that!");
        }

        tweeter.setDate(new Date());
        tweeter.setUsername(principal.getName());
        tweeterRepository.save(tweeter);

        return "redirect:/loginSuccess";
    }

}