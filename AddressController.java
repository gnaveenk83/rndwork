import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AddressController {

    private List<Address> addresses = new ArrayList<>(); // Mock database/session

    public AddressController() {
        // Initialize with some sample data
        addresses.add(new Address("123 Main St", "Home"));
        addresses.add(new Address("456 Elm St", "Office"));
    }

    // Show form with initial addresses
    @GetMapping("/form")
    public String showForm(Model model) {
        User user = new User(addresses);
        model.addAttribute("user", user);
        return "main";
    }

    // Add a new address dynamically
    @GetMapping("/add-address")
    public String addAddressBlock(@RequestParam int index, Model model) {
        Address newAddress = new Address(); // Create a placeholder for a new address
        model.addAttribute("address", newAddress);
        model.addAttribute("index", index); // Pass the index for unique naming
        return "fragments/addressDetails :: addressDetail"; // Return the fragment for the new address block
    }

    // Delete an address dynamically
    @GetMapping("/delete-address")
    public String deleteAddress(@RequestParam int index, Model model) {
        if (index < addresses.size()) {
            addresses.remove(index); // Remove the address from the list (mock session/database)
        }
        model.addAttribute("user", new User(addresses)); // Return the updated list
        return "fragments/addressList :: addressList"; // Return the updated address list fragment
    }
}
