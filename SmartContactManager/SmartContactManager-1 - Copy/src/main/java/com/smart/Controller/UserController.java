package com.smart.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mysql.cj.Session;
import com.smart.dao.UserRepository;
import com.smart.dao.contactRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;

import javassist.expr.NewArray;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private contactRepository contactRepository;
   
	@Autowired
	private BCryptPasswordEncoder bcPasswordEncoder;

	// method adding common data to response
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		String username = principal.getName();
		// getuser details
		User user = userRepository.getUserByUserName(username);
		model.addAttribute("user", user);

	}

	// home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");

		String username = principal.getName();

		System.err.println("username = " + username);

		// getuser details

		User user = userRepository.getUserByUserName(username);

		System.err.println("user = " + user);
		model.addAttribute("user", user);
		return "normal/user_dashboard";
	}

	@GetMapping("/add_contact")
	public String addContact(Model model) {
		model.addAttribute("title", "About");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// process to add contact
	@PostMapping("/process_contact")
	public String contact_process(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);
			if (!file.isEmpty()) {

				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			contact.setUser(user);
			user.getContact().add(contact);
			userRepository.save(user);
			System.err.println("user Data " + contact);
			session.setAttribute("message", new com.smart.helper.Message("Your Contact added", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new com.smart.helper.Message("something went wrong", "danger"));
		}
		return "normal/add_contact_form";
	}

	// show contact handler
	// per page =5[n]
	// current page=0[page]

	@GetMapping("/show_contact/{page}")
	public String showContact(
	        @PathVariable("page") Integer page,
	        @RequestParam(name = "contact_name", required = false) String contactName,
	        Model model,
	        Principal principal
	) {
	    model.addAttribute("title", "View Contacts");
	    String userName = principal.getName();
	    User user = userRepository.getUserByUserName(userName);

	    Pageable pageable = PageRequest.of(page, 2);

	    Page<Contact> contacts;
	    if (contactName != null) {
	        contacts = contactRepository.findByNameContainingAndUser(contactName, user, pageable);
	    } else {
	        contacts = contactRepository.findContactByUser(user.getId(), pageable);
	    }

	    model.addAttribute("contacts", contacts.getContent());
	    model.addAttribute("currentPage", page);
	    model.addAttribute("totalPages", contacts.getTotalPages());

	    return "normal/show_contacts";
	}

	// show specific contact
	@RequestMapping("/{cId}/contact")
	public String showPerticularContact(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		return "normal/contact_detail";
	}
	
	
	// show specific contact by name
	@RequestMapping("/serach_contact_name")
	public String listofContactByName( Model model, Principal principal,@RequestParam("contact_name") String contactName) {
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
	List<Contact> contactList=	contactRepository.findByNameContainingAndUser(contactName, user);
	model.addAttribute("contacts", contactList);
		return "normal/show_contacts";
	}

	// delete contact

	@GetMapping("/{cId}/delete")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		Contact contact = this.contactRepository.findById(cId).get();
		contact.setUser(null);
		this.contactRepository.delete(contact);

		session.setAttribute("message", new com.smart.helper.Message("Contact Deleted Successfuly", "success"));

		return "redirect:/user/show_contact/0";
	}

	// Update contact Data
	@PostMapping("/{cId}/update")
	public String contactData(@PathVariable("cId") Integer cId, Model model) {
		model.addAttribute("title", "Update Contacts");
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact", contact);
		return "normal/updata_form";
	}

	// process update contact
	@PostMapping("/process_update")
	public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		// get old contact detail
		Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
		try {
			if (!file.isEmpty()) {

				// deleting old contact detail image
				File deleteFile = new ClassPathResource("static/images").getFile();
				File confirmDelete = new File(deleteFile, oldContactDetails.getImage());
				confirmDelete.delete();
				// adding new contact detail image
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}

			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			session.setAttribute("message", new com.smart.helper.Message("Update Successfuly", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// View Your Profile
	@GetMapping("/profile")
	public String yourProfile() {

		return "normal/profile";
	}
	
	@GetMapping("/changePassword")
	public String openSetting() {
		
		return "normal/ChangePassword";
	}

	// change password
     @GetMapping("/changePasswordProcess") 
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("NewPassword") String NewPassword, Principal principal,HttpSession session) {
		String userName = principal.getName(); // this pass the login user email id
		User user = userRepository.getUserByUserName(userName);
		System.err.println("oldPassword"+oldPassword);
		System.err.println("NewPassword"+NewPassword);
		
		System.out.println(user.getPassword());
		if (bcPasswordEncoder.matches( oldPassword,user.getPassword())) {

			user.setPassword(bcPasswordEncoder.encode(NewPassword));
            userRepository.save(user);
            System.err.println("enter correct password");
            session.setAttribute("message", new com.smart.helper.Message("Password Changed Successfuly", "success"));
            return "redirect:/user/index";
            
		}
		else {
			session.setAttribute("message", new com.smart.helper.Message("Password Not Match", "danger"));
			System.err.println("enter wrong password");
			return "normal/ChangePassword";
		}
		
	}
}
