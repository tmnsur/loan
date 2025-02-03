package com.tanermansur.loan.service;

import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.exception.CustomerNotFoundException;
import com.tanermansur.loan.exception.InsufficientCustomerLimitException;
import com.tanermansur.loan.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CustomerServiceTest {
    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository mockCustomerRepository;

    @Test
    void checkInsufficientCustomerLimit() {
        Customer customer = mock(Customer.class);

        when(customer.getCreditLimit()).thenReturn(new BigDecimal("100"));

        assertDoesNotThrow(() -> customerService.checkInsufficientCustomerLimit(customer, new BigDecimal("10")));
        assertThrows(InsufficientCustomerLimitException.class, () -> customerService.checkInsufficientCustomerLimit(customer, new BigDecimal("101")));

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("10"));

        assertDoesNotThrow(() -> customerService.checkInsufficientCustomerLimit(customer, new BigDecimal("90")));
        assertThrows(InsufficientCustomerLimitException.class, () -> customerService.checkInsufficientCustomerLimit(customer, new BigDecimal("91")));
    }

    @Test
    void loadUserByUsernameWhenUserIsAdmin() {
        UserDetails userDetails = customerService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameWhenUserIsCustomer() {
        Customer customer = new Customer();

        customer.setName("first");
        customer.setSurname("customer");

        when(mockCustomerRepository.findCustomerByNameAndSurname("first", "customer"))
                .thenReturn(Optional.of(customer));

        UserDetails userDetails = customerService.loadUserByUsername("first.customer");

        assertEquals(customer, userDetails);
        assertEquals("first.customer", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsernameWhenInvalidUsername() {
        assertThrows(CustomerNotFoundException.class, () -> customerService.loadUserByUsername("invalid"));
    }

    @Test
    void loadUserByUsernameWhenCustomerNotFound() {
        assertThrows(CustomerNotFoundException.class, () -> customerService.loadUserByUsername("unknown.user"));
    }

    @Test
    void updateCustomer() {
        Customer customer = mock(Customer.class);

        customerService.updateCustomer(customer);

        verify(mockCustomerRepository).save(customer);
    }
}